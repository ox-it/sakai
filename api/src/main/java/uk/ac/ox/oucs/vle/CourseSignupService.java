package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CourseSignupService {
	
	public static enum Status {PENDING, WITHDRAWN, APPROVED, ACCEPTED};
	
	public static enum Range {ALL, UPCOMING, PREVIOUS};

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
	
	public void signup(Set<String> components, String supervisorEmail, String message);
	
	public void withdraw(String signupId);
	
	public List<CourseSignup> getCourseSignups(String courseId);
	
	public List<CourseSignup> getApprovals();
	
	public void approve(String signupId);
	
	public void reject(String signupId);
	
	public List<CourseGroup> getAdministering();
	
	public void setSignupStatus(String signupId, Status status);
	
	/**
	 * This is what the service should use when determining the current time.
	 * This is to support testing the data against different times.
	 * @return
	 */
	public Date getNow();

	public List<CourseSignup> getMySignups(Set<Status> statuses);
}
