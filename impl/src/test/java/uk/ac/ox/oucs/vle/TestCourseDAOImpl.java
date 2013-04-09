package uk.ac.ox.oucs.vle;

/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
		List<CourseGroupDAO> groups = courseDao.findCourseGroupByDept("3C05", Range.UPCOMING, END_MIC_2010, false);
		assertEquals(1, groups.size());
	}
	
	public void testAdminCourseGroups() {
		List<CourseGroupDAO> groups = courseDao.findAdminCourseGroups("d86d9720-eba4-40eb-bda3-91b3145729da");
		assertEquals(3, groups.size());
		groups = courseDao.findAdminCourseGroups("c10cdf4b-7c10-423c-8319-2d477051a94e");
		assertEquals(1, groups.size());
	}
	
	public void testFindSignupByCourse() {
		List<CourseSignupDAO> signups = courseDao.findSignupByCourse("d86d9720-eba4-40eb-bda3-91b3145729da", "course-1", null);
		assertEquals(1,signups.size());
	}
	
}
