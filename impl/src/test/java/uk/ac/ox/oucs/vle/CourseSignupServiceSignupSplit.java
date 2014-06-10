package uk.ac.ox.oucs.vle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ox.oucs.vle.proxy.SakaiProxyTest;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Test the splitting of a signup.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:/course-signup-beans.xml", "classpath:/test-with-h2.xml"})

public class CourseSignupServiceSignupSplit extends AbstractTransactionalSpringContextTests {

//	@Autowired
	CourseSignupService courseSignupService;

//	@Autowired
	CourseDAOImpl dao;

//	@Autowired
	SakaiProxyTest sakaiProxyTest;

//	@Autowired
	HibernateTransactionManager transactionManager;

	// Our transaction.
	private TransactionStatus transaction;
	private String signupId;

	@PostConstruct
	public void preLoad() {
		TransactionStatus data = transactionManager.getTransaction(null);
		CourseGroupDAO courseGroupDAO = dao.newCourseGroup("groupId", "title", "dept", null);
		courseGroupDAO.getAdministrators().add("adminId");

		CourseComponentDAO comp1 = dao.newCourseComponent("compId1");
		dao.save(comp1);

		CourseComponentDAO comp2 = dao.newCourseComponent("compId2");
		dao.save(comp2);

		courseGroupDAO.getComponents().add(comp1);
		courseGroupDAO.getComponents().add(comp2);

		dao.save(courseGroupDAO);

		CourseSignupDAO signup = dao.newSignup("userId", "supervisorId", new Date());
		signup.setStatus(CourseSignupService.Status.PENDING);
		signup.setGroup(courseGroupDAO);
		signupId = dao.save(signup);

		comp1.getSignups().add(signup);
		comp2.getSignups().add(signup);
		dao.save(comp1);
		dao.save(comp2);
		transactionManager.commit(data);
	}

//	@Before
//	public void setUp() {
//		transaction = transactionManager.getTransaction(null);
//		UserProxy courseAdmin = Mockito.mock(UserProxy.class);
//		Mockito.when(courseAdmin.getId()).thenReturn("adminId");
//		sakaiProxyTest.setCurrentUser(courseAdmin);
//	}

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        transaction = this.transactionManager.getTransaction(null);
        UserProxy courseAdmin = Mockito.mock(UserProxy.class);
        Mockito.when(courseAdmin.getId()).thenReturn("adminId");
        sakaiProxyTest.setCurrentUser(courseAdmin);
    }

    @Override
    protected void onTearDown() throws Exception {
        super.onTearDown();
        transactionManager.rollback(transaction);
    }

    @Override
    protected String[] getConfigPaths() {
//        return new String[]{"/course-signup-beans.xml", "/sakai-beans.xml", "/course-dao.xml", "/test-with-h2.xml"};
//        return new String[]{ "/course-dao.xml", "/test-with-h2.xml"};
        return new String[]{"/course-dao.xml", "/test-with-h2.xml", "/course-signup-beans.xml"};
    }

    @After
//	public void tearDown() {
//		transactionManager.rollback(transaction);
//	}

	@Test
	public void testSplit() {
		Set<String> newSignupComps = Collections.singleton("compId1");
		String newSignupId = courseSignupService.split(signupId, newSignupComps);
		// We haven't had are arguments modified.
		assertEquals(1, newSignupComps.size());
		dao.flushAndClear();
		CourseSignupDAO originalSignup = dao.findSignupById(signupId);
		CourseSignupDAO newSignup = dao.findSignupById(newSignupId);

		assertNotNull(newSignupId);

		// Check stuff got copied across
		assertEquals(originalSignup.getUserId(), newSignup.getUserId());
		assertEquals(originalSignup.getMessage(), newSignup.getMessage());
		assertEquals(originalSignup.getGroup(), newSignup.getGroup());
		assertEquals(originalSignup.getStatus(), newSignup.getStatus());
		assertEquals(originalSignup.getSupervisorId(), newSignup.getSupervisorId());

		// Check components are correct.
		assertEquals(1, originalSignup.getComponents().size());
		assertEquals(1, newSignup.getComponents().size());

		CourseComponentDAO comp1 = dao.findCourseComponent("compId1");
		assertEquals(1, comp1.getSignups().size());
		CourseComponentDAO comp2 = dao.findCourseComponent("compId2");
		assertEquals(1, comp2.getSignups().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSplitAll() {
		// We can't leave the old signup empty of components.
		Set all = new HashSet<String>();
		all.add("compId1");
		all.add("compId2");
		courseSignupService.split(signupId, all);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSplitNoComponents() {
		courseSignupService.split(signupId, Collections.<String>emptySet());
	}

	@Test(expected = NotFoundException.class)
	public void testSplitBadSignup() {
		courseSignupService.split("notGoodId", Collections.singleton("something"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSplitNotComponent() {
		courseSignupService.split(signupId, Collections.singleton("compId3"));
	}

	@Test(expected = PermissionDeniedException.class)
	public void testSplitWrongUser() {
		UserProxy nonAdmin = Mockito.mock(UserProxy.class);
		Mockito.when(nonAdmin.getId()).thenReturn("notAdmin");
		sakaiProxyTest.setCurrentUser(nonAdmin);
		courseSignupService.split(signupId, Collections.singleton("compId1"));
	}

}
