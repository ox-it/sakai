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
package uk.ac.ox.oucs.vle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

public class HbmTests extends AbstractTransactionalSpringContextTests {

	private CourseDAOImpl courseDao;
	private SessionFactory sessionFactory;

	public void onSetUp() throws Exception {
		super.onSetUp();
		courseDao = (CourseDAOImpl) getApplicationContext().getBean("uk.ac.ox.oucs.vle.CourseDAO");
		sessionFactory = (SessionFactory) getApplicationContext().getBean("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory");
	}
	
	public void onTearDown() throws Exception {
		super.onTearDown();
	} 


	protected String[] getConfigPaths() {
		return new String[]{"/course-signup-beans.xml", "/test-sakai-beans.xml"};
	}


	/**
	 * This checks that when you delete a component without a signup only that component gets deleted.
	 * This came about through WL-2645.
	 */
	
	public void testDeletingComponent() {
		// Create a dummp group and add the component.
		CourseGroupDAO newCourseGroup = courseDao.newCourseGroup("id", "Title", "Department", "Subunit");
		newCourseGroup.setSource("source");
		courseDao.save(newCourseGroup);

		// Create a dummy component.
		CourseComponentDAO newCourseComponent1 = courseDao.newCourseComponent("test1");
		newCourseComponent1.getGroups().add(newCourseGroup);
		newCourseComponent1.setSource("source");
		courseDao.save(newCourseComponent1);

		// Create a dummy component that is flagged to be deleted.
		CourseComponentDAO newCourseComponent2 = courseDao.newCourseComponent("test2");
		newCourseComponent2.getGroups().add(newCourseGroup);
		newCourseComponent2.setSource("source");
		newCourseComponent2.setDeleted(true);
		courseDao.save(newCourseComponent2);

		// Now create a signup linked to the group and the first component.
		CourseSignupDAO newSignup = courseDao.newSignup("userId", "supervisorId");
		newSignup.setGroup(newCourseGroup);
		newSignup.getComponents().add(newCourseComponent1);
		courseDao.save(newSignup);

		// Now attempt to delete the second component.
		courseDao.deleteSelectedCourseComponents("source");
	}
	
	public void testSharedComponent() {
		
		// First course group.
		CourseGroupDAO newCourseGroup1 = courseDao.newCourseGroup("id1", "Title", "Department", "Subunit");
		newCourseGroup1.setSource("source");
		courseDao.save(newCourseGroup1);

		// Second course group.
		CourseGroupDAO newCourseGroup2 = courseDao.newCourseGroup("id2", "Title", "Department", "Subunit");
		newCourseGroup2.setSource("source");
		courseDao.save(newCourseGroup2);
		
		// Create a component.
		CourseComponentDAO newCourseComponent = courseDao.newCourseComponent("test");
		newCourseComponent.setSource("source");
		newCourseComponent.getGroups().add(newCourseGroup1);
		newCourseComponent.getGroups().add(newCourseGroup2);
		courseDao.save(newCourseComponent);	
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		// Check the groups both have the component
		CourseGroupDAO daoA = courseDao.findCourseGroupById("id1");
		assertEquals(1, daoA.getComponents().size());
		
		CourseGroupDAO daoB = courseDao.findCourseGroupById("id2");
		assertEquals(1, daoB.getComponents().size());
		
		// Check the components both have groups
		CourseComponentDAO daoC = courseDao.findCourseComponent("test");
		assertEquals(2, daoC.getGroups().size());
	}
	
	
	public void testSharedComponentDeleteGroup() {
		
		// First course group.
		CourseGroupDAO newCourseGroup1 = courseDao.newCourseGroup("id1", "Title", "Department", "Subunit");
		newCourseGroup1.setSource("source");
		courseDao.save(newCourseGroup1);

		// Second course group.
		CourseGroupDAO newCourseGroup2 = courseDao.newCourseGroup("id2", "Title", "Department", "Subunit");
		newCourseGroup2.setSource("source");
		newCourseGroup2.setDeleted(true);
		courseDao.save(newCourseGroup2);
		
		// Create a component.
		CourseComponentDAO newCourseComponent = courseDao.newCourseComponent("test");
		newCourseComponent.setSource("source");
		newCourseComponent.getGroups().add(newCourseGroup1);
		newCourseComponent.getGroups().add(newCourseGroup2);
		courseDao.save(newCourseComponent);	
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		// Check the groups both have the component
		CourseGroupDAO daoA = courseDao.findCourseGroupById("id1");
		assertEquals(1, daoA.getComponents().size());
		
		CourseGroupDAO daoB = courseDao.findCourseGroupById("id2");
		assertEquals(1, daoB.getComponents().size());
		
		// Check the components both have groups
		CourseComponentDAO daoC = courseDao.findCourseComponent("test");
		assertEquals(2, daoC.getGroups().size());
		
		// Now test deletion
		courseDao.deleteSelectedCourseGroups("source");
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
			
		daoA = courseDao.findCourseGroupById("id1");
		assertNotNull(daoA);
		assertEquals(1, daoA.getComponents().size());
				
		daoB = courseDao.findCourseGroupById("id2");
		assertNull(daoB);
		
		daoC = courseDao.findCourseComponent("test");
		assertEquals(1, daoC.getGroups().size());
	}
	
