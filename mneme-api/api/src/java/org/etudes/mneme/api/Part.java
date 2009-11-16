/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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

import java.util.List;

/**
 * Part holds a set of questions within an assessment.
 */
public interface Part
{
	/**
	 * Add a draw to the part; pool and count to draw from.
	 * 
	 * @param pool
	 *        The pool to draw from.
	 * @param numQuestions
	 *        The number of questions to draw.
	 * @return the PoolDraw detail added.
	 */
	PoolDraw addDrawDetail(Pool pool, Integer numQuestions);

	/**
	 * Add a pick to the part; question.
	 * 
	 * @param question
	 *        The question to add.
	 * @return the QuestionPick detail added.
	 */
	QuestionPick addPickDetail(Question question);

	/**
	 * Access the back pointer to the assessment.
	 * 
	 * @return The back pointer to the assessment.
	 */
	Assessment getAssessment();

	/**
	 * Access the part details, each a PoolDraw or QuestionPick.
	 * 
	 * @return The List of part details.
	 */
	List<PartDetail> getDetails();

	/**
	 * Access the first question. The order will be in a random order (if enabled) based on the current user.
	 * 
	 * @return The first question, or null if there are none.
	 */
	Question getFirstQuestion();

	/**
	 * Access the id.
	 * 
	 * @return The id.
	 */
	String getId();

	/**
	 * Get a message describing what is wrong with the part.
	 * 
	 * @return A localized message describing what is wrong with the part, or null if the part is valid.
	 */
	String getInvalidMessage();

	/**
	 * Check if the part is valid.
	 * 
	 * @return TRUE if the part is valid, FALSE if not.
	 */
	Boolean getIsValid();

	/**
	 * Access the last question. The order will be in a random order (if enabled) based on the current user.
	 * 
	 * @return The last question, or null if there are none.
	 */
	Question getLastQuestion();

	/**
	 * Access the count of questions.
	 * 
	 * @return The count of questions.
	 */
	Integer getNumQuestions();

	/**
	 * Access the ordering information within the assessment.
	 * 
	 * @return The ordering information within the assessment.
	 */
	Ordering<Part> getOrdering();

	/**
	 * Access the assessment's presentation; the rich text and attachments that describe the assessment.
	 * 
	 * @return The assessment's presentation.
	 */
	Presentation getPresentation();

	/**
	 * Access one of the questions, by question id.
	 * 
	 * @param questionId
	 *        The question id.
	 * @return the question, or null if the question is not defined or not part of the Part.
	 */
	Question getQuestion(String questionId);

	/**
	 * Access the questions in delivery order.
	 * 
	 * @return The questions in delivery order.
	 */
	List<Question> getQuestions();

	/**
	 * Access the questions that have been used for this part in any submissions. <br />
	 * Order by question description.
	 * 
	 * @return The questions that have been used for this part in any submissions
	 */
	List<Question> getQuestionsUsed();

	/**
	 * Access the randomize flag.
	 * 
	 * @return TRUE if questions should be randomized per submission, FALSE if they should be presented in authored order.
	 */
	Boolean getRandomize();

	/**
	 * @return a non empty string describing the part; either the title, or text based on the part position.
	 */
	String getTag();
	
	/**
	 * Access the title.
	 * 
	 * @return The title.
	 */
	String getTitle();

	/**
	 * Access the sum of all possible points for all questions in the part.
	 * 
	 * @return The sum of all possible points for all questions in the part.
	 */
	Float getTotalPoints();

	/**
	 * Remove the detail with this detail id.
	 * 
	 * @param id
	 *        The detail id.
	 */
	void removeDetail(String id);

	/**
	 * Remove any draw detail that is for this pool.
	 * 
	 * @param pool
	 *        The pool to remove.
	 */
	void removeDrawDetail(Pool pool);

	/**
	 * Remove any pick detail that selects this question.
	 * 
	 * @param question
	 *        The question to remove.
	 */
	void removePickDetail(Question question);

	/**
	 * Set the randomize flag.
	 * 
	 * @param setting
	 *        TRUE if questions should be randomized per submission, FALSE if they should be presented in authored order.
	 */
	void setRandomize(Boolean setting);

	/**
	 * Set the title.
	 * 
	 * @param title
	 *        The title.
	 */
	void setTitle(String title);
}
