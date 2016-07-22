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

/**
 * GradingOptions contain the details of how submissions are graded.
 */
public interface AssessmentGrading
{
	/**
	 * Check if student identities are invisible to the grader when grading.
	 * 
	 * @return TRUE if student identities are invisible to the grader when grading, FALSE if not.
	 */
	Boolean getAnonymous();

	/**
	 * Check if submissions are to be considered graded as soon as submitted (based only on the auto-scoring).
	 * 
	 * @return TRUE if the submissions are considered graded on submission, FALSE for manual grading.
	 */
	Boolean getAutoRelease();

	/**
	 * Check if grades are to be sent to the Gradebook application.
	 * 
	 * @return TRUE if the assessment's grades are to be placed into the Gradebook, FALSE if not.
	 */
	Boolean getGradebookIntegration();

	/**
	 * Check if the assessment is rejected from entry into the gradebook.
	 * 
	 * @return TRUE if the assessment is rejected from entry into the gradebook, FALSE if not.
	 */
	Boolean getGradebookRejectedAssessment();

	/**
	 * Check if the grading options are valid; i.e. has not been rejected by the grades service if that option was enabled.
	 * 
	 * @return TRUE if the assessment dates are valid, FALSE if not.
	 */
	Boolean getIsValid();

	/**
	 * Set if student identities are invisible to the grader when grading.
	 * 
	 * @param setting
	 *        TRUE if student identities are invisible to the grader when grading, FALSE if not.
	 */
	void setAnonymous(Boolean setting);

	/**
	 * Set if submissions are to be considered graded as soon as submitted (based only on the auto-scoring).<br />
	 * If changed to FALSE, the assessment's submissions are retracted (un-released) when the assessment is saved.
	 * 
	 * @param setting
	 *        TRUE if the submissions are considered graded on submission, FALSE for manual grading.
	 */
	void setAutoRelease(Boolean setting);

	/**
	 * Set if grades are to be sent to the Gradebook application.
	 * 
	 * @param setting
	 *        TRUE if the assessment's grades are to be placed into the Gradebook, FALSE if not.
	 */
	void setGradebookIntegration(Boolean setting);
}
