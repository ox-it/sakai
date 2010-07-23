package uk.ac.ox.oucs.vle;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import uk.ac.ox.oucs.vle.proxy.User;

public class CourseSignupServiceImpl implements CourseSignupService {
	
	private final static Log log = LogFactory.getLog(CourseSignupServiceImpl.class);
	
	private final static ResourceBundle rb = ResourceBundle.getBundle("messages");

	private CourseDAO dao;
	private SakaiProxy proxy;
	
	
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
		if (currentUserId.equals(signupDao)) {
			canApprove = true;
		} else {
			canApprove = isAdministrator(signupDao, currentUserId, canApprove);
		}
		if (!canApprove) {
			throw new IllegalStateException("You are not alloed to approve this signup: "+ signupId);
		}
		signupDao.setStatus(Status.APPROVED);
		dao.save(signupDao);
		// Send email to student?
	}
	
	public void accept(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			// Todo need a notfound runtime exception that can me mapped to a 404 at the resource layer.
		}
		String currentUserId = proxy.getCurrentUser().getId();
		boolean canAccept = false;
		// If is course admin on one of the components.
		canAccept = isAdministrator(signupDao, currentUserId, canAccept);
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
		
		sendSignupEmail(supervisorId, signupDao, "approval.supervisor.subject", "approval.supervisor.body", null);
	}

	private boolean isAdministrator(CourseSignupDAO signupDao,
			String currentUserId, boolean canAccept) {
		for (CourseComponentDAO componentDao : signupDao.getComponents()) {
			if (componentDao.getAdministrator().equals(currentUserId)) {
				canAccept = true;
				break;
			}
		}
		return canAccept;
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
		// TODO Auto-generated method stub
		return null;
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

		List<CourseSignupDAO> signupDaos = dao.findSignupByCourse(userId, courseId);
		List<CourseSignup> signups = new ArrayList<CourseSignup>(signupDaos.size());
		for(CourseSignupDAO signupDao: signupDaos) {
			signups.add(new CourseSignupImpl(signupDao, this));
		}
		return signups;
	}

	public void reject(String signupId) {
		String userId = proxy.getCurrentUser().getId();
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			// TODO Need runtime exception.
		}

		if (Status.PENDING.equals(signupDao.getStatus())) { // Rejected by administrator.
			if (isAdministrator(signupDao, userId, false)) {
				signupDao.setStatus(Status.REJECTED);
				dao.save(signupDao);
				// Mail out to student
				sendSignupEmail(signupDao.getUserId(), signupDao, "reject-admin.student.subject", "reject-admin.student.body", new Object[] {proxy.getCurrentUser().getName()});
			} else {
				throw new IllegalStateException("You are not allowed to reject this signup: "+ signupId);
			}
		} else if (Status.ACCEPTED.equals(signupDao.getStatus())) {// Rejected by lecturer.
			if (isAdministrator(signupDao, userId, userId.equals(signupDao.getSupervisorId()))) {
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
		// TODO Check they are all have a common course group.
		Date now = getNow();
		String userId = proxy.getCurrentUser().getId();
		for(CourseComponentDAO componentDao: componentDaos) {
			if(componentDao.getOpens().after(now) || componentDao.getCloses().before(now)) {
				throw new IllegalStateException("Component isn't currently open: "+ componentDao.getId());
			}
			if ( (componentDao.getSize()-componentDao.getTaken()) < 1) {
				throw new IllegalStateException("No places left on: "+ componentDao.getId());
			}
			// TODO If state is withdrawn ignore it.
			for (CourseSignupDAO signupDao: componentDao.getSignups()) {
				if (userId.equals(signupDao.getUserId())) {
					throw new IllegalStateException("User "+ userId+ " already has a place on component: "+ componentDao.getId());
				}
			}
		}
		// Set the supervisor
		User supervisor = proxy.findUserByEmail(supervisorEmail);
		if (supervisor == null) {
			throw new IllegalArgumentException("Can't find a supervisor with email: "+ supervisorEmail);
		}
		
		// Create the signup.
		String supervisorId = supervisor.getId();
		CourseSignupDAO signupDao = dao.newSignup(userId, supervisorId);
		signupDao.setGroup(groupDao);
		signupDao.setStatus(Status.PENDING);
		signupDao.getProperties().put("message", message);
		dao.save(signupDao);
		
		// For each course admin store all the components they are responsible for.
		Map<String,Collection<String>> admins = new HashMap<String,Collection<String>>();
		// We're going to decrement the places on acceptance.
		for (CourseComponentDAO componentDao: componentDaos) {
			//componentDao.getSignups().add(signupDao); // Link to the signup
			//componentDao.setTaken(componentDao.getTaken()+1); // Increment places taken
			String admin = componentDao.getAdministrator();
			if (!admins.containsKey(admin)) {
				admins.put(admin, new ArrayList<String>());
			}
			admins.get(admin).add(componentDao.getTitle());
			componentDao.getSignups().add(signupDao);
			signupDao.getComponents().add(componentDao); // So when sending out email we know the components.
			dao.save(componentDao);
		}
		
		for (Map.Entry<String, Collection<String>>entry : admins.entrySet()) {
			sendSignupEmail(entry.getKey(), signupDao, "signup.admin.subject", "signup.admin.body", null);
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
		User user = proxy.findUserById(userId);
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
		return new Date();
	}

	/**
	 * Loads details about a user.
	 * @return
	 */
	User loadUser(String id) {
		return proxy.findUserById(id);
	}

}
