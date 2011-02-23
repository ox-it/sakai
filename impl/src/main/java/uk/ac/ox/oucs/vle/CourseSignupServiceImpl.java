package uk.ac.ox.oucs.vle;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;


public class CourseSignupServiceImpl implements CourseSignupService {
	
	private final static Log log = LogFactory.getLog(CourseSignupServiceImpl.class);
	
	private final static ResourceLoader rb = new ResourceLoader("messages");

	private CourseDAO dao;
	private SakaiProxy proxy;

	private Date now;

	private long adjustment;
	
	public void setDao(CourseDAO dao) {
		this.dao = dao;
	}
	
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	public void approve(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		CourseGroupDAO groupDao = signupDao.getGroup();
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
		signupDao.setStatus(Status.APPROVED);
		dao.save(signupDao);
		proxy.logEvent(groupDao.getId(), EVENT_SIGNUP);
		String url = proxy.getMyUrl();
		sendSignupEmail(signupDao.getUserId(), signupDao, "approved.student.subject","approved.student.body", new Object[]{url});
	}
	
	public void accept(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		String currentUserId = proxy.getCurrentUser().getId();
		boolean canAccept = false;
		// If is course admin on one of the components.
		canAccept = isAdministrator(signupDao.getGroup(), currentUserId, canAccept);
		if (!canAccept) {
			throw new PermissionDeniedException(currentUserId);
		}
		if (!Status.PENDING.equals(signupDao.getStatus())) {
			throw new IllegalStateException("You can only accept signups that are pending.");
		}
		for (CourseComponentDAO componentDao : signupDao.getComponents()) {
			componentDao.setTaken(componentDao.getTaken()+1);
			dao.save(componentDao);
		}
		signupDao.setStatus(Status.ACCEPTED);
		dao.save(signupDao);
		proxy.logEvent(signupDao.getGroup().getId(), EVENT_ACCEPT);
		
		String supervisorId = signupDao.getSupervisorId();
		String url = proxy.getConfirmUrl(signupId);
		if (supervisorId != null) {
			sendSignupEmail(supervisorId, signupDao, "approval.supervisor.subject", "approval.supervisor.body", new Object[]{url});
		}
	}

	public String findSupervisor(String search) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CourseGroup> getAdministering() {
		String userId = proxy.getCurrentUser().getId();
		List <CourseGroupDAO> groupDaos = dao.findAdminCourseGroups(userId);
		List<CourseGroup> groups = new ArrayList<CourseGroup>(groupDaos.size());
		for(CourseGroupDAO groupDao : groupDaos) {
			groups.add(new CourseGroupImpl(groupDao, this));
		}
		return groups;
	}

