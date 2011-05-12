/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.api;

import java.util.Date;
import java.util.List;

/**
 * AssessmentAccess holds details of a single special access grant for select users to an assessment.
 */
public interface AssessmentAccess
{
	/**
	 * Access the date after which submissions will not be accepted.<br />
	 * If null, no late submissions are accepted.
	 * 
	 * @return The date after which submissions will not be accepted, or null if there is none.
	 */
	Date getAcceptUntilDate();

	/**
	 * Access the due date. Submissions after this date are considered late, if they are accepted at all.
	 * 
	 * @return The due date, or null if there is none.
	 */
	Date getDueDate();

	/**
	 * Check if we have a time limit.
	 * 
	 * @return TRUE if a time limit is defined, FALSE if not.
	 */
	Boolean getHasTimeLimit();

	/**
	 * Check if we have a tries limit.
	 * 
	 * @return TRUE if a tries limit is defined, FALSE if not.
	 */
	Boolean getHasTriesLimit();

	/**
	 * Access the id.
	 * 
	 * @return The id.
	 */
	String getId();

	/**
	 * @return TRUE if the access is valid, FALSE if not.
	 */
	Boolean getIsValid();

	/**
	 * Access the open date. Only after this date (if defined) is the assessment open for submission.<br />
	 * If null, the test is open when it is published.
	 * 
	 * @return The assessment's open date, or null if there is none.
	 */
	Date getOpenDate();

	/**
	 * Check if we override the AcceptUntil date.
	 * 
	 * @return TRUE if we override the AcceptUntil date, FALSE if not.
	 */
	Boolean getOverrideAcceptUntilDate();

	/**
	 * Check if we override the due date.
	 * 
	 * @return TRUE if we override the due date, FALSE if not.
	 */
	Boolean getOverrideDueDate();

	/**
	 * Check if we override the open date.
	 * 
	 * @return TRUE if we override the open date, FALSE if not.
	 */
	Boolean getOverrideOpenDate();

	/**
	 * Check if we override the password.
	 * 
	 * @return TRUE if we override the password, FALSE if not.
	 */
	Boolean getOverridePassword();

	/**
	 * Check if we override the TimeLimit.
	 * 
	 * @return TRUE if we override the TimeLimit, FALSE if not.
	 */
	Boolean getOverrideTimeLimit();

	/**
	 * Check if we override the Tries.
	 * 
	 * @return TRUE if we override the Tries, FALSE if not.
	 */
	Boolean getOverrideTries();

	/**
	 * Access the special password.
	 * 
	 * @return The special password.
	 */
	AssessmentPassword getPassword();

	/**
	 * Access the password text.
	 * 
	 * @return The password text.
	 */
	String getPasswordValue();

	/**
	 * Access the special time limit for taking the assessment (ms).
	 * 
	 * @return The special time limit for taking the assessment (ms), or null if it is untimed.
	 */
	Long getTimeLimit();

	/**
	 * Access the special number of submissions allowed, if not unlimited.
	 * 
	 * @return The special number of submissions allowed, or null if unlimited.
	 */
	Integer getTries();

	/**
	 * Access the list of users for which this access applies.
	 * 
	 * @return The List of user ids for which this access applies.
	 */
	List<String> getUsers();

	/**
	 * Check if this access applies to this user.
	 * 
	 * @param userId
	 *        The user id to check.
	 * @return TRUE if the access applies to this user, FALSE if not.
	 */
	Boolean isForUser(String userId);

	/**
	 * Set the accept until date.
	 * 
	 * @param date
	 *        The accept until date, or null if there is none.
	 */
	void setAcceptUntilDate(Date date);

	/**
	 * Set the due date.
	 * 
	 * @param date
	 *        The due date, or null if there is none.
	 */
	void setDueDate(Date date);

	/**
	 * An alternate way to clear the time limit if set to false.
	 * 
	 * @param hasTimeLimit
	 *        if FALSE, clear the time limit.
	 */
	void setHasTimeLimit(Boolean hasTimeLimit);

	/**
	 * An alternate way to clear the tries limit if set to false.
	 * 
	 * @param hasTriesLimit
	 *        if FALSE, clear the tries.
	 */
	void setHasTriesLimit(Boolean hasTriesLimit);

	/**
	 * Set the open date.
	 * 
	 * @param date
	 *        The open date, or null if there is none.
	 */
	void setOpenDate(Date date);

	/**
	 * Set the override of the AcceptUntil date.
	 * 
	 * @param override
	 *        TRUE to override the AcceptUntil date, FALSE to not.
	 */
	void setOverrideAcceptUntilDate(Boolean override);

	/**
	 * Set the override of the due date.
	 * 
	 * @param override
	 *        TRUE to override the due date, FALSE to not.
	 */
	void setOverrideDueDate(Boolean override);

	/**
	 * Set the override of the open date.
	 * 
	 * @param override
	 *        TRUE to override the open date, FALSE to not.
	 */
	void setOverrideOpenDate(Boolean override);

	/**
	 * Set the override of the password.
	 * 
	 * @param override
	 *        TRUE to override the password, FALSE to not.
	 */
	void setOverridePassword(Boolean override);

	/**
	 * Set the override of the TimeLimit.
	 * 
	 * @param override
	 *        TRUE to override the TimeLimit, FALSE to not.
	 */
	void setOverrideTimeLimit(Boolean override);

	/**
	 * Set the override of the Tries.
	 * 
	 * @param override
	 *        TRUE to override the Tries, FALSE to not.
	 */
	void setOverrideTries(Boolean override);

	/**
	 * Set the password text.
	 * 
	 * @param password
	 *        The password text.
	 */
	void setPasswordValue(String password);

	/**
	 * Set the time limit for taking the assessment (ms).
	 * 
	 * @param limit
	 *        The time limit for the assessment, or null for unlimited.
	 */
	void setTimeLimit(Long limit);

	/**
	 * Set the special number of submissions allowed for limited submissions.
	 * 
	 * @param count
	 *        The special number of submissions allowed, or null to make it unlimited.
	 */
	void setTries(Integer count);

	/**
	 * Set the users for which this access applies.
	 * 
	 * @param userIds
	 *        The List of user ids for which this access applies.
	 */
	void setUsers(List<String> userIds);
}
