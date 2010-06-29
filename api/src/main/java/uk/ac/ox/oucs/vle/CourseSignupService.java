package uk.ac.ox.oucs.vle;

import java.util.List;
import java.util.Set;

public interface CourseSignupService {
	
	public static enum Status {PENDING, WITHDRAWN, APPROVED, ACCEPTED}; 

	public CourseGroup getCourseGroup(String courseId);
	
	/**
	 * This loads a course group with only the data that is available at the moment.
	 * @param courseId
	 * @return
	 */
	public CourseGroup getAvailableCourseGroup(String courseId);
	
	public String findSupervisor(String search);
	
	public void signup(Set<String> components, String superviorId, String message);
	
	public void withdraw(String signupId);
	
	public List<CourseSignup> getCourseSignups(String courseId);
	
	public List<CourseSignup> getApprovals();
	
	public void approve(String signupId);
	
	public void reject(String signupId);
	
	public List<CourseGroup> getAdministering();
	
	public void setSignupStatus(String signupId, Status status);
	
}
