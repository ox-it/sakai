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
