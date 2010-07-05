package uk.ac.ox.oucs.vle;

import java.util.Collections;

public class TestCourseSignupServiceImpl extends TestOnSampleData {
	
	private CourseSignupService service;
	
	public void onSetUp() throws Exception {
		service = (CourseSignupService) getApplicationContext().getBean(CourseSignupService.class.getName());
	}
	
	public void testSignupGood() {
		service.signup(Collections.singleton("comp-1"), "test.user.1@dept.ox.ac.uk", null);
	}
	
	public void testSignupFuture() {
		try{
			// Isn't open yet.
			service.signup(Collections.singleton("comp-5"), "test.user.1@dept.ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalStateException ise) {}
	}
	
	public void testSignupPast() {
		try {
			// Is now closed.
			service.signup(Collections.singleton("comp-6"), "test.user.1@dept.ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalStateException ise) {}
	}
	
	public void testSignupBadUser() {
		try {
			service.signup(Collections.singleton("comp-1"), "nosuchuser@ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalArgumentException iae) {}
	}

	public void testSignupFull() {
		try {
			service.signup(Collections.singleton("comp-8"), "test.user.1@dept.ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalStateException ise) {}
	}
}
