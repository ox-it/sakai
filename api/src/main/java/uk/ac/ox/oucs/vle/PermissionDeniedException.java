/*
 * #%L
 * Course Signup API
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

/**
 * This error is thrown by the service when the user attempts todo 
 * something which they aren't allowed todo. Having runtime errors 
 * means we can handle them well above the service layer but don't
 * need lots of declarations.
 * @author buckett
 *
 */
public class PermissionDeniedException extends CourseSignupException {

	private static final long serialVersionUID = 1908495537561080522L;
	
	private String userId;
	
	public PermissionDeniedException(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

}
