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
 * Base exception class for course signup.
 * In most cases you create a more specific instance.
 *
 * @author buckett
 */
public class CourseSignupException extends RuntimeException {

	private static final long serialVersionUID = 3523664446891089880L;

	/**
	 * @link RuntimeException#RuntimeException(String, Throwable)
	 */
	public CourseSignupException(String message, Throwable t) {
		super(message, t);
	}

	protected CourseSignupException() {
		super();
	}

}
