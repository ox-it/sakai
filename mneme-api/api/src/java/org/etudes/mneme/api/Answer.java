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
 * Answer is the user's response to one question of an assessment.
 */
public interface Answer
{
	/**
	 * Access the answer's automatic scoring value - the sum of the auto scores of the entries.
	 * 
	 * @return The answer's auto-score, or null if the answer's question type does not auto-score.
	 */
	Float getAutoScore();

	/**
	 * Access the evaluation for the answer.
	 * 
	 * @return The evaluation for the answer, or null if there is none.
	 */
	AnswerEvaluation getEvaluation();

	/**
	 * Access the Answer id.
	 * 
	 * @return The Answer id.
	 */
	String getId();

	/**
	 * Check if the question is answered; if the user has made the answer entries. Answers that have only "mark for review" or a rational are not
	 * considered answered.
	 * 
	 * @return TRUE if the question is considered to be answered, FALSE if not.
	 */
	Boolean getIsAnswered();

	/**
	 * Check if this answer has been changed by a setter.
	 * 
	 * @return TRUE if changed, FALSE if not.
	 */
	Boolean getIsChanged();

	/**
	 * Check if the question is marked as complete.
	 * 
	 * @return TRUE if the question is marked as complete, FALSE if not.
	 */
	Boolean getIsComplete();

	/**
	 * Access the "mark for review" setting for this answer.
	 * 
	 * @return The answer's "mark for review" setting; TRUE if the user has marked this answer for review, FALSE if not.
	 */
	Boolean getMarkedForReview();

	/**
	 * Access the assessment question that this is an answer to.
	 * 
	 * @return The answer's assessment question.
	 */
	Question getQuestion();

	/**
	 * Access the reason text, if any, provided as part of this answer.
	 * 
	 * @return The answer's reason text, or null if there is none.
	 */
	String getReason();

	/**
	 * Check if this answer should show correct answer in review, based on the assessment setting and answer correctness.
	 * 
	 * @return TRUE if the answer should show correct answer in review, FALSE if not.
	 */
	Boolean getShowCorrectReview();

	/**
	 * Check if this answer should show correct answer in review, based on the assessment setting and partial answer correctness.
	 * 
	 * @return TRUE if the answer should show correct answer in review, FALSE if not.
	 */
	Boolean getShowPartialCorrectReview();

	/**
	 * Access the back pointer to the submission.
	 * 
	 * @return The the back pointer to the submission.
	 */
	Submission getSubmission();

	/**
	 * Access the latest time that this answer was submitted.
	 * 
	 * @return The answer's submitted date.
	 */
	Date getSubmittedDate();

	/**
	 * Access the total score of the answer - the total of the auto score and the evaluation score.<br />
	 * Returns null if there is no auto score and no evaluation score.
	 * 
	 * @return The total score of the answer, or null if there is none.
	 */
	Float getTotalScore();

	/**
	 * Get the question-type-specific handler for the answer.
	 * 
	 * @return The question-type-specific handler for the answer.
	 */
	TypeSpecificAnswer getTypeSpecificAnswer();

	/**
	 * Set the "mark for review" setting for this answer.
	 * 
	 * @param forReview
	 *        The answer's "mark for review" setting; TRUE if the user has marked this answer for review, FALSE if not.
	 */
	void setMarkedForReview(Boolean forReview);

	/**
	 * Set the reason text, if any, provided as part of this answer.
	 * 
	 * @param reason
	 *        The answer's reason text, or null if there is none. Must be well formed HTML or plain text.
	 */
	void setReason(String reason);

	/**
	 * Set the latest time that this answer was submitted.
	 * 
	 * @param submitted
	 *        The answer's submitted date.
	 */
	void setSubmittedDate(Date submitted);

	/**
	 * Set the total score of the answer. The Evaluation will be adjusted so that this becomes the total score given the current answer auto score.
	 * 
	 * @param score
	 *        The new total score desired for the answer.
	 */
	void setTotalScore(Float score);
}
