/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ox.oucs.vle.email.EmailSendingService;
import uk.ac.ox.oucs.vle.email.StateChange;

public class CourseSignupServiceImpl implements CourseSignupService {
	
	private final static Log log = LogFactory.getLog(CourseSignupServiceImpl.class);

	private CourseDAO dao;
	private SakaiProxy proxy;
	private SearchService searchService;
	private EmailSendingService emailSendingService;
	private NowService now;

	protected final Comparator<CourseGroup> noDateCompatator = new NoDateComparator();

	public void setDao(CourseDAO dao) {
		this.dao = dao;
	}
	
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNowService(NowService now) {
		this.now = now;
	}

	public void setEmailSendingService(EmailSendingService emailSendingService) {
		this.emailSendingService = emailSendingService;
	}

	/**
	 * 
	 */
	public void approve(String signupId) {
		approve(signupId, false, proxy.getCurrentPlacementId());
	}
	
	public void approve(String signupId, boolean skipAuth, String placementId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		CourseGroupDAO groupDao = signupDao.getGroup();
		if (!skipAuth) {
			if (groupDao.getSupervisorApproval()) {
				String currentUserId = proxy.getCurrentUser().getId();
				boolean canApprove = false;
				if (currentUserId.equals(signupDao.getSupervisorId())) {
					canApprove = true;
				} else {
					canApprove = isAdministrator(groupDao, currentUserId, canApprove);
				}
				if (!canApprove) {
					throw new PermissionDeniedException(currentUserId);
				}
			}
		}
		Status oldStatus = signupDao.getStatus();
		signupDao.setStatus(Status.APPROVED);
		signupDao.setAmended(getNow());
		dao.save(signupDao);
		proxy.logEvent(groupDao.getCourseId(), EVENT_SIGNUP, placementId);
		
		//departmental approval
		boolean departmentApproval = false;
		CourseDepartmentDAO departmentDao = null;
		if (null != signupDao.getDepartment()) {
			departmentDao = dao.findDepartmentByCode(signupDao.getDepartment());
			if (null != departmentDao) {
				if (departmentDao.getApprove()) {
					if (!departmentDao.getApprovers().isEmpty()) {
						departmentApproval = true;
					}
				}
			}
		}
		
		if (departmentApproval) {
			sendMails(oldStatus, signupId, placementId, emailSendingService);
		} else {
			confirm(signupId, skipAuth, placementId);
		}
	}
	
	/**
	 * 
	 */
	public void accept(String signupId) {
		accept(signupId, false, proxy.getCurrentPlacementId());
	}
	
	public void accept(String signupId, boolean skipAuth, String placementId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		CourseGroupDAO groupDao = signupDao.getGroup();
		if (!skipAuth) {
			if (groupDao.getAdministratorApproval()) {
					String currentUserId = proxy.getCurrentUser().getId();
				boolean canAccept = false;
				// If is course admin on one of the components.
				canAccept = isAdministrator(signupDao.getGroup(), currentUserId, canAccept);
				if (!canAccept) {
					throw new PermissionDeniedException(currentUserId);
				}
			}
		}
		
		if (!Status.PENDING.equals(signupDao.getStatus()) &&
			!Status.WAITING.equals(signupDao.getStatus())) {
			throw new IllegalStateException("You can only accept signups that are waiting or pending.");
		}
		for (CourseComponentDAO componentDao : signupDao.getComponents()) {
			componentDao.setTaken(componentDao.getTaken()+1);
			dao.save(componentDao);
		}
		Status oldStatus = signupDao.getStatus();
		signupDao.setStatus(Status.ACCEPTED);
		signupDao.setAmended(getNow());
		dao.save(signupDao);

		// Send emails

		proxy.logEvent(signupDao.getGroup().getCourseId(), EVENT_ACCEPT, placementId);
		
		if (groupDao.getSupervisorApproval()) {
			sendMails(oldStatus, signupId, placementId, emailSendingService);
		} else {
			approve(signupId, skipAuth, placementId);
		}
	}
	
