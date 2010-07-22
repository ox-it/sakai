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
		// Will need to decremnt places on a course.
	}

	public String findSupervisor(String search) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CourseGroup> getAdministering() {
		String userId = proxy.getCurrentUser().getId();
		List <CourseGroupDAO> groupDaos = dao.findAdminCourseGroups(userId);
		return null;
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
		// TODO Auto-generated method stub

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
			dao.save(componentDao);
		}
		
		for (Map.Entry<String, Collection<String>>entry : admins.entrySet()) {
			String subject = MessageFormat.format(rb.getString("signup.admin.subject"), new Object[]{proxy.getCurrentUser().getName(), groupDao.getTitle()});
			String adminId = entry.getKey();
			User user = proxy.findUserById(adminId);
			if (user == null) {
				log.warn("Failed to find user for signup: "+ adminId);
				continue;
			}
			String to = user.getEmail();
			StringBuilder components = new StringBuilder();
			for(String component : entry.getValue()) {
				components.append(component);
				components.append('\n');
			}
			String body = MessageFormat.format(rb.getString("signup.admin.body"), new Object[] {
					proxy.getCurrentUser().getName(),
					components.toString(),
					groupDao.getTitle()
			});
			proxy.sendEmail(to, subject, body);
		} 
	}

	public void withdraw(String signupId) {
		CourseSignupDAO signupDao = dao.findSignupById(signupId);
		if (signupDao == null) {
			throw new IllegalArgumentException("Could not find signup: "+ signupId);
		}
		if (Status.PENDING.equals(signupDao.getStatus())) {
			throw new IllegalStateException("Can only withdraw from pending signups: "+ signupId);
		}
		signupDao.setStatus(Status.WITHDRAWN);
		for (CourseComponentDAO componentDao: signupDao.getComponents()) {
			componentDao.setTaken(componentDao.getTaken()-1);
		}
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
