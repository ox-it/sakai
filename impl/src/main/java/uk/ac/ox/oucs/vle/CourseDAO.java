package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.List;

public interface CourseDAO {

	CourseGroupDAO findCourseGroupById(String courseId);
	
	CourseGroupDAO findAvailableCourseGroupById(String courseId);
	
	List<CourseGroupDAO> findCourseGroupByDept(String dept);

	List<CourseComponentDAO> findOpenComponents(String id, Date at);

	CourseGroupDAO findUpcomingComponents(String courseId, Date available);
	

}
