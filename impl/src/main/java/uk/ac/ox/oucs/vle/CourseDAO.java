package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.List;
import java.util.Set;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public interface CourseDAO {

	CourseGroupDAO findCourseGroupById(String courseId, Range range, Date now);
	
	CourseGroupDAO findCourseGroupById(String courseId);
	
	CourseGroupDAO findAvailableCourseGroupById(String courseId);
	
	List<CourseGroupDAO> findCourseGroupByDept(String dept, Range range, Date now);
	
	List<CourseGroupDAO> findCourseGroupBySubunit(String subunit, Range range, Date now);

	List<CourseComponentDAO> findOpenComponents(String id, Date at);

	CourseGroupDAO findUpcomingComponents(String courseId, Date available);
	
	CourseComponentDAO findCourseComponent(String id);
	
	CourseComponentDAO newCourseComponent(String id);

	CourseSignupDAO newSignup(String userId, String supervisorId);

	String save(CourseSignupDAO signupDao);

	String save(CourseComponentDAO componentDao);

	CourseSignupDAO findSignupById(String signupId);

	List<CourseSignupDAO> findSignupForUser(String userId, Set<Status> statuses);

	CourseGroupDAO newCourseGroup(String id, String title, String dept, String subunit);

	void save(CourseGroupDAO groupDao);

	List<CourseGroupDAO> findAdminCourseGroups(String userId);

	List<CourseSignupDAO> findSignupByCourse(String userId, String courseId, Set<Status> statuses);

	List<CourseGroupDAO> findCourseGroupByWords(String[] words, Range range, Date date);

	List<CourseSignupDAO> findSignupByComponent(String componentId, Set<Status> statuses);

	List<CourseSignupDAO> findSignupPending(String currentUser);

	void remove(CourseSignupDAO existingSignup);
	

}
