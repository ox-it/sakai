package uk.ac.ox.oucs.vle;

import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

public class HbmTests extends AbstractTransactionalSpringContextTests {

	private CourseDAOImpl courseDao;

	public void onSetUp() throws Exception {
		super.onSetUp();
		courseDao = (CourseDAOImpl) getApplicationContext().getBean("uk.ac.ox.oucs.vle.CourseDAO");
	}
	
	public void onTearDown() throws Exception {
		super.onTearDown();
	} 


	protected String[] getConfigPaths() {
		//return new String[]{"/components.xml", "/test-components.xml"};
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

	public void testDeletingSharedComponent() {
		// First course group.
		CourseGroupDAO newCourseGroup1 = courseDao.newCourseGroup("id1", "Title", "Department", "Subunit");
		newCourseGroup1.setSource("source");
		courseDao.save(newCourseGroup1);

		// Second course group.
		CourseGroupDAO newCourseGroup2 = courseDao.newCourseGroup("id2", "Title", "Department", "Subunit");
		newCourseGroup2.setSource("source");
		newCourseGroup2.setDeleted(true);
		courseDao.save(newCourseGroup2);

		// Create a dummy component.
		CourseComponentDAO newCourseComponent = courseDao.newCourseComponent("test");
		newCourseComponent.getGroups().add(newCourseGroup1);
		newCourseComponent.getGroups().add(newCourseGroup2);
		newCourseComponent.setSource("source");
		courseDao.save(newCourseComponent);

		courseDao.deleteSelectedCourseGroups("source");

		// Check that the first group is still there.
		assertNotNull(courseDao.findCourseGroupById("id1"));
	}
	
	public void testSharedComponent() {
		// First course group.
		CourseGroupDAO newCourseGroup1 = courseDao.newCourseGroup("ida", "Title", "Department", "Subunit");
		newCourseGroup1.setSource("source");
		courseDao.save(newCourseGroup1);

		// Second course group.
		CourseGroupDAO newCourseGroup2 = courseDao.newCourseGroup("idb", "Title", "Department", "Subunit");
		newCourseGroup2.setSource("source");
		courseDao.save(newCourseGroup2);

		// Create a dummy component.
		CourseComponentDAO newCourseComponent = courseDao.newCourseComponent("test");
		newCourseComponent.getGroups().add(newCourseGroup1);
		newCourseComponent.getGroups().add(newCourseGroup2);
		newCourseComponent.setSource("source");
		courseDao.save(newCourseComponent);
		
		// Check the groups both have the component
		CourseGroupDAO daoA = courseDao.findCourseGroupById("ida");
		assertEquals(1, daoA.getComponents().size());
		
		CourseGroupDAO daoB = courseDao.findCourseGroupById("idb");
		assertEquals(1, daoB.getComponents().size());
		
	}


}