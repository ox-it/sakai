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

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import java.text.SimpleDateFormat;
import java.util.*;

import static uk.ac.ox.oucs.vle.CourseSignupService.*;

public class TestCourseDAO extends AbstractTransactionalSpringContextTests {

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
		return new String[]{"/course-dao.xml", "/test-with-h2.xml"};
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
		CourseSignupDAO newSignup = courseDao.newSignup("userId", "supervisorId", new Date());
		newSignup.setGroup(newCourseGroup);
		newSignup.getComponents().add(newCourseComponent1);
		courseDao.save(newSignup);

		// Now attempt to delete the second component.
		try {
			courseDao.deleteSelectedCourseComponents("source");
		} catch (RuntimeException re) {
			// Fail the test if we get an exception.
			throw re;
		}
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

		CourseCategoryDAO cat1 = new CourseCategoryDAO(CourseGroup.CategoryType.RM, "C1", "Category 1");
		CourseCategoryDAO cat2 = new CourseCategoryDAO(CourseGroup.CategoryType.RM, "C2", "Category 2");
		CourseCategoryDAO cat3 = new CourseCategoryDAO(CourseGroup.CategoryType.RM, "C3", "Category 3");

		courseDao.save(cat1);
		courseDao.save(cat2);
		courseDao.save(cat3);

		// First course group.
		CourseGroupDAO newCourseGroup1 = courseDao.newCourseGroup("id1", "Title", "Department", "Subunit");
		newCourseGroup1.setSource("source");

		newCourseGroup1.getCategories().add(cat1);
		newCourseGroup1.getCategories().add(cat2);

		courseDao.save(newCourseGroup1);

		// Second course group.
		CourseGroupDAO newCourseGroup2 = courseDao.newCourseGroup("id2", "Title", "Department", "Subunit");
		newCourseGroup2.setSource("source");
		newCourseGroup2.setDeleted(true);

		newCourseGroup2.getCategories().add(cat1);
		newCourseGroup2.getCategories().add(cat2);
		newCourseGroup2.getCategories().add(cat3);

		courseDao.save(newCourseGroup2);

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		// Check all is as it should be
		CourseGroupDAO daoA = courseDao.findCourseGroupById("id1");
		assertEquals(2, daoA.getCategories().size());

		CourseGroupDAO daoB = courseDao.findCourseGroupById("id2");
		assertEquals(3, daoB.getCategories().size());

		// now delete a group
		courseDao.deleteSelectedCourseGroups("source");
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		// and the checks
		CourseGroupDAO gA = courseDao.findCourseGroupById("id1");
		assertEquals(2, gA.getCategories().size());

