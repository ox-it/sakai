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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;


@Transactional
public class TestCourseSignupServiceSignup extends OnSampleData {

	@Before
	public void resetUser() {
		proxy.setCurrentUser(proxy.findUserById("current"));
	}

	@Test
	public void testSignupGood() {
		service.signup("course-1", Collections.singleton("comp-1"), "test.user.1@dept.ox.ac.uk", null);
	}

	@Test
	public void testSignupGoodSet() {
		Set<String> componentIds = new HashSet<String>();
		componentIds.add("comp-1");
		componentIds.add("comp-3");
		service.signup("course-1", componentIds, "test.user.1@dept.ox.ac.uk", null);
	}

	@Test
	public void testSignupMultipleUsers() {
		for (int i = 1; i<=15; i++) {
			proxy.setCurrentUser(proxy.findUserById("id"+i));
			service.signup("course-1", Collections.singleton("comp-3"), "test.user.1@dept.ox.ac.uk", null);
		}
	}

	@Test
	public void testSignupSingle() {
		service.signup("course-1", Collections.singleton("comp-1"), "test.user.1@dept.ox.ac.uk", null);
		dao.flushAndClear();
		List<CourseSignup> signups = service.getMySignups(Collections.singleton(Status.PENDING));
		assertEquals(1, signups.size());
		try {
			service.signup("course-1", Collections.singleton("comp-1"), "test.user.2@dept.ox.ac.uk", null);
			fail("Shouldn't be able to signup twice.");
		} catch (Exception e) {}
		dao.flushAndClear();
		signups = service.getMySignups(Collections.singleton(Status.PENDING));
		assertEquals("test.user.1@dept.ox.ac.uk", signups.get(0).getSupervisor().getEmail());
	}

	@Test
	public void testSignupWithdrawSignupSingle() {
		service.signup("course-1", Collections.singleton("comp-1"), "test.user.1@dept.ox.ac.uk", null);
		dao.flushAndClear();
		List<CourseSignup> signups = service.getMySignups(Collections.singleton(Status.PENDING));
		assertEquals(1, signups.size());
		service.withdraw(signups.get(0).getId());
		dao.flushAndClear();
		service.signup("course-1", Collections.singleton("comp-1"), "test.user.2@dept.ox.ac.uk", null);
		dao.flushAndClear();
		signups = service.getMySignups(Collections.singleton(Status.PENDING));
		assertEquals("test.user.2@dept.ox.ac.uk", signups.get(0).getSupervisor().getEmail());
	}

	@Test
	public void testSignupFuture() {
		try{
			// Isn't open yet.
			service.signup("course-1", Collections.singleton("comp-5"), "test.user.1@dept.ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalStateException ise) {
			// The course isn't current available for signup.
		}
	}

	@Test
	public void testSignupPast() {
		try {
			// Is now closed.
			service.signup("course-1", Collections.singleton("comp-6"), "test.user.1@dept.ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalStateException ise) {
			// The course isn't currently available for signup.
		}
	}

	@Test
	public void testSignupBadUser() {
		try {
			service.signup("course-1", Collections.singleton("comp-1"), "nosuchuser@ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalArgumentException iae) {
			// The user couldn't be found.
		}
	}

	@Test
	public void testFullCourseToWaitingList() {
		// comp-8 doesn't have any spaces left so the user should end up on the waiting list.
		service.signup("course-1", Collections.singleton("comp-8"), "test.user.1@dept.ox.ac.uk", null);
		List<CourseSignup> signups = service.getMySignups(Collections.singleton(Status.WAITING));
		assertEquals(1, signups.size());
	}

	@Test
	public void testSignupWrongCourse() {
		try {
			service.signup("course-3", Collections.singleton("comp-1"), "test.user.1@dept.ox.ac.uk", null);
			fail("Should throw exception");
		} catch (IllegalArgumentException iae) {
			// We attempted to signup for component that didn't match the course.
		}
	}
}
