package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.List;

import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public class TestCourseSignupServiceMySignups extends TestOnSampleData {

	public void testMySignups() {
		List<CourseSignup> signups = service.getMySignups(null);
		assertNotNull(signups);
		assertEquals(2, signups.size());
		CourseSignup accepted = signups.get(0);
		assertEquals(Status.ACCEPTED, accepted.getStatus());
		assertEquals(2, accepted.getComponents().size());
	}
	
	public void testMySignupsStatuses() {
		assertEquals(0, service.getMySignups(Collections.singleton(Status.PENDING)).size());
		assertEquals(0, service.getMySignups(Collections.singleton(Status.WITHDRAWN)).size());
	}
}
