package uk.ac.ox.oucs.vle;

import java.util.List;
import java.util.Set;

public class CourseSignupServiceImpl implements CourseSignupService {

	private CourseDAO dao;
	
	public void approve(String signupId) {
		
	}

	public String findSupervisor(String search) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CourseGroup> getAdministering() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CourseSignup> getApprovals() {
		// TODO Auto-generated method stub
		return null;
	}

	public CourseGroup getCourseGroup(String courseId) {
		CourseGroupDAO courseGroupDao = dao.findCourseGroupById(courseId);
		CourseGroup courseGroup = null;
		if (courseGroupDao != null) {
			courseGroup = new CourseGroupImpl(courseGroupDao, dao);
		}
		return courseGroup;
	}

	public List<CourseSignup> getCourseSignups(String courseId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void reject(String signupId) {
		// TODO Auto-generated method stub

	}

	public void setSignupStatus(String signupId, Status status) {
		// TODO Auto-generated method stub

	}

	public void signup(Set<String> components, String superviorId,
			String message) {
		// Need to find all the components.
		// Check they are valid as a choice
		// Set the supervisor
		// Save
		// Send out email message.
	}

	public void withdraw(String signupId) {
		// TODO Auto-generated method stub

	}

	public CourseGroup getAvailableCourseGroup(String courseId) {
		// TODO Auto-generated method stub
		return null;
	}

}