	/**
	 * 
	 */
	public void confirm(String signupId) {
		confirm(signupId, false, proxy.getCurrentPlacementId());
	}
	public void confirm(String signupId, boolean skipAuth, String placementId) {
		
		String currentUserId = proxy.getCurrentUser().getId();
		
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		CourseGroupDAO groupDao = signupDao.getGroup();
		
		boolean departmentApproval = false;
		if (null != signupDao.getDepartment()) {
			CourseDepartmentDAO department = dao.findDepartmentByCode(signupDao.getDepartment());
			if (null != department) {
				departmentApproval = department.getApprove();
			}
		}
			
		if (!skipAuth) {
			if (departmentApproval) {
				List<CourseDepartmentDAO> departments = dao.findApproverDepartments(currentUserId);
				boolean canConfirm = false;
				for (CourseDepartmentDAO dept : departments) {
					if (dept.getCode().equals(signupDao.getDepartment())) {
						canConfirm = true;
						break;
					}
				}
				if (!canConfirm) {
					throw new PermissionDeniedException(currentUserId);
				}
			}
		}

		Status oldStatus = signupDao.getStatus();
		signupDao.setStatus(Status.CONFIRMED);
		signupDao.setAmended(getNow());
		dao.save(signupDao);

		proxy.logEvent(groupDao.getCourseId(), EVENT_SIGNUP, placementId);
		sendMails(oldStatus, signupId, placementId, emailSendingService);
	}
	
	/**
	 * 
	 */
	public void waiting(String signupId) {
		waiting(signupId, false, proxy.getCurrentPlacementId());
	}
	
	public void waiting(String signupId, boolean skipAuth, String placementId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		CourseGroupDAO groupDao = signupDao.getGroup();
		if (!skipAuth) {
			if (groupDao.getAdministratorApproval()) {
					String currentUserId = proxy.getCurrentUser().getId();
				boolean canAccept = false;
				// If is course admin on one of the components.
				canAccept = isAdministrator(signupDao.getGroup(), currentUserId, canAccept);
				if (!canAccept) {
					throw new PermissionDeniedException(currentUserId);
				}
			}
		}
		
		if (!Status.PENDING.equals(signupDao.getStatus())) {
			throw new IllegalStateException("You can only add to waiting list signups that pending.");
		}

		Status oldStatus = signupDao.getStatus();
		signupDao.setStatus(Status.WAITING);
		signupDao.setAmended(getNow());
		dao.save(signupDao);
		proxy.logEvent(signupDao.getGroup().getCourseId(), EVENT_WAITING, placementId);
		sendMails(oldStatus, signupId, placementId, emailSendingService);
	}

