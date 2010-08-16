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

import uk.ac.ox.oucs.vle.proxy.SakaiProxy;
import uk.ac.ox.oucs.vle.proxy.UserProxy;

public class CourseSignupServiceImpl implements CourseSignupService {
	
	private final static Log log = LogFactory.getLog(CourseSignupServiceImpl.class);
	
	private final static ResourceBundle rb = ResourceBundle.getBundle("messages");

	private CourseDAO dao;
	private SakaiProxy proxy;

	private Date now;
	
	public void setDao(CourseDAO dao) {
		this.dao = dao;
	}
	
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	public void approve(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		String currentUserId = proxy.getCurrentUser().getId();
		boolean canApprove = false;
		if (currentUserId.equals(signupDao.getSupervisorId())) {
			canApprove = true;
		} else {
			canApprove = isAdministrator(signupDao.getGroup(), currentUserId, canApprove);
		}
		if (!canApprove) {
			throw new IllegalStateException("You are not allowed to approve this signup: "+ signupId);
		}
		signupDao.setStatus(Status.APPROVED);
		dao.save(signupDao);
		// TODO Send email to student?
	}
	
	public void accept(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			// Todo need a notfound runtime exception that can me mapped to a 404 at the resource layer.
		}
		String currentUserId = proxy.getCurrentUser().getId();
		boolean canAccept = false;
		// If is course admin on one of the components.
		canAccept = isAdministrator(signupDao.getGroup(), currentUserId, canAccept);
		if (!canAccept) {
			throw new IllegalStateException("You aren't an admin on any on the component for signup: "+ signupId);
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
		
		String supervisorId = signupDao.getSupervisorId();
		if (supervisorId != null) {
			sendSignupEmail(supervisorId, signupDao, "approval.supervisor.subject", "approval.supervisor.body", null);
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
			throw new IllegalStateException("Cannot find courseId: "+ courseId);
		}
		if(!isAdministrator(groupDao, userId, false)) {
			throw new IllegalStateException("You aren't an admin for course: "+ courseId);
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
			throw new IllegalStateException("Cannot find componentId: "+ componentId);
		}
		String userId = proxy.getCurrentUser().getId();
		if (!isAdministrator(componentDao, userId, false)) {
			throw new IllegalStateException("You aren't an admin for component: "+ componentId);
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
		String userId = proxy.getCurrentUser().getId();
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			// TODO Need runtime exception.
		}

		if (Status.PENDING.equals(signupDao.getStatus())) { // Rejected by administrator.
			if (isAdministrator(signupDao.getGroup(), userId, false)) {
				signupDao.setStatus(Status.REJECTED);
				dao.save(signupDao);
				// Mail out to student
				sendSignupEmail(signupDao.getUserId(), signupDao, "reject-admin.student.subject", "reject-admin.student.body", new Object[] {proxy.getCurrentUser().getName()});
			} else {
				throw new IllegalStateException("You are not allowed to reject this signup: "+ signupId);
			}
		} else if (Status.ACCEPTED.equals(signupDao.getStatus())) {// Rejected by lecturer.
			if (isAdministrator(signupDao.getGroup(), userId, userId.equals(signupDao.getSupervisorId()))) {
				signupDao.setStatus(Status.REJECTED);
				dao.save(signupDao);
				// Mail out to student
				sendSignupEmail(signupDao.getUserId(), signupDao, "reject-supervisor.student.subject", "reject-supervisor.student.body", new Object[] {proxy.getCurrentUser().getName()});
			} else {
				throw new IllegalStateException("You are not allowed to reject this signup: "+ signupId);
			}
		} else {
			throw new IllegalStateException("You can only reject signups that are PENDING or ACCEPTED");
		}

	}

	public void setSignupStatus(String signupId, Status status) {
		// TODO Auto-generated method stub

	}
	
