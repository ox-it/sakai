/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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

/**
 * AssessmentDates contain the details of the availability dates of an assessment.
 */
public interface AssessmentDates
{
	/**
	 * Access the date after which submissions will not be accepted.<br />
	 * If null, no late submissions are accepted.
	 * 
	 * @return The date after which submissions will not be accepted, or null if there is none.
	 */
	Date getAcceptUntilDate();

	/**
	 * Access the date the assessment was archived.
	 * 
	 * @return The date the assessment was archived, or null if it is not archived.
	 */
	Date getArchivedDate();

	/**
	 * Access the due date. Submissions after this date are considered late, if they are accepted at all.
	 * 
	 * @return The assessment's due date, or null if there is none.
	 */
	Date getDueDate();

	/**
	 * Access the number of ms from now that the due date on this assessment will be reached.
	 * 
	 * @return The number of ms from now that the due date on this assessment will be reached, 0 if it has already been reached, or null if it has no
	 *         due date.
	 */
	Long getDurationTillDue();

	/**
	 * Check if hide until open is set.
	 * 
	 * @return TRUE if hide until open is defined, FALSE if not.
	 */
	Boolean getHideUntilOpen();	
	

	/**
	 * Access the expiration information for the assessment.
	 * 
	 * @return The expiration information for the assessment.
	 */
	Expiration getExpiration();

	/**
	 * Check if the assessment is closed for submissions - unpublished, archived, not yet open or past submit-until date.
	 * 
	 * @return TRUE if closed for submission, FALSE if not.
	 */
	Boolean getIsClosed();

	/**
	 * Check if we are now between due and accept until dates.
	 * 
	 * @return TRUE if we are in the "late but open" period, FALSE if not.
	 */
	Boolean getIsLate();

	/**
	 * Check if the assessment is open for submissions - published, not archived, past open date, before submit-until date.
	 * 
	 * @param withGrace
	 *        TRUE to consider the grace period, FALSE not to.
	 * @return TRUE if open for submission, FALSE if not.
	 */
	Boolean getIsOpen(Boolean withGrace);

	/**
	 * Check if the assessment dates are valid; i.e. has no inconsistencies in the definition.
	 * 
	 * @return TRUE if the assessment dates are valid, FALSE if not.
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
	 * Access the date after which submissions are not allowed. Computed based on due and accept-until dates.
	 * 
	 * @return The date after which submissions are not allowed.
	 */
	Date getSubmitUntilDate();

	/**
	 * Init the hide until open value.
	 * 
	 * @param hideUntilOpen
	 *        The hide until open value.
	 */
	void initHideUntilOpen(Boolean hideUntilOpen);

	/**
	 * Set the date after which submissions will not be accepted.
	 * 
	 * @param date
	 *        The date after which submissions will not be accepted, or null if there is none.
	 */
	void setAcceptUntilDate(Date date);

	/**
	 * Set the due date. Submissions after this date are considered late, if they are accepted at all.
	 * 
	 * @param date
	 *        The assessment's due date, or null if there is none.
	 */
	void setDueDate(Date date);

	/**
	 * Set the hide until open value
	 * 
	 * @param hideUntilOpen
	 *        
	 */
	void setHideUntilOpen(Boolean hideUntilOpen);		

	/**
	 * Set the release date. Only after this date (if defined) is the assessment open for submission.
	 * 
	 * @param date
	 *        The assessment's release date, or null if there is none.
	 */
	void setOpenDate(Date date);
}
