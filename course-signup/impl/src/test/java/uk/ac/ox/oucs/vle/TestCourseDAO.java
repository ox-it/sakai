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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static uk.ac.ox.oucs.vle.CourseSignupService.Range;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/course-dao.xml", "/test-with-h2.xml"})
@Transactional
public class TestCourseDAO extends Assert {

	@Autowired
	private CourseDAOImpl courseDao;
	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void testUpdatingAdministrators() {
		CourseGroupDAO dao;
		dao = courseDao.newCourseGroup("id", "Title", "Department", "Subunit");
		dao.setSource("source");
		dao.getAdministrators().clear();
		dao.getAdministrators().add("1234");
		dao.getAdministrators().add("5678");
		courseDao.save(dao);

		CourseGroupDAO dao2;
		dao2 = courseDao.newCourseGroup("id2", "Title", "Department", "Subunit");
		dao2.setSource("source");
		dao2.getAdministrators().clear();
		dao2.getAdministrators().add("1234");
		dao2.getAdministrators().add("5678");
		courseDao.save(dao2);

		courseDao.flushAndClear();

		dao = courseDao.findCourseGroupById("id");
		assertFalse(dao.getAdministrators().isEmpty());

		dao2 = courseDao.findCourseGroupById("id2");
		dao2.getAdministrators().clear();
		dao2.getAdministrators().add("abcd");
		courseDao.save(dao2);

		courseDao.flushAndClear();

		dao2 = courseDao.findCourseGroupById("id2");

		assertFalse(dao2.getAdministrators().isEmpty());

	}

	/**
	 * This checks that when you delete a component without a signup only that component gets deleted.
	 * This came about through WL-2645.
	 */

	@Test
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

	@Test
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

	@Test
    public void testComponentFilter() {
        CourseGroupDAO group = courseDao.newCourseGroup("id1", "Title", "Department", "Subunit");
        courseDao.save(group);
        CourseComponentDAO oldComponent = courseDao.newCourseComponent("old");
        oldComponent.setBaseDate(createDate(2009, 1, 1));
        oldComponent.getGroups().add(group);
        courseDao.save(oldComponent);
        CourseComponentDAO newComponent = courseDao.newCourseComponent("new");
        newComponent.setBaseDate(createDate(2020, 1, 1));
        newComponent.getGroups().add(group);
        courseDao.save(newComponent);
        // We only show a few years of previous courses.
        CourseComponentDAO veryOldComponent = courseDao.newCourseComponent("very-old");
        veryOldComponent.setBaseDate(createDate(2000, 1, 1));
        veryOldComponent.getGroups().add(group);
        courseDao.save(veryOldComponent);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        List<CourseComponentDAO> returned;
        // Check we can find all of them
        returned = courseDao.findCourseComponents("id1", Range.ALL, createDate(2010, 1, 1));
        assertNotNull(returned);
        assertEquals(3, returned.size());

        // Check we can just get the newer ones.
        returned = courseDao.findCourseComponents("id1", Range.UPCOMING, createDate(2010, 1, 1));
        assertNotNull(returned);
        assertEquals(1, returned.size());

        // Check we can just get the older ones.
        returned = courseDao.findCourseComponents("id1", Range.PREVIOUS, createDate(2010, 1, 1));
        assertNotNull(returned);
        assertEquals(1, returned.size());

    }


	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
	public void testFindCourseGroupById() {
		// This test was to try and reproduce problems I was having with the findCourseGroupById getting confused
		// between the column aliases in the SQL and the aliases in the result set. I couldn't reproduce the error
		// in a test but it's worth leaving the test in to check it doesn't get any worse.
		CourseCategoryDAO category = new CourseCategoryDAO(CourseGroup.CategoryType.RDF, "C1", "Test");
		courseDao.save(category);
		// Create a course
		CourseGroupDAO aCourse = courseDao.newCourseGroup("groupId", "Title", "dept", "subunit");
		aCourse.getCategories().add(category);
		courseDao.save(aCourse);
		// Create a component
		// Create a component in the future
		CourseComponentDAO newComponent = courseDao.newCourseComponent("newComponentId");
		newComponent.getGroups().add(aCourse);
		courseDao.save(newComponent);

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		assertNotNull(courseDao.findCourseComponents("groupId", Range.ALL, new Date()));
	}

