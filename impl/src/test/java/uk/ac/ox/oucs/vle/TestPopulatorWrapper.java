
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
	
	CourseDAOImpl courseDao;
	
	protected void onSetUp() throws JDOMException, IOException, InvalidElementException {
		courseDao = (CourseDAOImpl) getApplicationContext().getBean("uk.ac.ox.oucs.vle.CourseDAO");
		
		CourseGroupDAO groupDao = new CourseGroupDAO();
		
		groupDao.setAdministratorApproval(true);
		groupDao.setAdministrators(Collections.singleton("administrator"));
		groupDao.setCategories(Collections.singleton(new CourseCategoryDAO()));
		groupDao.setComponents(Collections.singleton(new CourseComponentDAO()));
		groupDao.setContactEmail("contactEmail");
		groupDao.setCourseId("courseId-1");
		groupDao.setDepartmentName("departmentName");
		groupDao.setDept("dept");
		groupDao.setDescription("description");
		groupDao.setHideGroup(false);
		groupDao.setOtherDepartments(Collections.singleton("otherDepartment"));
		groupDao.setRegulations("Regulations");
		groupDao.setSignups(Collections.singleton(new CourseSignupDAO()));
		groupDao.setSource("test");
		groupDao.setSubunit("subunit");
		groupDao.setSubunitName("subunitName");
		groupDao.setSuperusers(Collections.singleton("superUser"));
		groupDao.setSupervisorApproval(false);
		groupDao.setTitle("title");
		groupDao.setVisibility("visibility");
		
		courseDao.save(groupDao);
	}
	
	protected void onTearDown() {
		
	}
	
	
	public void testFlagSelectedCourseGroups() {
		courseDao.flagSelectedCourseGroups("Test");
		
		CourseGroupDAO dao = courseDao.findCourseGroupById("courseId-1");
		assertNotNull(dao);
		assertTrue(dao.getDeleted());
	}
		
}
