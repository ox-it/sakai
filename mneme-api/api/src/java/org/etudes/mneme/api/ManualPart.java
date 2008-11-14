/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.api;

import java.util.List;

/**
 * ManualPart holds a set of manually selected questions.
 */
public interface ManualPart extends Part
{
	/**
	 * Add a question to the part.
	 * 
	 * @param question
	 *        The question to add.
	 */
	void addQuestion(Question question);

	/**
	 * Access the questions. The order will be in authored order.
	 * 
	 * @return The questions. The order will be in authored order.
	 */
	List<Question> getQuestionsAsAuthored();

	/**
	 * Access the randomize flag.
	 * 
	 * @return TRUE if questions should be randomized per submission, FALSE if they should be presented in authored order.
	 */
	Boolean getRandomize();

	/**
	 * Remove a question from the part.
	 * 
	 * @param question
	 *        The question to remove.
	 */
	void removeQuestion(Question question);

	/**
	 * Reorder the existing questions to match this order.<br />
	 * Any question not listed remain in their order following this list.<br />
	 * Any questions in the list not matching existing questions are ignored.
	 * 
	 * @param questionIds
	 *        A list of the question ids in order.
	 */
	void setQuestionOrder(String[] questionIds);

	/**
	 * Set the randomize flag.
	 * 
	 * @param setting
	 *        TRUE if questions should be randomized per submission, FALSE if they should be presented in authored order.
	 */
	void setRandomize(Boolean setting);
}
