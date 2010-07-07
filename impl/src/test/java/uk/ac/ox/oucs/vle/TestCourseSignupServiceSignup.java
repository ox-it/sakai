package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class TestCourseSignupServiceSignup extends TestOnSampleData {
	
	public void testSignupGood() {
		service.signup(Collections.singleton("comp-1"), "test.user.1@dept.ox.ac.uk", null);
	}
	
	public void testSignupGoodSet() {
		Set<String> componentIds = new HashSet<String>();
		componentIds.add("comp-1");
		componentIds.add("comp-2");
		service.signup(componentIds, "test.user.1@dept.ox.ac.uk", null);
	}
	
	public void testSignupMultiple() {
		// TODO This affects all further tests.
		for (int i = 1; i<=15; i++) {
			proxy.setCurrentUser(proxy.findUserById("id"+i));
			service.signup(Collections.singleton("comp-3"), "test.user.1@dept.ox.ac.uk", null);
		}
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
