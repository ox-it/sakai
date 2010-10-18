/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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

import org.sakaiproject.user.api.User;

/**
 * AssessmentService manages assessments (tests, surveys, assignments, etc).
 */
public interface AssessmentService
{
	/**
	 * Sort options for getContextAssessments()
	 */
	enum AssessmentsSort
	{
		cdate_a, cdate_d, ddate_a, ddate_d, odate_a, odate_d, published_a, published_d, title_a, title_d, type_a, type_d
	}

	/**
	 * Check if the user is allowed to edit this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return TRUE if the user is allowed, FALSE if not.
	 */
	Boolean allowEditAssessment(Assessment assessment);

	/**
	 * Check if the user is allowed as a guest.
	 * 
	 * @param context
	 *        The context.
	 * @return TRUE if the user is allowed, FALSE if not.
	 */
	Boolean allowGuest(String context);

	/**
	 * Check if the user is allowed to list delivery assessments in this context.
	 * 
	 * @param context
	 *        The context.
	 * @return TRUE if the user is allowed to list delivery assessments in this context, FALSE if not.
	 */
	Boolean allowListDeliveryAssessment(String context);

	/**
	 * Check if the user is allowed to manage assessments in this context.
	 * 
	 * @param context
	 *        The context.
	 * @return TRUE if the user is allowed to manage assessments in this context, FALSE if not.
	 */
	Boolean allowManageAssessments(String context);

	/**
	 * Check if the assessment may be removed at this time by this user.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return TRUE if the assessment may be removed by this user, FALSE if not.
	 */
	Boolean allowRemoveAssessment(Assessment assessment);

	/**
	 * Check if the user is allowed to setup a formal course evaluation.
	 * 
	 * @param context
	 *        The context.
	 * @return TRUE if the user is allowed, FALSE if not.
	 */
	Boolean allowSetFormalCourseEvaluation(String context);

	/**
	 * Apply base date changes to open, due and accept until dates of this context's unarchived assessments
	 * 
	 * @param context
	 *        The context.
	 * @param days_diff
	 *        The time difference in days
	 */
	void applyBaseDateTx(String context, int days_diff);

	/**
	 * Clear out any mint objects that are old enough to be considered abandoned.
	 */
	void clearStaleMintAssessments();

	/**
	 * Create a new Assessment in the context that is a copy of another.<br />
	 * The new assessment is non-archived and un-published.
	 * 
	 * @param context
	 *        The context in which the assessment lives.
	 * @param assessment
	 *        The assessment to copy.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to create assessments in this context.
	 * @return The new Assessment.
	 */
	Assessment copyAssessment(String context, Assessment assessment) throws AssessmentPermissionException;

	/**
	 * Count the assessments in the context - all of them that are not archived.
	 * 
	 * @param context
	 *        The context.
	 * @return The count of assessments in the context.
	 */
	Integer countAssessments(String context);

	/**
	 * Get all the archived assessments in the context.
	 * 
	 * @param context
	 *        The context.
	 * @return The List<Assesment> of all archived assesments in the context, or empty if there are none.
	 */
	List<Assessment> getArchivedAssessments(String context);

	/**
	 * Access an assessment by id.
	 * 
	 * @param id
	 *        The assessment id.
	 * @return The assessment object, or null if not found.
	 */
	Assessment getAssessment(String id);

	/**
	 * @return the assessments that need to have their results email sent. These are set for results email, are closed, and not yet sent.
	 */
	List<Assessment> getAssessmentsNeedingResultsEmail();

	/**
	 * Get all the assessments for the context, sorted. Does not include archived assessments.
	 * 
	 * @param context
	 *        The context.
	 * @param sort
	 *        The sort specification.
	 * @param publishedOnly
	 *        if TRUE, return only published & valid assessments, else return unpublished or invalid as well.
	 * @return The List<Assessment> of all assessments in the context, sorted, or empty if there are none.
	 */
	List<Assessment> getContextAssessments(String context, AssessmentsSort sort, Boolean publishedOnly);

	/**
	 * Get the earliest open date of assessments in the context.
	 * 
	 * @param context
	 *        The context.
	 * @return If open dates exist for assessment, returns the earliest open date, otherwise returns null.
	 */
	Date getMinStartDate(String context);

	/**
	 * Get a list of Users who can submit in this context.
	 * 
	 * @param context
	 *        The context.
	 * @return a List of Users who can submit in this context, sorted by user sort name.
	 */
	List<User> getSubmitUsers(String context);

	/**
	 * Create a new Assessment in the context.
	 * 
	 * @param context
	 *        The context in which the assessment lives.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to create assessments in this context.
	 * @return The new Assessment.
	 */
	Assessment newAssessment(String context) throws AssessmentPermissionException;

	/**
	 * Remove this assessment.
	 * 
	 * @param assessment
	 *        The assessment to remove.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to remove this assessment.
	 * @throws AssessmentPolicyException
	 *         if the assessment may not be removed due to API policy.
	 */
	void removeAssessment(Assessment assessment) throws AssessmentPermissionException, AssessmentPolicyException;

	/**
	 * Re-compute the scores for all submissions in this assessment, updateing the grading authority if needed.
	 * 
	 * @param assessment
	 *        The assessment to re-score.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to create assessments in this context.
	 * @throws GradesRejectsAssessmentException
	 *         if the assessment has trouble getting back into the gradebook.
	 */
	void rescoreAssessment(Assessment assessment) throws AssessmentPermissionException, GradesRejectsAssessmentException;

	/**
	 * Save changes made to this assessment.
	 * 
	 * @param assessment
	 *        The assessment to save.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to edit this assessment.
	 * @throws AssessmentPolicyException
	 *         if the changes are not allowed to be saved due to policy violation.
	 */
	void saveAssessment(Assessment assessment) throws AssessmentPermissionException, AssessmentPolicyException;

	/**
	 * If the assessment is setup for sending email results, and is closed, send the email.
	 * 
	 * @param assessment
	 *        The assessment.
	 */
	void sendResults(Assessment assessment);

	/**
	 * Set the date that the results email was sent.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param date
	 *        The date, or null to indicate not sent.
	 */
	void setResultsSent(Assessment assessment, Date date);
}
