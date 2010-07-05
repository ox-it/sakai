package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.Date;

import uk.ac.ox.oucs.vle.proxy.SakaiProxy;

public class TestCourseSignupServiceImpl extends TestOnSampleData {
	
	private CourseSignupServiceTest service;
	private CourseDAOImpl dao;
	
	public void setUp() throws Exception {
		super.setUp();
		dao = new CourseDAOImpl();
		dao.setSessionFactory(factory);
		
		service = new CourseSignupServiceTest();
		service.setDao(dao);
		
		service.setProxy(new SakaiProxy());
		service.setNow(new Date(110, 9, 1));
	}
	
	public void testSignup() {
		service.signup(Collections.singleton("comp-1"), "test.user.1@dept.ox.ac.uk", null);
	}

}
