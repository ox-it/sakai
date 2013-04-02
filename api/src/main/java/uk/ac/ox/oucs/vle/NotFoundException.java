package uk.ac.ox.oucs.vle;

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

/**
 * This error is thrown by the service when a request is made for
 * something that doesn't exsits. Having runtime errors 
 * means we can handle them well above the service layer but don't
 * need lots of declarations.
 * @author buckett
 *
 */
public class NotFoundException extends CourseSignupException {
	
	private static final long serialVersionUID = -8240382294176253690L;
	
	private String id;

	public NotFoundException(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}