	public void testSharedComponentDeleteComponent() {
		
		// First course group.
		CourseGroupDAO newCourseGroup1 = courseDao.newCourseGroup("id1", "Title", "Department", "Subunit");
		newCourseGroup1.setSource("source");
		courseDao.save(newCourseGroup1);

		// Second course group.
		CourseGroupDAO newCourseGroup2 = courseDao.newCourseGroup("id2", "Title", "Department", "Subunit");
		newCourseGroup2.setSource("source");
		courseDao.save(newCourseGroup2);
		
		// Create a component.
		CourseComponentDAO newCourseComponent1 = courseDao.newCourseComponent("test1");
		newCourseComponent1.setSource("source");
		newCourseComponent1.getGroups().add(newCourseGroup1);
		newCourseComponent1.getGroups().add(newCourseGroup2);
		courseDao.save(newCourseComponent1);	
		
		// and another
		CourseComponentDAO newCourseComponent2 = courseDao.newCourseComponent("test2");
		newCourseComponent2.setSource("source");
		newCourseComponent2.setDeleted(true);
		newCourseComponent2.getGroups().add(newCourseGroup1);
		newCourseComponent2.getGroups().add(newCourseGroup2);
		courseDao.save(newCourseComponent2);	
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		// Check the groups both have the component
		CourseGroupDAO daoA = courseDao.findCourseGroupById("id1");
		assertEquals(2, daoA.getComponents().size());
		
		CourseGroupDAO daoB = courseDao.findCourseGroupById("id2");
		assertEquals(2, daoB.getComponents().size());
		
		// Check the components both have groups
		CourseComponentDAO daoC = courseDao.findCourseComponent("test1");
		assertEquals(2, daoC.getGroups().size());
		
		CourseComponentDAO daoD = courseDao.findCourseComponent("test2");
		assertEquals(2, daoD.getGroups().size());
		
		// Now test deletion
		courseDao.deleteSelectedCourseComponents("source");
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
			
		daoA = courseDao.findCourseGroupById("id1");
		assertNotNull(daoA);
		assertEquals(1, daoA.getComponents().size());
				
		daoB = courseDao.findCourseGroupById("id2");
		assertNotNull(daoB);
		assertEquals(1, daoB.getComponents().size());
		
		daoC = courseDao.findCourseComponent("test1");
		assertNotNull(daoC);
		assertEquals(2, daoC.getGroups().size());
		
		daoD = courseDao.findCourseComponent("test2");
		assertNull(daoD);
	}
	
public void testCourseCategory() {
		
		CourseCategoryDAO cat1 = new CourseCategoryDAO(CourseGroup.Category_Type.RM, "C1", "Category 1");
		CourseCategoryDAO cat2 = new CourseCategoryDAO(CourseGroup.Category_Type.RM, "C2", "Category 2");
		CourseCategoryDAO cat3 = new CourseCategoryDAO(CourseGroup.Category_Type.RM, "C3", "Category 3");
		
		// First course group.
		CourseGroupDAO newCourseGroup1 = courseDao.newCourseGroup("id1", "Title", "Department", "Subunit");
		newCourseGroup1.setSource("source");
		courseDao.save(newCourseGroup1);

		// Second course group.
		CourseGroupDAO newCourseGroup2 = courseDao.newCourseGroup("id2", "Title", "Department", "Subunit");
		newCourseGroup2.setSource("source");
		newCourseGroup2.setDeleted(true);
		courseDao.save(newCourseGroup2);
		
		cat1.getGroups().add(newCourseGroup1);
		cat2.getGroups().add(newCourseGroup1);
		
		cat1.getGroups().add(newCourseGroup2);
		cat2.getGroups().add(newCourseGroup2);
		cat3.getGroups().add(newCourseGroup2);
		
		courseDao.save(cat1);
		courseDao.save(cat2);
		courseDao.save(cat3);
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		
		// Check all is as it should be
		CourseGroupDAO daoA = courseDao.findCourseGroupById("id1");
		assertEquals(2, daoA.getCategories().size());
		
		CourseGroupDAO daoB = courseDao.findCourseGroupById("id2");
		assertEquals(3, daoB.getCategories().size());
		
		CourseCategoryDAO c1 = courseDao.findCourseCategory("C1");
		assertEquals(2, c1.getGroups().size());
		
		CourseCategoryDAO c2 = courseDao.findCourseCategory("C2");
		assertEquals(2, c2.getGroups().size());
		
		CourseCategoryDAO c3 = courseDao.findCourseCategory("C3");
		assertEquals(1, c3.getGroups().size());
		
		// now delete a group
		courseDao.deleteSelectedCourseGroups("source");
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		
		// and the checks
		CourseGroupDAO gA = courseDao.findCourseGroupById("id1");
		assertEquals(2, gA.getCategories().size());

		CourseGroupDAO gB = courseDao.findCourseGroupById("id2");
		assertNull(gB);

		CourseCategoryDAO ct1 = courseDao.findCourseCategory("C1");
		assertEquals(1, ct1.getGroups().size());

		CourseCategoryDAO ct2 = courseDao.findCourseCategory("C2");
		assertEquals(1, ct2.getGroups().size());

		CourseCategoryDAO ct3 = courseDao.findCourseCategory("C3");
		assertEquals(0, ct3.getGroups().size());

	}

