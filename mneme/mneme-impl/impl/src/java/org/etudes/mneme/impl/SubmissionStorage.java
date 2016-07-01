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

import java.util.List;
import java.util.Map;

import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.Submission;

/**
 * SubmissionStorage defines the storage interface for Submissions.
 */
public interface SubmissionStorage
{
	/**
	 * Construct a new Submission object as a copy of another.
	 * 
	 * @param other
	 *        The submission to copy.
	 * @return A new Submission object.
	 */
	SubmissionImpl clone(SubmissionImpl other);

	/**
	 * Find the question ids that have been used in submissions in this assessment part.
	 * 
	 * @param part
	 *        The assessment part.
	 * @return A List of Question ids found used by submissions to this assessment part.
	 */
	List<String> findPartQuestions(Part part);

	/**
	 * Get this answer.
	 * 
	 * @param id
	 *        The answer id.
	 * @return The Answer, or null if not found.s
	 */
	Answer getAnswer(String id);

	/**
	 * Get the official complete submissions to the assignment made by all users.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param sort
	 *        The sort order.
	 * @return A sorted List<Submission> of the submissions for the assessment.
	 */
	List<SubmissionImpl> getAssessmentCompleteSubmissions(Assessment assessment);

	/**
	 * Check if there are any completed submissions that have any null scores for answered answers for this assessment (skip survey questions).
	 * 
	 * @return a list of user ids of the users with unscored submissions to this assessment.
	 */
	List<String> getAssessmentHasUnscoredSubmissions(Assessment assessment);

	/**
	 * Find the highest submission score for each user who has submitted to the assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param releasedOnly
	 *        if TRUE, report only released submissions, else report all completed ones.
	 * @return A Map of keyed by user id to that user's highest Float submission score for the assessment.
	 */
	Map<String, Float> getAssessmentHighestScores(Assessment assessment, Boolean releasedOnly);

	/**
	 * Check if there are any completed submissions that have any null scores for answered answers to this question for this assessment.
	 * 
	 * @return A list of user ids for users with unscored submissions to this assessment.
	 */
	List<String> getAssessmentQuestionHasUnscoredSubmissions(Assessment assessment, Question question);

	/**
	 * Access all the submission scores to this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return A list of the submission scores to this assessment.
	 */
	List<Float> getAssessmentScores(Assessment assessment);

	/**
	 * Get the submissions to the assignment made by all users.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param question
	 *        An optional question, to use for sort-by-score (the score would be for this question in the submission, not the overall).
	 * @return A List<Submission> of the submissions for the assessment.
	 */
	List<SubmissionImpl> getAssessmentSubmissions(Assessment assessment);

	/**
	 * Get all the submissions in this context (non-test-drive, for non-archived assesments).
	 * 
	 * @param context
	 *        The context.
	 * @return The list of submissions in this context.
	 */
	List<? extends Submission> getContextSubmissions(String context);

	/**
	 * Access a set of (complete) submissions from this user for this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param uid
	 *        The user id.
	 * @return The List of submissions, in submit date order.
	 */
	List<SubmissionImpl> getMultipleSubmissions(Assessment assessment, String uid);

	/**
	 * Get all the in-progress (open) submissions (all users, all assessments, all contexts).<br />
	 * Note: the submission answers are not needed nor read in.
	 * 
	 * @return The list of all in-progress (open) submissions.
	 */
	List<SubmissionImpl> getOpenSubmissions();

	/**
	 * Access all the submission scores to this question.
	 * 
	 * @param question
	 *        The question.
	 * @return A list of the submission scores to this question.
	 */
	List<Float> getQuestionScores(Question question);

	/**
	 * Access a submission by id.
	 * 
	 * @param id
	 *        the submission id.
	 * @return The submission with this id, or null if not found.
	 */
	SubmissionImpl getSubmission(String id);

	/**
	 * Find the highest submission score for this user to this assessment (among completed and released submissions).
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param userId
	 *        The user.
	 * @return The highest submission score to this assessment by this user (or null if there are no submissions).
	 */
	Float getSubmissionHighestScore(Assessment assessment, String userId);

	/**
	 * Access this submission's total score.
	 * 
	 * @param submission
	 *        The submission.
	 * @return The total score for this submission.
	 */
	Float getSubmissionScore(Submission submissionImpl);

	/**
	 * Get the submissions to the assignment made by this user.
	 * 
	 * @param assessment
	 *        The assessment to use.
	 * @param userId
	 *        The user id - if null, use the current user.
	 * @return The user's submissions for this assessment.
	 */
	List<SubmissionImpl> getUserAssessmentSubmissions(Assessment assessment, String userId);

	/**
	 * Get all the submissions by this user in this context.
	 * 
	 * @param context
	 *        The context.
	 * @param userId
	 *        The user.
	 * @param publishedOnly
	 *        if TRUE, return only published assessments, else return submissions from unpublished as well.
	 * @return The list of submissions by this user in this context.
	 */
	List<SubmissionImpl> getUserContextSubmissions(String context, String userId, Boolean publishedOnly);

	/**
	 * Get all the users who have submitted to this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return The list of user (ids) who have submitted to this assessment.
	 */
	List<String> getUsersSubmitted(Assessment assessment);

	/**
	 * Initialize.
	 */
	void init();

	/**
	 * Construct a new Answer object.
	 * 
	 * @return A new Answer object.
	 */
	AnswerImpl newAnswer();

	/**
	 * Construct a new Submission object.
	 * 
	 * @return A new Submission object.
	 */
	SubmissionImpl newSubmission();

	/**
	 * Remove any test-drive submissions for this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 */
	void removeTestDriveSubmissions(Assessment assessment);

	/**
	 * Remove any test-drive submissions for this context.
	 * 
	 * @param context
	 *        The context.
	 */
	void removeTestDriveSubmissions(String context);

	/**
	 * Save changes made to these answers.
	 * 
	 * @param answers
	 *        the answers to save.
	 */
	void saveAnswers(List<Answer> answers);

	/**
	 * Save changes made to this submission.
	 * 
	 * @param submission
	 *        the submission to save.
	 */
	void saveSubmission(SubmissionImpl submission);

	/**
	 * Save evaluation changes made to this submission.
	 * 
	 * @param submission
	 *        the submission with evaluation changes to save.
	 */
	void saveSubmissionEvaluation(SubmissionImpl submission);

	/**
	 * Save changes made to this submission's released setting.
	 * 
	 * @param submission
	 *        the submission to save.
	 */
	void saveSubmissionReleased(SubmissionImpl submission);

	/**
	 * Save changes made to this submission's reviewed setting.
	 * 
	 * @param submission
	 *        the submission to save.
	 */
	void saveSubmissionReviewed(SubmissionImpl submission);

	/**
	 * Check if there are any submissions that are dependent on this question.
	 * 
	 * @param question
	 *        The question.
	 * @return TRUE if there are submissions dependent on this question, FALSE if not.
	 */
	Boolean submissionsDependsOn(Question question);

	/**
	 * Check if any submissions in any state exist for this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return TRUE if there are any submissions to the assesment.
	 */
	Boolean submissionsExist(Assessment assessment);
}