	public List<CourseSignup> getApprovals() {
		String currentUser = proxy.getCurrentUser().getId();
		List <CourseSignupDAO> signupDaos = dao.findSignupPending(currentUser);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao : signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
	
	public List<CourseSignup> getMySignups(Set<Status> statuses) {
		String userId = proxy.getCurrentUser().getId();
		if (log.isDebugEnabled()) {
			log.debug("Loading all signups for : "+ userId+ " of status "+ statuses);
		}
		List<CourseSignup> signups = new ArrayList<CourseSignup>();
		for (CourseSignupDAO signupDao:  dao.findSignupForUser(userId, (statuses==null)?Collections.EMPTY_SET:statuses)) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
	
	public List<CourseSignup> getUserComponentSignups(String userId, Set<Status> statuses) {
		if (log.isDebugEnabled()) {
			log.debug("Loading all signups for : "+ userId+ " of status "+ statuses);
		}
		List<CourseSignup> signups = new ArrayList<CourseSignup>();
		for (CourseSignupDAO signupDao:  dao.findSignupForUser(userId, (statuses==null)?Collections.EMPTY_SET:statuses)) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}

	public CourseGroup getCourseGroup(String courseId, Range range) {
		if (range == null) {
			range = Range.UPCOMING;
		}
		CourseGroupDAO courseGroupDao = dao.findCourseGroupById(courseId, range, getNow());
		CourseGroup courseGroup = null;
		if (courseGroupDao != null) {
			courseGroup = new CourseGroupImpl(courseGroupDao, this);
		}
		return courseGroup;
	}

	public List<CourseSignup> getCourseSignups(String courseId) {
		// Find all the components and then find all the signups.
		String userId = proxy.getCurrentUser().getId();
		
		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			return null;
		}
		if(!isAdministrator(groupDao, userId, false)) {
			throw new PermissionDeniedException(userId);
		}
		
		List<CourseSignupDAO> signupDaos = dao.findSignupByCourse(userId, courseId);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao: signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
	
	public List<CourseSignup> getComponentSignups(String componentId) {
		CourseComponentDAO componentDao = dao.findCourseComponent(componentId);
		if (componentDao == null) {
			throw new NotFoundException(componentId);
		}
		String currentUserId = proxy.getCurrentUser().getId();
		if (!isAdministrator(componentDao, currentUserId, false)) {
			throw new PermissionDeniedException(currentUserId);
		}
		List<CourseSignupDAO> signupDaos = dao.findSignupByComponent(componentId);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao : signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}
		

	private boolean isAdministrator(CourseGroupDAO groupGroup, String currentUserId, boolean defaultValue) {
		boolean isAdmin = defaultValue;
		if(groupGroup.getAdministrator().equals(currentUserId)) {
				isAdmin = true;
		}
		return isAdmin;
	}

	private boolean isAdministrator(CourseComponentDAO componentDao,
			String userId, boolean defaultValue) {
		for (CourseGroupDAO groupDao: componentDao.getGroups()) {
			if (groupDao.getAdministrator().equals(userId)) {
				return true;
			}
		}
		return false;
	}

	public void reject(String signupId) {
		String currentUserId = proxy.getCurrentUser().getId();
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}

		if (Status.PENDING.equals(signupDao.getStatus())) { // Rejected by administrator.
			if (isAdministrator(signupDao.getGroup(), currentUserId, false)) {
				signupDao.setStatus(Status.REJECTED);
				dao.save(signupDao);
				proxy.logEvent(signupDao.getGroup().getId(), EVENT_REJECT);
				sendSignupEmail(signupDao.getUserId(), signupDao, "reject-admin.student.subject", "reject-admin.student.body", new Object[] {proxy.getCurrentUser().getName(), proxy.getMyUrl()});
			} else {
				throw new PermissionDeniedException(currentUserId);
			}
		} else if (Status.ACCEPTED.equals(signupDao.getStatus())) {// Rejected by lecturer.
			if (isAdministrator(signupDao.getGroup(), currentUserId, currentUserId.equals(signupDao.getSupervisorId()))) {
				signupDao.setStatus(Status.REJECTED);
				dao.save(signupDao);
				for (CourseComponentDAO componentDao: signupDao.getComponents()) {
					componentDao.setTaken(componentDao.getTaken()-1);
					dao.save(componentDao);
				}
				proxy.logEvent(signupDao.getGroup().getId(), EVENT_REJECT);
				sendSignupEmail(signupDao.getUserId(), signupDao, "reject-supervisor.student.subject", "reject-supervisor.student.body", new Object[] {proxy.getCurrentUser().getName(), proxy.getMyUrl()});
			} else {
				throw new PermissionDeniedException(currentUserId);
			}
		} else {
			throw new IllegalStateException("You can only reject signups that are PENDING or ACCEPTED");
		}

	}

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
	
	public void signup(String userId, String courseId, Set<String> componentIds) {
		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			throw new NotFoundException(courseId);
		}
		// Need to find all the components.
		if (componentIds == null) {
			throw new IllegalArgumentException("You must specify some components to signup to.");
		}
		Set<CourseComponentDAO> componentDaos = new HashSet<CourseComponentDAO>(componentIds.size());
		for(String componentId: componentIds) {
			CourseComponentDAO componentDao = dao.findCourseComponent(componentId);
			if (componentDao != null) {
				componentDaos.add(componentDao);
				if (!componentDao.getGroups().contains(groupDao)) { // Check that the component is actually part of the set.
					throw new IllegalArgumentException("The component: "+ componentId+ " is not part of the course: "+ courseId);
				}
			} else {
				throw new NotFoundException(componentId);
			}
		}
		String currentUserId = proxy.getCurrentUser().getId();
		if (!isAdministrator(groupDao, currentUserId, false)) {
			throw new PermissionDeniedException(currentUserId);
		}
		CourseSignupDAO signupDao = dao.newSignup(userId, null);
		signupDao.setCreated(getNow());
		signupDao.setGroup(groupDao);
		signupDao.setStatus(Status.ACCEPTED);
		dao.save(signupDao);
		
		for (CourseComponentDAO componentDao: componentDaos) {
			List<CourseSignupDAO> signupsToRemove = new ArrayList<CourseSignupDAO>();
			for(CourseSignupDAO componentSignupDao: componentDao.getSignups()) {
				if (componentSignupDao.getUserId().equals(userId)) {
					signupsToRemove.add(componentSignupDao);
				}
			}
			Set <CourseSignupDAO> signupDaos = componentDao.getSignups();
			for (CourseSignupDAO removeSignup: signupsToRemove) {
				// If they had already been accepted then decrement the taken count.
				if (removeSignup.getStatus().equals(Status.APPROVED) || removeSignup.getStatus().equals(Status.ACCEPTED)) {
					for (CourseComponentDAO signupComponentDao : removeSignup.getComponents()) {
						signupComponentDao.setTaken(signupComponentDao.getTaken()-1);
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
		proxy.logEvent(signupDao.getGroup().getId(), EVENT_ACCEPT);
	}

	public void signup(String courseId, Set<String> componentIds, String supervisorEmail,
			String message){

		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			throw new NotFoundException(courseId);
		}
		
		// Need to find all the components.
		Set<CourseComponentDAO> componentDaos = new HashSet<CourseComponentDAO>(componentIds.size());
		for(String componentId: componentIds) {
			CourseComponentDAO componentDao = dao.findCourseComponent(componentId);
			if (componentDao != null) {
				componentDaos.add(componentDao);
				if (!componentDao.getGroups().contains(groupDao)) { // Check that the component is actually part of the set.
					throw new IllegalArgumentException("The component: "+ componentId+ " is not part of the course: "+ courseId);
				}
			} else {
				throw new NotFoundException(componentId);
			}
		}
		
		
		// Check they are valid as a choice (in signup period (student), not for same component in same term)
		Date now = getNow();
		String userId = proxy.getCurrentUser().getId();
		
		List<CourseSignupDAO> existingSignups = new ArrayList<CourseSignupDAO>();
		for(CourseComponentDAO componentDao: componentDaos) {
			if(componentDao.getOpens().after(now) || componentDao.getCloses().before(now)) {
				throw new IllegalStateException("Component isn't currently open: "+ componentDao.getId());
			}
			if ( (componentDao.getSize()-componentDao.getTaken()) < 1) {
				throw new IllegalStateException("No places left on: "+ componentDao.getId());
			}
			for (CourseSignupDAO signupDao: componentDao.getSignups()) {
				// Look for exisiting signups for these components
				if ( userId.equals(signupDao.getUserId())) {
					existingSignups.add(signupDao);
					if(!signupDao.getStatus().equals(Status.WITHDRAWN)) {
						throw new IllegalStateException("User "+ userId+ " already has a place on component: "+ componentDao.getId());
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
		UserProxy supervisor = proxy.findUserByEmail(supervisorEmail);
		if (supervisor == null) {
			throw new IllegalArgumentException("Can't find a supervisor with email: "+ supervisorEmail);
		}
		
		// Create the signup.
		String supervisorId = supervisor.getId();
		CourseSignupDAO signupDao = dao.newSignup(userId, supervisorId);
		signupDao.setCreated(getNow());
		signupDao.setGroup(groupDao);
		signupDao.setStatus(Status.PENDING);
		signupDao.setMessage(message);
		String signupId = dao.save(signupDao);
		
		// We're going to decrement the places on acceptance.
		for (CourseComponentDAO componentDao: componentDaos) {
			//componentDao.getSignups().add(signupDao); // Link to the signup
			//componentDao.setTaken(componentDao.getTaken()+1); // Increment places taken
			componentDao.getSignups().add(signupDao);
			signupDao.getComponents().add(componentDao); // So when sending out email we know the components.
			dao.save(componentDao);
		}
		proxy.logEvent(groupDao.getId(), EVENT_SIGNUP);
		
		String adminId = groupDao.getAdministrator();
		String url = proxy.getConfirmUrl(signupId);
		if (adminId != null) {
			sendSignupEmail(adminId, signupDao, "signup.admin.subject", "signup.admin.body", new Object[]{url});
		} else {
			log.warn("Failed to send email as no administrator set for: "+ groupDao.getId());
		}
	}
	
	/**
	 * Generic method for sending out a signup email.
	 * @param userId The ID of the user who the message should be sent to.
	 * @param signupDao The signup the message is about.
	 * @param subjectKey The resource bundle key for the subject
	 * @param bodyKey The resource bundle key for the body.
	 * @param additionalBodyData Additional objects used to format the email body. Typically used for the confirm URL.
	 */
	public void sendSignupEmail(String userId, CourseSignupDAO signupDao, String subjectKey, String bodyKey, Object[] additionalBodyData) {
		UserProxy recepient = proxy.findUserById(userId);
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ userId);
			return;
		}
		UserProxy signupUser = proxy.findUserById(signupDao.getUserId());
		if (signupUser == null) {
			log.warn("Failed to find the user who made the signup: "+ signupDao.getUserId());
			return;
		}
		
		String to = recepient.getEmail();
		String subject = MessageFormat.format(rb.getString(subjectKey), new Object[]{proxy.getCurrentUser().getName(), signupDao.getGroup().getTitle(), signupUser.getName()});
		String componentDetails = formatSignup(signupDao);
		Object[] baseBodyData = new Object[] {
				proxy.getCurrentUser().getName(),
				componentDetails,
				signupDao.getGroup().getTitle(),
				signupUser.getName()
		};
		Object[] bodyData = baseBodyData;
		if (additionalBodyData != null) {
			bodyData = new Object[bodyData.length + additionalBodyData.length];
			System.arraycopy(baseBodyData, 0, bodyData, 0, baseBodyData.length);
			System.arraycopy(additionalBodyData, 0, bodyData, baseBodyData.length, additionalBodyData.length);
		}
		String body = MessageFormat.format(rb.getString(bodyKey), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	
	// Computer-Aided Formal Verification (Computing Laboratory)
	// - Lectures: 16 lectures for 16 sessions starts in Michaelmas 2010 with Daniel Kroening
	
	/**
	 * This formats the details of a signup into plain text.
	 * 
	 * @param signupDao
	 * @return
	 */
	public String formatSignup(CourseSignupDAO signupDao) {
		CourseSignup signup = new CourseSignupImpl(signupDao, this); // wrap is up to make it easier to handle.
		StringBuilder output = new StringBuilder(); // TODO Maybe should use resource bundle.
		output.append(signup.getGroup().getTitle());
		output.append(" (");
		output.append(signup.getGroup().getDepartment());
		output.append(" )\n");
		for(CourseComponent component: signup.getComponents()) {
			output.append("  - ");
			output.append(component.getTitle());
			output.append(": ");
			output.append(component.getSlot());
			output.append(" for ");
			output.append(component.getSessions());
			output.append(" starts in ");
			output.append(component.getWhen());
			Person presenter = component.getPresenter();
			if(presenter != null) {
				output.append(" with ");
				output.append(presenter.getName());
			}
			output.append("\n");
		}
		return output.toString();
	}

	public void withdraw(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new NotFoundException(signupId);
		}
		if (!Status.PENDING.equals(signupDao.getStatus())) {
			throw new IllegalStateException("Can only withdraw from pending signups: "+ signupId);
		}
		signupDao.setStatus(Status.WITHDRAWN);
		dao.save(signupDao);
		proxy.logEvent(signupDao.getGroup().getId(), EVENT_WITHDRAW);
	}

	public CourseGroup getAvailableCourseGroup(String courseId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CourseGroup> getCourseGroups(String deptId, Range range) {
		List<CourseGroupDAO> cgDaos = dao.findCourseGroupByDept(deptId, range, getNow());
		List<CourseGroup> cgs = new ArrayList<CourseGroup>(cgDaos.size());
		for (CourseGroupDAO cgDao: cgDaos) {
			cgs.add(new CourseGroupImpl(cgDao, this));
		}
		return cgs;
	}

	public Date getNow() {
		return (adjustment != 0)?new Date(new Date().getTime() + adjustment):new Date();
	}
	
	public void setNow(Date newNow) {
		adjustment = newNow.getTime() - new Date().getTime();
	}

	/**
	 * Loads details about a user.
	 * @return
	 */
	UserProxy loadUser(String id) {
		return proxy.findUserById(id);
	}

	public List<CourseGroup> search(String search) {
		String words[] = search.split(" ");
		List<CourseGroupDAO> groupDaos = dao.findCourseGroupByWords(words, Range.UPCOMING, getNow());
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

}