	/**
	 * 
	 */
	public String findSupervisor(String search) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 
	 */
	public void setSupervisor(String signupId, String supervisorId) {
		
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		
		String currentUserId = proxy.getCurrentUser().getId();
		if (!isAdministrator(signupDao.getGroup(), currentUserId, false)) {
			throw new PermissionDeniedException(currentUserId);
		}
		
		String currentSupervisorId = signupDao.getSupervisorId();
		if (null == currentSupervisorId) {
			signupDao.setSupervisorId(supervisorId);
			dao.save(signupDao);
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public void setHideCourse(String courseId, boolean hideGroup) {
		UserProxy user = proxy.getCurrentUser();
		CourseGroupDAO courseGroupDao = dao.findCourseGroupById(courseId);
		if (!isAdministrator(courseGroupDao, user.getId(), false)) {
			throw new PermissionDeniedException(user.getId());
		}
		courseGroupDao.setHideGroup(hideGroup);
		dao.save(courseGroupDao);
		// Re-index. This should be re-factored so that all saves on the CourseGroup
		// cause a re-index to happen.
		searchService.addCourseGroup(new CourseGroupImpl(courseGroupDao, this));
		searchService.tidyUp();
	}
	
	/**
	 * 
	 */
	public List<CourseGroup> getAdministering() {
		String userId = proxy.getCurrentUser().getId();
		List <CourseGroupDAO> groupDaos;
		if (proxy.isAdministrator()) {
			groupDaos = dao.findAllGroups();
		} else {
			groupDaos = dao.findAdminCourseGroups(userId);
		}
		List<CourseGroup> groups = new ArrayList<CourseGroup>(groupDaos.size());
		for(CourseGroupDAO groupDao : groupDaos) {
			groups.add(new CourseGroupImpl(groupDao, this));
		}
		return groups;
	}
	
	/**
	 * Get list of course groups for which current user is a teacher
	 */
	public List<CourseGroup> getLecturing() {
		String userId = proxy.getCurrentUser().getId();
		List <CourseGroupDAO> groupDaos = dao.findLecturingCourseGroups(userId);
		List<CourseGroup> groups = new ArrayList<CourseGroup>(groupDaos.size());
		for(CourseGroupDAO groupDao : groupDaos) {
			groups.add(new CourseGroupImpl(groupDao, this));
		}
		return groups;
	}

	/**
	 * 
	 */
	public List<CourseSignup> getPendings() {
		String currentUser = proxy.getCurrentUser().getId();
		List <CourseSignupDAO> signupDaos = dao.findSignupPending(currentUser);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao : signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
	
	/**
	 * 
	 */
	public List<CourseSignup> getApprovals() {
		String currentUser = proxy.getCurrentUser().getId();
		List <CourseSignupDAO> signupDaos = dao.findSignupApproval(currentUser);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao : signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
	
	/**
	 * 
	 */
	public List<CourseSignup> getMySignups(Set<Status> statuses) {
		String userId = proxy.getCurrentUser().getId();
		if (log.isDebugEnabled()) {
			log.debug("Loading all signups for : "+ userId+ " of status "+ statuses);
		}
		List<CourseSignup> signups = new ArrayList<CourseSignup>();
		for (CourseSignupDAO signupDao:  dao.findSignupForUser(userId, (statuses==null)?Collections.<Status>emptySet():statuses)) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
	
	/**
	 * 
	 */
	public List<CourseSignup> getUserComponentSignups(String userId, Set<Status> statuses) {
		if (log.isDebugEnabled()) {
			log.debug("Loading all signups for : "+ userId+ " of status "+ statuses);
		}
		List<CourseSignup> signups = new ArrayList<CourseSignup>();
		for (CourseSignupDAO signupDao:  dao.findSignupForUser(userId, (statuses==null)?Collections.<Status>emptySet():statuses)) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}

	/**
	 * Get a course group with components. The components can be limited by specifying a range to load.
	 */
	public CourseGroup getCourseGroup(String courseId, Range range) {
		if (range == null) {
			range = Range.UPCOMING;
		}
		CourseGroupDAO courseGroupDao = dao.findCourseGroupById(courseId);
		CourseGroup courseGroup = null;
		if (courseGroupDao != null) {
			List<CourseComponentDAO> componentDAOs = dao.findCourseComponents(courseId, range, getNow());
			courseGroup = new CourseGroupImpl(courseGroupDao, this, componentDAOs);
		}
		return courseGroup;
	}

	/**
	 * 
	 */
	public List<CourseSignup> getCourseSignups(String courseId, Set<Status> statuses) {
		// Find all the components and then find all the signups.
		String userId = proxy.getCurrentUser().getId();
		
		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			return null;
		}
		if(!isAdministrator(groupDao, userId, false) && !isLecturer(groupDao, proxy.getCurrentUser(), false)) {
			throw new PermissionDeniedException(userId);
		}
		List<CourseSignupDAO> signupDaos = dao.findSignupByCourse(userId, courseId, statuses);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao: signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
	
	public Integer getCountCourseSignups(String courseId, Set<Status> statuses) {
		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			return null;
		}
		
		return dao.countSignupByCourse(courseId, statuses, getNow());
	}
	
	public String getDirectUrl(String courseId) {
		return proxy.getDirectUrl(courseId);
	}
	
	/**
	 * 
	 */
	public CourseComponent getCourseComponent(String componentId) {
		CourseComponentDAO componentDao = dao.findCourseComponent(componentId);
		return new CourseComponentImpl(componentDao);
	}
	
	public List<CourseComponent> getAllComponents() {
		List<CourseComponentDAO> componentDaos = dao.findAllComponents();
		List<CourseComponent> courseComponents = new ArrayList<CourseComponent>();
		for (CourseComponentDAO componentDao : componentDaos) {
			courseComponents.add(new CourseComponentImpl(componentDao));
		}
		return courseComponents;
	}
	
	/**
	 * 
	 */
	public List<CourseSignup> getComponentSignups(String componentId, Set<Status> statuses) 
	throws NotFoundException {
		return getComponentSignups(componentId, statuses, null);
	}
		
	public List<CourseSignup> getComponentSignups(String componentId, Set<Status> statuses, Integer year) 
	throws NotFoundException {
	
		CourseComponentDAO componentDao = dao.findCourseComponent(componentId);
		if (componentDao == null) {
			throw new NotFoundException(componentId);
		}
		UserProxy currentUser = proxy.getCurrentUser();
		if (!isAdministrator(componentDao) && !isLecturer(componentDao, currentUser)) {
			throw new PermissionDeniedException(currentUser.getId());
		}
		List<CourseSignupDAO> signupDaos = dao.findSignupByComponent(componentId, statuses, year);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao : signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}

	/**
	 * Check if the current user is a member of a set.
	 * @param userIds The set of user IDs to check
	 * @return <code>true</code> if the user is in the set.
	 */
	boolean containsCurrentUser(Set<String> userIds) {
		// This is used by the CourseGroup class
		String currentUserId = proxy.getCurrentUser().getId();
		return userIds.contains(currentUserId);
	}
	
	private boolean isAdministrator(CourseGroupDAO groupGroup, String currentUserId, boolean defaultValue) {
		if (groupGroup.getAdministrators().contains(currentUserId)) {
			return true;
		}
		UserProxy user = proxy.findUserById(currentUserId);
		if (user != null && user.getEid().equals(this.getDaisyAdmin())) {
			return true;
		}
		if (proxy.isAdministrator()) {
			return true;
		}
		if (groupGroup.getSuperusers().contains(currentUserId)) {
			return true;
		}
		return defaultValue;
	}
	
	private boolean isLecturer(CourseGroupDAO groupGroup, UserProxy user, boolean defaultValue) {
		for (CourseComponentDAO componentDao : groupGroup.getComponents()) {
			if (isLecturer(componentDao, user)) {
				return true;
			}
		}
		return defaultValue;
	}


	private boolean isAdministrator(CourseComponentDAO componentDao) {
		UserProxy user = proxy.getCurrentUser();
		if (null == user) {
			return false;
		}
		if (user.getEid().equals(this.getDaisyAdmin())) {
			return true;
		}
		if (proxy.isAdministrator()) {
			return true;
		}
		// Check all the CourseGroups
		for (CourseGroupDAO groupDao: componentDao.getGroups()) {
			if (isAdministrator(groupDao, user.getId(), false)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isLecturer(CourseComponentDAO componentDao, UserProxy user) {
		
		if (null == user) {
			return false;
		}
		if (user.getId().equals(componentDao.getTeacher())) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	public void reject(String signupId) {
		reject(signupId, false, proxy.getCurrentPlacementId());
	}
	public void reject(String signupId, boolean skipAuth, String placementId) {
		
		String currentUserId = proxy.getCurrentUser().getId();
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		CourseSignup signup = new CourseSignupImpl(signupDao, this);

		Status status = signupDao.getStatus();
		if (Status.PENDING.equals(status) || Status.WAITING.equals(status)) { // Rejected at pending or waiting.
			if (!skipAuth) {
				if (!isAdministrator(signupDao.getGroup(), currentUserId, false)) {
					throw new PermissionDeniedException(currentUserId);
				}
			}
			signupDao.setStatus(Status.REJECTED);
			signupDao.setAmended(getNow());
			dao.save(signupDao);
			proxy.logEvent(signupDao.getGroup().getCourseId(), EVENT_REJECT, placementId);
			sendMails(status, signupId, placementId, emailSendingService);

		} else if (Status.ACCEPTED.equals(status)) { // Rejected at supervisor stage
			if (!skipAuth) {
				if (!isAdministrator(signupDao.getGroup(), currentUserId, currentUserId.equals(signupDao.getSupervisorId()))) {
					throw new PermissionDeniedException(currentUserId);
				}
			}
			signupDao.setStatus(Status.REJECTED);
			signupDao.setAmended(getNow());
			dao.save(signupDao);
			for (CourseComponentDAO componentDao: signupDao.getComponents()) {
				componentDao.setTaken(componentDao.getTaken()-1);
				dao.save(componentDao);
			}
			proxy.logEvent(signupDao.getGroup().getCourseId(), EVENT_REJECT, placementId);
			sendMails(Status.ACCEPTED, signupId, placementId, emailSendingService);
		} else if (Status.APPROVED.equals(status)) {// Rejected at approver stage
			if (!skipAuth) {
				boolean isApprover = dao.findDepartmentApprovers(signupDao.getDepartment()).contains(currentUserId);
				if (!isAdministrator(signupDao.getGroup(), currentUserId, isApprover)) {
					throw new PermissionDeniedException(currentUserId);
				}
			}
			signupDao.setStatus(Status.REJECTED);
			signupDao.setAmended(getNow());
			dao.save(signupDao);
			for (CourseComponentDAO componentDao: signupDao.getComponents()) {
				componentDao.setTaken(componentDao.getTaken()-1);
				dao.save(componentDao);
			}
			proxy.logEvent(signupDao.getGroup().getCourseId(), EVENT_REJECT, placementId);
			sendMails(Status.ACCEPTED, signupId, placementId, emailSendingService);
		} else {
			throw new IllegalStateException("You can only reject signups that are WAITING, PENDING, ACCEPTED or APPROVED");
		}
	}

	/**
	 * 
	 */
	public void setSignupStatus(String signupId, Status newStatus) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		String currentUserId = proxy.getCurrentUser().getId();
		if (isAdministrator(signupDao.getGroup(), currentUserId, false)) {
			Status currentStatus = signupDao.getStatus();
			if (!currentStatus.equals(newStatus)) { // Check it actually needs changing.
				signupDao.setStatus(newStatus);
				signupDao.setAmended(getNow());
				dao.save(signupDao);
				int spaceAdjustment = (-currentStatus.getSpacesTaken()) + newStatus.getSpacesTaken();
				if (spaceAdjustment != 0) {
					for(CourseComponentDAO component: signupDao.getComponents()) {
						component.setTaken(component.getTaken()+spaceAdjustment);
						dao.save(component);
					}
				}
			}
		} else {
			throw new PermissionDeniedException(currentUserId);
		}
	}

	/**
	 * @inheritDoc
	 */
	public CourseSignup signup(String userId, String userName, String userEmail, String courseId, Set<String> componentIds, String supervisorId) {
		
		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			throw new NotFoundException(courseId);
		}
		
		Set<CourseComponentDAO> componentDaos = loadComponents(componentIds, groupDao);

		// Check the current user is the administrator for all the components.
		String currentUserId = proxy.getCurrentUser().getId();
		if (!isAdministrator(groupDao, currentUserId, false)) {
			throw new PermissionDeniedException(currentUserId);
		}
		
		// Create the signup.
		UserProxy user = null;
		if (userId == null) {
			// Check for the user by email.
			if (userEmail == null) {
				throw new IllegalArgumentException("If you don't supply a userId you must supply an email address");
			}
			user = proxy.findUserByEmail(userEmail);
			if (user == null) {
				user = proxy.newUser(userName, userEmail);
			}
		} else {
			user = proxy.findUserById(userId);
		}
		CourseSignupDAO signupDao = dao.newSignup(user.getId(), supervisorId, getNow());
		signupDao.setGroup(groupDao);
		signupDao.setStatus(Status.PENDING);
		signupDao.setAmended(getNow());
		Department department = findPracDepartment(user.getPrimaryOrgUnit());
		if (null != department) {
			signupDao.setDepartment(department.getPracCode());
		}
		String signupId = dao.save(signupDao);
		
		for (CourseComponentDAO componentDao: componentDaos) {
			List<CourseSignupDAO> signupsToRemove = new ArrayList<CourseSignupDAO>();
			for(CourseSignupDAO componentSignupDao: componentDao.getSignups()) {
				if (componentSignupDao.getUserId().equals(user.getId())) {
					signupsToRemove.add(componentSignupDao);
				}
			}
			Set <CourseSignupDAO> signupDaos = componentDao.getSignups();
			for (CourseSignupDAO removeSignup: signupsToRemove) {
				// If they had already been accepted then decrement the taken count.
				if (removeSignup.getStatus().equals(Status.APPROVED) || 
					removeSignup.getStatus().equals(Status.ACCEPTED) || 
					removeSignup.getStatus().equals(Status.CONFIRMED)) {
					for (CourseComponentDAO signupComponentDao : removeSignup.getComponents()) {
						signupComponentDao.setTaken(signupComponentDao.getTaken()-1);
						signupComponentDao.getSignups().remove(removeSignup);  
						dao.save(signupComponentDao);
					}
				}
				signupDaos.remove(removeSignup);
				dao.remove(removeSignup);
			}
			
			componentDao.getSignups().add(signupDao);
			componentDao.setTaken(componentDao.getTaken()+1);
			dao.save(componentDao);
		}
		
		accept(signupId, false, null);
		return new CourseSignupImpl(signupDao, this);
	}

	/**
	 * Signup by Student
	 */
	public CourseSignup signup(String courseId, Set<String> componentIds, String supervisorEmail,
			String message) throws IllegalStateException, IllegalArgumentException {

		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			throw new NotFoundException(courseId);
		}
		
		boolean full = false;
		Set<Status> statuses = Collections.singleton(Status.WAITING);
		if (dao.countSignupByCourse(courseId, statuses, getNow()).intValue() > 0) {
			full = true;
		}
		
		Set<CourseComponentDAO> componentDaos = loadComponents(componentIds, groupDao);
		
		// Check they are valid as a choice (in signup period (student), not for same component in same term)
		UserProxy user = proxy.getCurrentUser();
		Date now = getNow();
		List<CourseSignupDAO> existingSignups = new ArrayList<CourseSignupDAO>();
		for(CourseComponentDAO componentDao: componentDaos) {
			if(componentDao.getOpens().after(now) || componentDao.getCloses().before(now)) {
				throw new IllegalStateException("Component isn't currently open: "+ componentDao.getPresentationId());
			}
			if ( (componentDao.getSize()-componentDao.getTaken()) < 1) {
				full = true;
			}
			for (CourseSignupDAO signupDao: componentDao.getSignups()) {
				// Look for exisiting signups for these components
				if (user.getId().equals(signupDao.getUserId())) {
					existingSignups.add(signupDao);
					if(!signupDao.getStatus().equals(Status.WITHDRAWN)) {
						throw new IllegalStateException(
								"User "+ user.getId()+ " already has a place on component: "+ componentDao.getPresentationId());
					}
				}
			}
		}
		
		// Remove all traces of the existing signup.
		for (CourseSignupDAO existingSignup :existingSignups) {
			for (CourseComponentDAO componentDao: existingSignup.getComponents()) {
				componentDao.getSignups().remove(existingSignup);
				dao.save(componentDao);
			}
			dao.remove(existingSignup);
		}
		// Set the supervisor
		String supervisorId = null;
		if (null != supervisorEmail) {
			UserProxy supervisor = proxy.findUserByEmail(supervisorEmail);
			if (supervisor == null) {
				if (groupDao.getSupervisorApproval()) {
					throw new IllegalArgumentException(
						"Can't find a supervisor with email: "+ supervisorEmail);
				}
			} else {
				supervisorId = supervisor.getId();
			}
		}
		
		// Create the signup.
		CourseSignupDAO signupDao = dao.newSignup(user.getId(), supervisorId, getNow());
		signupDao.setGroup(groupDao);
		if (full) {
			signupDao.setStatus(Status.WAITING);
		} else {
			signupDao.setStatus(Status.PENDING);
		}
		signupDao.setAmended(getNow());
		signupDao.setMessage(message);
		Department department = findPracDepartment(user.getPrimaryOrgUnit());
		if (null != department) {
			signupDao.setDepartment(department.getPracCode());
		} else {
			signupDao.setDepartment(null);
		}
		String signupId = dao.save(signupDao);
		
		// We're going to decrement the places on acceptance.
		for (CourseComponentDAO componentDao: componentDaos) {
			componentDao.getSignups().add(signupDao);
			signupDao.getComponents().add(componentDao); // So when sending out email we know the components.
			dao.save(componentDao);
		}
		proxy.logEvent(groupDao.getCourseId(), EVENT_SIGNUP, null);

		// Now send emails.
		// Old status, new status, signup ID.
		String placementId = proxy.getCurrentPlacementId();
		CourseSignup signup = new CourseSignupImpl(signupDao, this);

		if (full || groupDao.getAdministratorApproval()) {
			sendMails(null, signupId, placementId, emailSendingService);
		} else {
			accept(signupId, false, null);
		}
		
		return new CourseSignupImpl(signupDao, this);
	}
	
	/**
	 * Find all the components and check they are associated with the supplied course group.
	 * @param componentIds The component IDs to load.
	 * @param groupDao The course group to check they belong to.
	 * @return The loaded components.
	 * @throws IllegalArgumentException If there aren't any component IDs or they don't belong to the group.
	 * @throws NotFoundException If one of the component IDs couldn't be found.
	 */
	private Set<CourseComponentDAO> loadComponents(Set<String> componentIds, CourseGroupDAO groupDao) {
		if (componentIds == null) {
			throw new IllegalArgumentException("You must specify some components to signup to.");
		}
	
		Set<CourseComponentDAO> componentDaos = new HashSet<CourseComponentDAO>(componentIds.size());
		for(String componentId: componentIds) {
			CourseComponentDAO componentDao = dao.findCourseComponent(componentId);
			if (componentDao != null) {
				componentDaos.add(componentDao);
				if (!componentDao.getGroups().contains(groupDao)) { // Check that the component is actually part of the set.
					throw new IllegalArgumentException("The component: "+ componentId+ " is not part of the course: "+ groupDao.getCourseId());
				}
			} else {
				throw new NotFoundException(componentId);
			}
		}
		return componentDaos;
	}



	public void withdraw(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		Status oldStatus = signupDao.getStatus();
		if (Status.ACCEPTED.equals(signupDao.getStatus()) || 
			Status.APPROVED.equals(signupDao.getStatus()) ||
			Status.CONFIRMED.equals(signupDao.getStatus())) {

			for (CourseComponentDAO componentDao: signupDao.getComponents()) {
				componentDao.setTaken(componentDao.getTaken()-1);
				dao.save(componentDao);
			}
		}
		signupDao.setStatus(Status.WITHDRAWN);
		signupDao.setAmended(getNow());
		dao.save(signupDao);
		proxy.logEvent(signupDao.getGroup().getCourseId(), EVENT_WITHDRAW, null);
		String placementId =  proxy.getCurrentPlacementId();

		sendMails(oldStatus, signupId, placementId, emailSendingService);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String split(String signupId, Set<String> componentPresentationIds) {
		// Check we can find the signup.
		CourseSignupDAO signupDAO = dao.findSignupById(signupId);
		if (signupDAO == null) {
			throw new NotFoundException(signupId);
		}
		String userId = proxy.getCurrentUser().getId();
		if (!isAdministrator(signupDAO.getGroup(), userId, false)) {
			throw new PermissionDeniedException(userId);
		}
		if (componentPresentationIds == null || componentPresentationIds.isEmpty()) {
			throw new IllegalArgumentException("You must specify some componentPresentationIds.");
		}
		if (componentPresentationIds.size() >= signupDAO.getComponents().size()) {
			throw new IllegalArgumentException("You can't specify all the componentPresentationIds in the signup.");
		}

		// This won't affect the take counts as we keep the same number of components and the new signup is in the
		// same status as the existing one.
		CourseSignupDAO newSignup = dao.newSignup(signupDAO.getUserId(), signupDAO.getSupervisorId(), getNow());
		newSignup.setStatus(signupDAO.getStatus());
		newSignup.setGroup(signupDAO.getGroup());
		newSignup.setDepartment(signupDAO.getDepartment());
		newSignup.setMessage(signupDAO.getMessage());
		dao.save(newSignup);

		// Now move the components to the new signup.
		Set<CourseComponentDAO> newSignupComponents = new HashSet<CourseComponentDAO>();
		// We don't modify arguments.
		Set<String> remainingComponentIds = new HashSet<String>(componentPresentationIds);
		for (CourseComponentDAO componentDAO: signupDAO.getComponents()) {
			if (remainingComponentIds.remove(componentDAO.getPresentationId())) {
				newSignupComponents.add(componentDAO);
				componentDAO.getSignups().remove(signupDAO);
				componentDAO.getSignups().add(newSignup);
				dao.save(componentDAO);
			}
		}

		// Check we removed them all, transactions will clear up the mess.
		if (!remainingComponentIds.isEmpty()) {
			throw new IllegalArgumentException("Some components weren't part of the signup: "+ componentPresentationIds);
		}

		return newSignup.getId();
	}


	public List<CourseGroup> getCourseGroupsByDept(String deptId, Range range, boolean externalUser) {
		List<CourseGroupDAO> cgDaos = dao.findCourseGroupByDept(deptId, range, getNow(), externalUser);
		List<CourseGroup> cgs = new ArrayList<CourseGroup>(cgDaos.size());
		for (CourseGroupDAO cgDao: cgDaos) {
			cgs.add(new CourseGroupImpl(cgDao, this));
		}
		return cgs;
	}
	
	public List<CourseGroup> getCourseGroupsBySubUnit(String subunitId, Range range, boolean externalUser) {
		List<CourseGroupDAO> cgDaos = dao.findCourseGroupBySubUnit(subunitId, range, getNow(), externalUser);
		List<CourseGroup> cgs = new ArrayList<CourseGroup>(cgDaos.size());
		for (CourseGroupDAO cgDao: cgDaos) {
			cgs.add(new CourseGroupImpl(cgDao, this));
		}
		return cgs;
	}
	
	public List<CourseGroup> getCourseGroupsByComponent(String componentId) {
		List<CourseGroupDAO> cgDaos = dao.findCourseGroupByComponent(componentId);
		List<CourseGroup> cgs = new ArrayList<CourseGroup>(cgDaos.size());
		for (CourseGroupDAO cgDao: cgDaos) {
			cgs.add(new CourseGroupImpl(cgDao, this));
		}
		return cgs;
	}
	
	public List<SubUnit> getSubUnitsByDept(String deptId) {
		List<Object[]> subNodes = dao.findSubUnitByDept(deptId);
		List<SubUnit> subUnits = new ArrayList<SubUnit>(subNodes.size());
		for (Object[] subNode : subNodes) {
			subUnits.add(new SubUnitImpl((String) subNode[0], (String) subNode[1]));
		}
		return subUnits;
	}
	
	public Map<String, String> getDepartments() {
		List<CourseDepartmentDAO> subNodes = dao.findAllDepartments();
		Map<String, String> departments = new HashMap<String, String>();
		for (CourseDepartmentDAO departmentDAO : subNodes) {
			departments.put(departmentDAO.getCode(), departmentDAO.getName());
		}
		return departments;
	}

	public Date getNow() {
		return now.getNow();
	}
	
	/**
	 * Loads details about a user without additionalUserDetails.
	 * @return
	 */
	UserProxy loadUser(String id) {
		return proxy.findUserById(id);
	}
	
	/**
	 * Loads details about a user with additionalUserDetails.
	 * @return
	 */
	UserProxy loadStudent(String id) {
		return proxy.findStudentById(id);
	}
	
	public Department findPracDepartment(String primaryOrgUnit) {
		CourseDepartmentDAO department = dao.findDepartmentByPrimaryOrgUnit(primaryOrgUnit);
		if (null == department) {
			return null;
		}
		return new DepartmentImpl(department);
	}
	
	/**
	 * 
	 */
	public boolean isDepartmentCode(String code) {
		return dao.findDepartmentByCode(code) != null;
	}

	public List<CourseGroup> search(String search, Range range, boolean external) {
		String words[] = search.split(" ");
		List<CourseGroupDAO> groupDaos = dao.findCourseGroupByWords(words, range, getNow(), external);
		List<CourseGroup> groups = new ArrayList<CourseGroup>(groupDaos.size());
		for(CourseGroupDAO groupDao: groupDaos) {
			groups.add(new CourseGroupImpl(groupDao, this));
		}
		return groups;
	}
	
	public CourseSignup getCourseSignup(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			return null;
		}
		
		String currentUserId = proxy.getCurrentUser().getId();
		if (
				currentUserId.equals(signupDao.getUserId()) ||
				currentUserId.equals(signupDao.getSupervisorId()) ||
				isAdministrator(signupDao.getGroup(), currentUserId, false)
			) {
			return new CourseSignupImpl(signupDao, this);
		} else {
			throw new PermissionDeniedException(currentUserId);
		}
	}
	
	public CourseSignup getCourseSignupAnyway(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			return null;
		} else {
			return new CourseSignupImpl(signupDao, this);
		}
	}

	
	public String[] getCourseSignupFromEncrypted(String encrypted) {
		
		String string = proxy.uncode(encrypted);
		if (null == string){
			throw new IllegalStateException("the encryptied string cannot be decyphered");
		}
		return string.split("\\$");
	}
	
	/**
	 * 
	 */
	public List<CourseGroup> getCourseCalendar(boolean external, String providerId) {
		String userId = proxy.getCurrentUser().getId();
		List <CourseComponentDAO> componentDaos = dao.findCourseGroupsByCalendar(external, providerId);
		List<CourseGroup> groups = new ArrayList<CourseGroup>();
		for (CourseComponentDAO componentDao : componentDaos) {
			for (CourseGroupDAO groupDao : componentDao.getGroups()) {
				CourseGroupDAO myGroupDao = (CourseGroupDAO)groupDao.clone();
				myGroupDao.setComponents(Collections.singleton(componentDao));
				groups.add(new CourseGroupImpl(myGroupDao, this));
			}
		}
		return groups;
	}
	
	/**
	 * 
	 */
	public List<CourseGroup> getCourseNoDates(boolean external, String providerId) {
		String userId = proxy.getCurrentUser().getId();
		List <CourseComponentDAO> componentDaos = dao.findCourseGroupsByNoDates(external, providerId);
		List<CourseGroup> groups = new ArrayList<CourseGroup>();
		for (CourseComponentDAO componentDao : componentDaos) {
			for (CourseGroupDAO groupDao : componentDao.getGroups()) {
				CourseGroupDAO myGroupDao = (CourseGroupDAO)groupDao.clone();
				myGroupDao.setComponents(Collections.singleton(componentDao));
				groups.add(new CourseGroupImpl(myGroupDao, this));
			}
		}

		Collections.sort(groups, noDateCompatator);
		
		return groups;
	}
	
	/**
	 * @return This returns the EID of the Daisy administrator.
	 */
	protected String getDaisyAdmin() {
		return proxy.getConfigParam("daisy.administrator", "admin");
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getRecentDays() {
		return proxy.getConfigParam("recent.days", 14);
	}

	@Override
	public CourseDepartmentImpl findDepartmentByCode(String departmentCode) {
		CourseDepartmentDAO departmentDao = dao.findDepartmentByCode(departmentCode);
		if (departmentDao != null) {
			return new CourseDepartmentImpl(departmentDao.getCode(), departmentDao.getApprove(), departmentDao.getApprovers());
		}
		return null;
	}

	/**
     * This should get called whenever a signup is changed.
	 * @param current The current signup status.
     * @param old The original signup status, can be <code>null</code>.
	 * @param signupId The ID of the signup.
	 * @param placementId The current placement.
	 * @param emailSendingService
	 */
	public void sendMails(Status old, String signupId, String placementId, EmailSendingService emailSendingService) {
        CourseSignup signup = getCourseSignupAnyway(signupId);
        CourseDepartment department = null;
        if (null != signup.getDepartment()) {
            department = findDepartmentByCode(signup.getDepartment());
        }
        StateChange stateChange = new StateChange(old, signup, department, placementId);
        emailSendingService.applyRules(stateChange);
    }
}
