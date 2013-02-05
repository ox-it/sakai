package uk.ac.ox.oucs.vle;

import java.util.Collections;
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
	
}