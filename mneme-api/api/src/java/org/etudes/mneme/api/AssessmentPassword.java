/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.api;

/**
 * AssessmentPassword models passwords on assessments.
 */
public interface AssessmentPassword
{
	/**
	 * Check if the provided password (clear text) matches the defined password for the assessment.
	 * 
	 * @param password
	 *        The clear text password as entered.
	 * @return TRUE if the password is a match, false if not.
	 */
	Boolean checkPassword(String password);

	/**
	 * Access the defined password.
	 * 
	 * @return The password, or null if not defined. TODO: part of special access?
	 */
	String getPassword();

	/**
	 * Set the access password.
	 * 
	 * @param password
	 *        The access password, or null to remove it.
	 */
	void setPassword(String password);
}
