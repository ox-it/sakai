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

 /*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, 
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * The above copyright notice and this permission notice shall be included in all copies 
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package uk.ac.ox.oucs.vle;

import org.junit.Test;

import java.util.Date;

public class TestPopulatorWrapper extends OnSampleData {

	public static final Date START2011 = SampleDataLoader.newCalendar(2011, 1, 1).getTime();

	@Test
	public void testFlagSelectedCourseGroups() {
		assertTrue(dao.flagSelectedCourseGroups("Test") > 0);
		checkDeletedGroup("course-3", true);
	}

	@Test
	public void testFlagSelectedCourseComponents() {
		assertTrue(dao.flagSelectedCourseComponents("Test") > 0);
		checkDeletedComponent("comp-1", true);
	}

	/**
	 * Daisy Import will delete all Daisy courses that are future
	 * and not in the xcri and have no signups
	 */
	@Test
	public void testFlagSelectedDaisyCourseGroups() {
		//
		assertTrue(dao.flagSelectedDaisyCourseGroups("Test", START2011) > 0);

		checkDeletedGroup("course-1", false); // In past and signups.
		checkDeletedGroup("course-2", false); // In past and no signups.
		checkDeletedGroup("course-3", true); // In future and no signups.
		checkDeletedGroup("course-4", false); // In future with signups.
	}

	/**
	 * Daisy Import will delete all Daisy components that are future
	 * and not in the xcri and have no signups
	 */
	@Test
	public void testFlagSelectedDaisyCourseComponents() {

		assertTrue(dao.flagSelectedDaisyCourseComponents("Test", START2011) > 0);

		checkDeletedComponent("comp-1", false); // In past and no signups
		checkDeletedComponent("comp-2", false); // In past and no signups
		checkDeletedComponent("comp-3", false); // In past and no signups
		checkDeletedComponent("comp-4", true); // In future, no signups
		checkDeletedComponent("comp-5", true); // In future, no signups
		checkDeletedComponent("comp-6", false); // In past and signups
		checkDeletedComponent("comp-7", false); // In past and signups
		checkDeletedComponent("comp-8", false); // In past and no signups
		checkDeletedComponent("comp-9", true); // In future and no signups
		checkDeletedComponent("comp-10", false); // In future with signups.

	}

	private void checkDeletedGroup(String courseId, boolean deleted) {
		CourseGroupDAO group;
		group = dao.findCourseGroupById(courseId);
		assertNotNull(group);
		assertEquals(deleted, group.getDeleted());
	}

	private void checkDeletedComponent(String componentId, boolean deleted) {
		CourseComponentDAO componentDAO;
		componentDAO = dao.findCourseComponent(componentId);
		assertNotNull(componentDAO);
		assertEquals(deleted, componentDAO.getDeleted());
	}
	
}
