package uk.ac.ox.oucs.vle;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;

public class TestCourseDAOImpl extends TestOnSampleData {

	CourseDAOImpl courseDao;
	
	private final Date END_MIC_2010 = createDate(2010, 12, 4); 

	public void onSetUp() throws Exception {
		courseDao = (CourseDAOImpl) getApplicationContext().getBean("uk.ac.ox.oucs.vle.CourseDAO");
	}
	
	private static Date createDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return new Date(cal.getTimeInMillis());
	}
	
	public void testAvailableCourses() {
		CourseGroupDAO course = courseDao.findUpcomingComponents("course-1", END_MIC_2010);
		assertNotNull(course);
		assertNotNull(course.getComponents());
		assertEquals(2, course.getComponents().size());
	}
	
	public void testCoursesInDept() {
		List<CourseGroupDAO> groups = courseDao.findCourseGroupByDept("3C05", Range.UPCOMING, END_MIC_2010);
		assertEquals(2, groups.size());
	}
	
}