	public void signup(String userId, String courseId, Set<String> componentIds) {
		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			throw new IllegalArgumentException("Failed to find group with ID: "+ courseId);
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
				throw new IllegalArgumentException("Failed to find component with ID: "+ componentId);
			}
		}
		String currentUserId = proxy.getCurrentUser().getId();
		if (!isAdministrator(groupDao, currentUserId, false)) {
			throw new IllegalStateException("You are not an administrator for these components"); // TODO I think this needs to be handled through the UI.
		}
		CourseSignupDAO signupDao = dao.newSignup(userId, null);
		signupDao.setCreated(getNow());
		signupDao.setGroup(groupDao);
		signupDao.setStatus(Status.PENDING);
		dao.save(signupDao);
		for (CourseComponentDAO componentDao: componentDaos) {
			componentDao.getSignups().add(signupDao);
			dao.save(componentDao);
		}
	}

	public void signup(String courseId, Set<String> componentIds, String supervisorEmail,
			String message){

		CourseGroupDAO groupDao = dao.findCourseGroupById(courseId);
		if (groupDao == null) {
			throw new IllegalArgumentException("Failed to find group with ID: "+ courseId);
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
				throw new IllegalArgumentException("Failed to find component with ID: "+ componentId);
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
		for (CourseSignupDAO existingSignup :existingSignups) {
			for (CourseComponentDAO componentDao: existingSignup.getComponents()) {
				componentDao.getSignups().remove(existingSignup);
				existingSignup.getComponents().remove(componentDao);
				dao.save(componentDao);
			}
			if (existingSignup.getComponents().size() == 0) {
				dao.remove(existingSignup);
			}
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
		signupDao.getProperties().put("message", message);
		dao.save(signupDao);
		
		// We're going to decrement the places on acceptance.
		for (CourseComponentDAO componentDao: componentDaos) {
			//componentDao.getSignups().add(signupDao); // Link to the signup
			//componentDao.setTaken(componentDao.getTaken()+1); // Increment places taken
			componentDao.getSignups().add(signupDao);
			signupDao.getComponents().add(componentDao); // So when sending out email we know the components.
			dao.save(componentDao);
		}
		String adminId = groupDao.getAdministrator();
		UserProxy admin = loadUser(adminId);
		if (admin != null && admin.getEmail() != null) {
			sendSignupEmail(admin.getEmail(), signupDao, "signup.admin.subject", "signup.admin.body", null);
		} else {
			log.warn("Failed to find user for signup: "+ adminId);
		}
	}
	
	/**
	 * Generic method for sending out a signup email.
	 * @param userId The ID of the user who the message should be sent to.
	 * @param signupDao The signup the message is about.
	 * @param subjectKey The resource bundle key for the subject
	 * @param bodyKey The resource bundle key for the body.
	 * @param url The URL the user should be directed to.
	 */
	public void sendSignupEmail(String userId, CourseSignupDAO signupDao, String subjectKey, String bodyKey, Object[] additionalBodyData) {
		UserProxy user = proxy.findUserById(userId);
		if (user == null) {
			log.warn("Failed to find user for sending email: "+ userId);
			return;
		}
		String to = user.getEmail();
		String subject = MessageFormat.format(rb.getString(subjectKey), new Object[]{proxy.getCurrentUser().getName(), signupDao.getGroup().getTitle()});
		StringBuilder components = new StringBuilder();
		for(CourseComponentDAO componentDao: signupDao.getComponents()) {
			components.append(componentDao.getTitle());
			components.append('\n');
		}
		Object[] baseBodyData = new Object[] {
				proxy.getCurrentUser().getName(),
				components.toString(),
				signupDao.getGroup().getTitle()
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

	public void withdraw(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new IllegalArgumentException("Could not find signup: "+ signupId);
		}
		if (!Status.PENDING.equals(signupDao.getStatus())) {
			throw new IllegalStateException("Can only withdraw from pending signups: "+ signupId);
		}
		signupDao.setStatus(Status.WITHDRAWN);
		dao.save(signupDao);
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
		return (now==null)?new Date():now;
	}
	
	public void setNow(Date now) {
		this.now = now;
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

}
