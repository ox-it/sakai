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

	public static final Date START2010 = SampleDataLoader.newCalendar(2010, 1, 1).getTime();

	@Test
	public void testFlagSelectedCourseGroups() {
		dao.flagSelectedCourseGroups("Test");
		CourseGroupDAO group = dao.findCourseGroupById("course-3");
		assertNotNull(group);
		assertTrue(group.getDeleted());
	}

	@Test
	public void testFlagSelectedCourseComponents() {
		dao.flagSelectedCourseComponents("Test");
		CourseComponentDAO component = dao.findCourseComponent("comp-9");
		assertNotNull(component);
		assertTrue(component.getDeleted());
	}

	// The DAISY importer tests don't work as they use updates with joins and these don't work on
	// using SQL on test databases. We can't migrate to hibernate as it doesn't support joins on
	// update statements either. So for now they aren't running
	// TODO Fix them.

	/**
	 * Daisy Import will delete all Daisy courses that are future
	 * and not in the xcri and have no signups
	 */
	public void testFlagSelectedDaisyCourseGroups() {
		//
		dao.flagSelectedDaisyCourseGroups("Test", START2010);

		checkDeletedGroup("course-2");
		checkDeletedGroup("course-3");
	}


	/**
	 * Daisy Import will delete all Daisy courses that are future
	 * and not in the xcri and have no signups
	 */

	public void testFlagSelectedDaisyCourseComponents() {

		dao.flagSelectedDaisyCourseComponents("Test", START2010);
		CourseComponentDAO component;
		//

	}

	private void checkDeletedGroup(String courseId) {
		CourseGroupDAO group;
		group = dao.findCourseGroupById(courseId);
		assertNotNull(group);
		assertTrue(group.getDeleted());
	}

	private void checkDeletedComponent(String componentId) {
		CourseComponentDAO componentDAO;
		componentDAO = dao.findCourseComponent(componentId);
		assertNotNull(componentDAO);
		assertTrue(componentDAO.getDeleted());
	}
	
}