	@Test
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

	@Test
	public void testFindComponentSignups() {
		CourseGroupDAO groupA = courseDao.newCourseGroup("groupA", "Title Group A", "dept", "subunit");
		courseDao.save(groupA);
		CourseGroupDAO groupB = courseDao.newCourseGroup("groupB", "Title Group A", "dept", "subunit");
		courseDao.save(groupB);

		CourseSignupDAO signupW = courseDao.newSignup("userW", null, new Date());
		signupW.setStatus(Status.ACCEPTED);
		signupW.setGroup(groupA);
		courseDao.save(signupW);
		CourseSignupDAO signupX = courseDao.newSignup("userX", null, new Date());
		signupX.setStatus(Status.APPROVED);
		signupX.setGroup(groupA);
		courseDao.save(signupX);
		CourseSignupDAO signupY = courseDao.newSignup("userY", null, new Date());
		signupY.setStatus(Status.PENDING);
		signupY.setGroup(groupB);
		courseDao.save(signupY);
		CourseSignupDAO signupZ = courseDao.newSignup("userZ", null, new Date());
		signupZ.setStatus(Status.ACCEPTED);
		signupZ.setGroup(groupB);
		courseDao.save(signupZ);

		CourseComponentDAO component1 = courseDao.newCourseComponent("component1");
		component1.setStarts(createDate(2015, 12, 1));
		component1.getGroups().add(groupA);
		component1.getSignups().add(signupW);
		component1.getSignups().add(signupX);
		courseDao.save(component1);
		CourseComponentDAO component2 = courseDao.newCourseComponent("component2");
		component2.setStarts(createDate(2016, 12, 1));
		component2.getGroups().add(groupB);
		component2.getSignups().add(signupY);
		component2.getSignups().add(signupZ);
		courseDao.save(component2);

		courseDao.flushAndClear();

		List<Map> signups;
		signups = courseDao.findComponentSignups(null, null, null);

		assertEquals(4, signups.size());
		// Check ordering, same component first, then groupId.
		assertEquals("userZ", ((CourseSignupDAO)signups.get(0).get("signup")).getUserId());
		assertEquals("component2", ((CourseComponentDAO)signups.get(0).get("this")).getPresentationId());
		assertEquals("userY", ((CourseSignupDAO)signups.get(1).get("signup")).getUserId());
		assertEquals("component2", ((CourseComponentDAO)signups.get(1).get("this")).getPresentationId());
		assertEquals("userX", ((CourseSignupDAO)signups.get(2).get("signup")).getUserId());
		assertEquals("component1", ((CourseComponentDAO)signups.get(2).get("this")).getPresentationId());
		assertEquals("userW", ((CourseSignupDAO)signups.get(3).get("signup")).getUserId());
		assertEquals("component1", ((CourseComponentDAO)signups.get(2).get("this")).getPresentationId());

		signups = courseDao.findComponentSignups(null, Collections.singleton(Status.PENDING), null);
		assertEquals(1, signups.size());

		signups = courseDao.findComponentSignups("component1", null, null);
		assertEquals(2, signups.size());
		Collection<String> component1UserIds = Arrays.asList(new String[]{"userW", "userX"});
		for(Map signup: signups) {
			String signupId = ((CourseSignupDAO) signup.get("signup")).getUserId();
			if (!component1UserIds.contains(signupId)) {
				fail("Incorrect signup found: "+ signupId);
			}
		}

		signups =  courseDao.findComponentSignups(null, Collections.singleton(Status.ACCEPTED), 2016);
		assertEquals(1, signups.size());
		assertEquals("userZ", ((CourseSignupDAO)signups.get(0).get("signup")).getUserId());

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
