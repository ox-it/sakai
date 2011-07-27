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

package org.etudes.mneme.impl;

import java.util.Date;
import java.util.List;

import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Question;

/**
 * AssessmentStorage defines the storage interface for Assessments.
 */
public interface AssessmentStorage
{
	/**
	 * Clear out any mint objects that are old enough to be considered abandoned.
	 * 
	 * @param stale
	 *        The time to compare to the create date; before this they are stale.
	 * @return the List of assessment ids deleted.
	 */
	List<String> clearStaleMintAssessments(Date stale);

	/**
	 * Construct a new Assessment object that is a copy of another.
	 * 
	 * @param assessment
	 *        The assessment to copy.
	 * @return A new Assessment object.
	 */
	AssessmentImpl clone(AssessmentImpl assessment);

	/**
	 * Count the assessments in a context that are not archived.
	 * 
	 * @param context
	 *        The context.
	 * @return The count of assessment defined in a context.
	 */
	Integer countAssessments(String context);

	/**
	 * Check if an assessment by this id exists.
	 * 
	 * @param id
	 *        The assessment id
	 * @return TRUE if the assessment with this id exists, FALSE if not.
	 */
	Boolean existsAssessment(String id);

	/**
	 * Get all the archived assessments in the context.
	 * 
	 * @param context
	 *        The context.
	 * @return The List<Assesment> of all archived assesments in the context, or empty if there are none.
	 */
	List<AssessmentImpl> getArchivedAssessments(String context);

	/**
	 * Access a assessment by id.
	 * 
	 * @param id
	 *        the assessment id.
	 * @return The assessment with this id, or null if not found.
	 */
	AssessmentImpl getAssessment(String id);

	/**
	 * @return the assessments that *may* need to have their results email sent - set for email, not yet sent - but we are not sure if these are closed yet.
	 */
	List<AssessmentImpl> getAssessmentsNeedingResultsEmail();

	/**
	 * Get all the assessments defined in this context, sorted. Does not include archived assessments.
	 * 
	 * @param context
	 *        The context.
	 * @param sort
	 *        The sort specification.
	 * @param publishedOnly
	 *        if TRUE, return only published and valid assessments, else return unpublished and invalid as well.
	 * @return The list of Assessments defined in the context, sorted.
	 */
	List<AssessmentImpl> getContextAssessments(String context, AssessmentService.AssessmentsSort sort, Boolean publishedOnly);

	/**
	 * Get all the assessments defined in this context, that are invalid due to gb integration.
	 * 
	 * @param context
	 *        The context.
	 * @return The list of Assessments defined in the context, that are invalid due to gb integration.
	 */
	List<AssessmentImpl> getContextGbInvalidAssessments(String context);

	/**
	 * Get the earliest open date of assessments in this context.
	 * 
	 * @param context
	 *        The context.
	 * @return If open dates exist for assessment, returns the earliest open date, otherwise returns null.
	 */
	Date getMinStartDate(String context);
	
	/**
	 * Get the latest open date of assessments in this context.
	 * 
	 * @param context
	 *        The context.
	 * @return If open dates exist for assessment, returns the latest open date, otherwise returns null.
	 */
	Date getMaxStartDate(String context);	

	/**
	 * Initialize.
	 */
	void init();

	/**
	 * Set this assessment to be live.
	 * 
	 * @param assessment
	 *        The assessment.
	 */
	void makeLive(Assessment assessment);

	/**
	 * Construct a new Assessment object.
	 * 
	 * @return A new Assessment object.
	 */
	AssessmentImpl newAssessment();

	/**
	 * Remove a assessment from storage.
	 * 
	 * @param assessment
	 *        The assessment to remove.
	 */
	void removeAssessment(AssessmentImpl assessment);

	/**
	 * Remove any draw dependencies on this pool from all live assessments.
	 * 
	 * @param pool
	 *        The pool.
	 */
	void removeDependency(Pool pool);

	/**
	 * Remove any pick dependencies on this question from all live assessments.
	 * 
	 * @param question
	 *        The question.
	 */
	void removeDependency(Question question);

	/**
	 * Save changes made to this assessment.
	 * 
	 * @param assessment
	 *        the assessment to save.
	 */
	void saveAssessment(AssessmentImpl assessment);

	/**
	 * Set the date that the results email was sent.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param date
	 *        The date, or null to indicate not sent.
	 */
	void setResultsSent(String id, Date date);
}
