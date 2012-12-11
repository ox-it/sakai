
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

import java.io.IOException;
import java.util.Collections;

import org.jdom.JDOMException;
import org.xcri.exceptions.InvalidElementException;
public class TestPopulatorWrapper extends TestOnSampleData {
	
	public void testFlagSelectedCourseGroups() {
		dao.flagSelectedCourseGroups("Test");
		CourseGroupDAO group = dao.findCourseGroupById("30");
		assertNotNull(group);
		assertTrue(group.getDeleted());
	}
	
	public void testFlagSelectedCourseComponents() {
		dao.flagSelectedCourseComponents("Test");
		CourseComponentDAO component = dao.findCourseComponent("30");
		assertNotNull(component);
		assertTrue(component.getDeleted());
	}
	
	/**
	 * Daisy Import will delete all Daisy courses that are future 
	 * and not in the xcri and have no signups
	 */
	
	public void testFlagSelectedDaisyCourseGroups() {
		dao.flagSelectedDaisyCourseGroups("Test");
		
		CourseGroupDAO group;
		
		group = dao.findCourseGroupById("30");
		assertNotNull(group);
		assertFalse(group.getDeleted());
		
		group = dao.findCourseGroupById("31");
		assertNotNull(group);
		assertFalse(group.getDeleted());
		
		group = dao.findCourseGroupById("32");
		assertNotNull(group);
		assertFalse(group.getDeleted());
		
		group = dao.findCourseGroupById("34");
		assertNotNull(group);
		assertTrue(group.getDeleted());
		
		group = dao.findCourseGroupById("33");
		assertNotNull(group);
		assertTrue(group.getDeleted());
		
		group = dao.findCourseGroupById("36");
		assertNotNull(group);
		assertTrue(group.getDeleted());
		
	}
	
	/**
	 * Daisy Import will delete all Daisy courses that are future 
	 * and not in the xcri and have no signups
	 */
	
	public void testFlagSelectedDaisyCourseComponents() {
		
		dao.flagSelectedDaisyCourseComponents("Test");
		CourseComponentDAO component;
		
		component = dao.findCourseComponent("30");
		assertNotNull(component);
		assertFalse(component.getDeleted());
		
		component = dao.findCourseComponent("31");
		assertNotNull(component);
		assertFalse(component.getDeleted());
		
		component = dao.findCourseComponent("32");
		assertNotNull(component);
		assertFalse(component.getDeleted());
		
		component = dao.findCourseComponent("34");
		assertNotNull(component);
		assertTrue(component.getDeleted());
		
		component = dao.findCourseComponent("33");
		assertNotNull(component);
		assertTrue(component.getDeleted());
		
		component = dao.findCourseComponent("36");
		assertNotNull(component);
		assertTrue(component.getDeleted());
		
	}
	
}
