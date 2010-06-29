package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.List;

public interface CourseDAO {

	CourseGroupDAO findCourseGroupById(String courseId);
	
	CourseGroupDAO findAvailableCourseGroupById(String courseId);

	List<CourseComponentDAO> findOpenComponents(String id, Date at);
	

}
