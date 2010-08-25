package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CourseSignupService {
	
	public static enum Status {PENDING, WITHDRAWN, APPROVED, ACCEPTED, REJECTED};
	
	public static enum Range {ALL, UPCOMING, PREVIOUS};
	
	// List of events
	public static final String EVENT_SIGNUP = "coursesignup.signup";
	public static final String EVENT_ACCEPT = "coursesignup.accept";
	public static final String EVENT_WITHDRAW = "coursesignup.withdraw";
	public static final String EVENT_APPROVE = "coursesignup.approve";
	public static final String EVENT_REJECT = "coursesignup.reject";
	

	public CourseGroup getCourseGroup(String courseId, Range range);
	
	/**
	 * This loads a course group with only the data that is available at the moment.
	 * @param courseId
	 * @return
	 */
	public CourseGroup getAvailableCourseGroup(String courseId);
	
	/**
	 * Finds all course groups 
	 * @param deptId
	 * @param range
	 * @return
	 */
	public List<CourseGroup> getCourseGroups(String deptId, Range range);
	
	public String findSupervisor(String search);
	
	public void signup(String courseId, Set<String> components, String supervisorEmail, String message);
	
	/**
	 * A signup made
	 * @param userId
	 * @param courseId
	 * @param components
	 */
	public void signup(String userId, String courseId, Set<String> components);
	
	public List<CourseSignup> getCourseSignups(String courseId);
	
	public List<CourseSignup> getComponentSignups(String componentId);
	
	public List<CourseSignup> getApprovals();
	
	public void approve(String signupId);
	
	public void accept(String signupId);
	
	public void reject(String signupId);

	public void withdraw(String signupId);
	
	/**
	 * Gets all the CourseGroups that the current user can administer.
	 * @return
	 */
	public List<CourseGroup> getAdministering();
	
	public void setSignupStatus(String signupId, Status status);
	
	public List<CourseGroup> search(String search);
	
	/**
	 * This is what the service should use when determining the current time.
	 * This is to support testing the data against different times.
	 * @return
	 */
	public Date getNow();
	
	/**
	 * Allow setting the date. This should be removed once out of development.
	 */
	public void setNow(Date date);

	public List<CourseSignup> getMySignups(Set<Status> statuses);
}