		CourseGroupDAO gB = courseDao.findCourseGroupById("id2");
		assertNull(gB);

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
				end, format.format(end), "location 1");
		CourseComponentSessionDAO ss2 = new CourseComponentSessionDAO("2",
				start, format.format(start),
				end, format.format(end), "location 2");
		CourseComponentSessionDAO ss3 = new CourseComponentSessionDAO("3",
				start, format.format(start),
				end, format.format(end), "location 3");

		// Create a component.
		CourseComponentDAO courseComponent = courseDao.newCourseComponent("test");
		courseComponent.setSource("source");
		courseComponent.getComponentSessions().add(ss1);
		courseDao.save(courseComponent);

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		// Check all is as it should be
		courseComponent  = courseDao.findCourseComponent("test");
		Iterator<CourseComponentSessionDAO> sessionsIt = courseComponent.getComponentSessions().iterator();
		assertTrue("Expected at least one session", sessionsIt.hasNext());
		assertEquals("location 1", sessionsIt.next().getLocation());
		assertFalse("Should be no more than one session", sessionsIt.hasNext());

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

	private final Date END_MIC_2010 = createDate(2010, 12, 4);

	public void testAvailableCourses() {
		// Create the course group
		CourseGroupDAO group1 = courseDao.newCourseGroup("course-1", "title", "dept", "subunit");
		courseDao.save(group1);
		// Create the course component
		CourseComponentDAO comp1 = courseDao.newCourseComponent("comp1");
		comp1.setCloses(createDate(2012, 12, 4)); // END_MIC_2012
		comp1.getGroups().add(group1);
		courseDao.save(comp1);
		// Create the course component
		CourseComponentDAO comp2 = courseDao.newCourseComponent("comp2");
		comp2.setCloses(createDate(2011, 12, 4)); // END_MIC_2011
		comp2.getGroups().add(group1);
		courseDao.save(comp2);
		// Create the out of range course component
		CourseComponentDAO comp3 = courseDao.newCourseComponent("comp3");
		comp3.setCloses(createDate(2009, 12, 4)); // END_MIC_2009
		comp3.getGroups().add(group1);
		courseDao.save(comp3);

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		CourseGroupDAO course = courseDao.findUpcomingComponents("course-1", END_MIC_2010);
		assertNotNull(course);
		assertNotNull(course.getComponents());
		// Although we have 3 components in the DB because we preloaded based on the close date this gives
		// us just 2.
		assertEquals(2, course.getComponents().size());
	}

	public void testCoursesInDept() {
		CourseGroupDAO wrongDept = courseDao.newCourseGroup("wrong-dept", "Wrong Department", "4B03", null);
		wrongDept.setVisibility("PB");
		courseDao.save(wrongDept);
		CourseComponentDAO wrongDeptComp = courseDao.newCourseComponent("wrong-dept-comp");
		wrongDeptComp.setBaseDate(createDate(2012, 12, 4));
		wrongDeptComp.getGroups().add(wrongDept);
		courseDao.save(wrongDeptComp);

		CourseGroupDAO tooOld = courseDao.newCourseGroup("too-old", "Too Old", "3C05", null);
		tooOld.setVisibility("PB");
		courseDao.save(tooOld);
		CourseComponentDAO tooOldComp = courseDao.newCourseComponent("too-old-comp");
		tooOldComp.setBaseDate(createDate(2009, 1, 1));
		tooOldComp.getGroups().add(tooOld);
		courseDao.save(tooOldComp);

		CourseGroupDAO justRight = courseDao.newCourseGroup("just-right", "Just Right", "3C05", null);
		justRight.setVisibility("PB");
		courseDao.save(justRight);
		CourseComponentDAO justRightComp = courseDao.newCourseComponent("just-right-comp");
		justRightComp.setBaseDate(createDate(2011, 12, 2));
		justRightComp.getGroups().add(justRight);
		courseDao.save(justRightComp);

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		List<CourseGroupDAO> groups = courseDao.findCourseGroupByDept("3C05", Range.UPCOMING, END_MIC_2010, false);
		assertEquals(1, groups.size());
	}

	public void testCountSignups() {

		// Create some dates
		Date now = createDate(2010, 3, 1);
		Date beforeNow = createDate(2009, 3, 1);
		Date afterNow = createDate(2011, 3, 1);

		// First check for non-existant course
		assertEquals(0, courseDao.countSignupByCourse("groupId", Collections.singleton(Status.ACCEPTED), now).intValue());

		// Create a course
		CourseGroupDAO aCourse = courseDao.newCourseGroup("groupId", "Title", "dept", "subunit");
		courseDao.save(aCourse);

		// Create a component in the past
		CourseComponentDAO oldComponent = courseDao.newCourseComponent("oldComponentId");
		oldComponent.setStarts(beforeNow);
		oldComponent.getGroups().add(aCourse);
		courseDao.save(oldComponent);

		// Create a signup to the old component
		CourseSignupDAO oldSignup = courseDao.newSignup("aUserId", "aSupervisorId", new Date());
		oldSignup.setGroup(aCourse);
		oldSignup.setStatus(Status.ACCEPTED);
		courseDao.save(oldSignup);
		oldComponent.getSignups().add(oldSignup);
		courseDao.save(oldComponent);

		assertEquals(0, courseDao.countSignupByCourse("groupId", Collections.singleton(Status.ACCEPTED), now).intValue());

		// Create a component in the future
		CourseComponentDAO newComponent = courseDao.newCourseComponent("newComponentId");
		newComponent.setStarts(afterNow);
		newComponent.getGroups().add(aCourse);
		courseDao.save(newComponent);

		assertEquals(0, courseDao.countSignupByCourse("groupId", Collections.singleton(Status.ACCEPTED), now).intValue());

		// Create a signup for the future component
		CourseSignupDAO newSignup = courseDao.newSignup("aUserId", "aSupervisorId", new Date());
		newSignup.setGroup(aCourse);
		newSignup.setStatus(Status.ACCEPTED);
		courseDao.save(newSignup);
		newComponent.getSignups().add(newSignup);
		courseDao.save(newComponent);

		assertEquals(1, courseDao.countSignupByCourse("groupId", Collections.singleton(Status.ACCEPTED), now).intValue());

	}

//	public void testAdminCourseGroups() {
//		List<CourseGroupDAO> groups = courseDao.findAdminCourseGroups("d86d9720-eba4-40eb-bda3-91b3145729da");
//		assertEquals(3, groups.size());
//		groups = courseDao.findAdminCourseGroups("c10cdf4b-7c10-423c-8319-2d477051a94e");
//		assertEquals(1, groups.size());
//	}
//
//	public void testFindSignupByCourse() {
//		List<CourseSignupDAO> signups = courseDao.findSignupByCourse("d86d9720-eba4-40eb-bda3-91b3145729da", "course-1", null);
//		assertEquals(1,signups.size());
//	}

	private static Date createDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return new Date(cal.getTimeInMillis());
	}
}
