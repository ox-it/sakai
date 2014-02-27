/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2013 Etudes, Inc.
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
 * AssessmentReview contain the details of how submissions can be reviewed.
 */
public interface AssessmentReview
{
	/**
	 * Access the review date (for timing=BY_DATE) after which submissions may be reviewed.
	 * 
	 * @return The review date.
	 */
	Date getDate();

	/**
	 * Check if review is enabled right now for this assessment, considering when now is and all the review settings.
	 * 
	 * @return TRUE if feedback is enabled right now, FALSE if not.
	 */
	Boolean getNowAvailable();

	/**
	 * Access the setting that controls if review includes correct-answer based information (checks and x-es, answer key, item scores).
	 * 
	 * @return TRUE to include correct-answer based review, FALSE to not.
	 */
	ReviewShowCorrect getShowCorrectAnswer();

	/**
	 * Access the setting that controls if review includes only incorrect questions.
	 * 
	 * @return TRUE to include only incorrect questions, FALSE to not.
	 */
	ReviewShowCorrect getShowIncorrectQuestions();

	/**
	 * Access setting that controls if review includes the authored correct / incorrect feedback.
	 * 
	 * @return TRUE to include the correct / incorrect feedback in review, FALSE to not.
	 */
	Boolean getShowFeedback();

	/**
	 * Access setting that controls if review includes summary of data visible to students.
	 * 
	 * @return TRUE to include the summary in review, FALSE to not.
	 */
	Boolean getShowSummary();

	/**
	 * Access the timing setting that tells when review can happen.
	 * 
	 * @return The review timing setting.
	 */
	ReviewTiming getTiming();

	/**
	 * Set the review date (for timing=BY_DATE) after which submissions may be reviewed.
	 * 
	 * @param date
	 *        The review date.
	 */
	void setDate(Date date);

	/**
	 * Set the setting that controls if review includes correct-answer based information (checks and xes, answer key, item scores).
	 * 
	 * @param setting
	 *        the setting.
	 */
	void setShowCorrectAnswer(ReviewShowCorrect setting);

	/**
	 * Set the setting that controls if review includes only incorect questions.
	 * 
	 * @param setting
	 *        the setting.
	 */
	void setShowIncorrectQuestions(ReviewShowCorrect setting);

	/**
	 * Set setting that controls if review includes the authored correct / incorrect feedback.
	 * 
	 * @param setting
	 *        TRUE to include the correct / incorrect feedback in review, FALSE to not.
	 */
	void setShowFeedback(Boolean setting);

	/**
	 * Set setting that controls if review includes the summary of data visible to students.
	 * 
	 * @param setting
	 *        TRUE to include the summary of data in review, FALSE to not.
	 */
	void setShowSummary(Boolean setting);

	/**
	 * Set the timing setting that tells when review can happen.
	 * 
	 * @param setting
	 *        The review timing setting.
	 */
	void setTiming(ReviewTiming setting);
}
