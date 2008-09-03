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

import java.util.List;

/**
 * GradesService provides integration with external grading authorities for Mneme.
 */
public interface GradesService
{
	/**
	 * Check if this assessment has an entry in the grading authority.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return TRUE if the assessment has an entry in the grading authoity, FALSE if not.
	 */
	Boolean assessmentReported(Assessment assessment);

	/**
	 * Check if the grading authority is available for this context.
	 * 
	 * @param context
	 *        The context.
	 * @return TRUE if the grading authority is available for this context, FALSE if not.
	 */
	Boolean available(String context);

	/**
	 * Make sure the assessment is added to the grading authority.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return TRUE if successful, FALSE if not.
	 * @throws GradesRejectsAssessmentException
	 *         if the assessment was not able to be added to the grading authority.
	 */
	Boolean initAssessmentGrades(Assessment assessment) throws GradesRejectsAssessmentException;

	/**
	 * Report this assessment's grade. All completed "best" submissions that are found are reported.<br />
	 * Replace the entire set of grades for this assessment that are currently reported.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return TRUE if successful, FALSE if not.
	 */
	Boolean reportAssessmentGrades(Assessment assessment);

	/**
	 * Report this submission's grade.<br />
	 * Establish the Assessment with the grading authority if needed.<br />
	 * Replace any grade for this user / assessment that is currently reported.
	 * 
	 * @param submission
	 *        The submission.
	 * @return TRUE if successful, FALSE if not.
	 */
	Boolean reportSubmissionGrade(Submission submission);

	/**
	 * Remove this assessment's grades.<br />
	 * Remove the assessment completely from the grading authority.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return TRUE if successful, FALSE if not.
	 */
	Boolean retractAssessmentGrades(Assessment assessment);

	/**
	 * Remove this submission's grade.<br />
	 * 
	 * @param submission
	 *        The submission.
	 * @return TRUE if successful, FALSE if not.
	 */
	Boolean retractSubmissionGrade(Submission submission);
}