	public void testCourseComponentSession() {
	
		SimpleDateFormat format = new SimpleDateFormat("EEEE dd MMMM yyyy, HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 10);
		Date start = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 2);
		Date end = cal.getTime();
		
		CourseComponentSessionDAO ss1 = new CourseComponentSessionDAO("1",
				start, format.format(start),
				end, format.format(end));
		CourseComponentSessionDAO ss2 = new CourseComponentSessionDAO("2",
				start, format.format(start),
				end, format.format(end));
		CourseComponentSessionDAO ss3 = new CourseComponentSessionDAO("3",
				start, format.format(start),
				end, format.format(end));
	
		// Create a component.
		CourseComponentDAO courseComponent = courseDao.newCourseComponent("test");
		courseComponent.setSource("source");
		courseComponent.getComponentSessions().add(ss1);
		courseDao.save(courseComponent);	
	
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	
		// Check all is as it should be
		courseComponent  = courseDao.findCourseComponent("test");
		assertEquals(1, courseComponent.getComponentSessions().size());
	
		// now add a session
		courseComponent.getComponentSessions().add(ss2);
		courseDao.save(courseComponent);
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	
		// and check
		courseComponent  = courseDao.findCourseComponent("test");
		assertEquals(2, courseComponent.getComponentSessions().size());
		
		// change the sessions
		courseComponent.getComponentSessions().clear();
		courseComponent.getComponentSessions().add(ss3);
		courseDao.save(courseComponent);
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	
		// and check
		courseComponent  = courseDao.findCourseComponent("test");
		assertEquals(1, courseComponent.getComponentSessions().size());

	}
	
}