/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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
 * QuestionPick contain the details of a part's inclusion of a question from a manual selection.
 */
public interface QuestionPick extends PartDetail
{
	/**
	 * Access the original question id.
	 * 
	 * @return the original question id.
	 */
	String getOrigQuestionId();

	/**
	 * Access the actual question.
	 * 
	 * @return the actual question.
	 */
	Question getQuestion();

	/**
	 * Access the actual question id.
	 * 
	 * @return the actual question id.
	 */
	String getQuestionId();

	/**
	 * Set the question id (original and actual).
	 * 
	 * @param questionId
	 *        The question id.
	 */
	void setQuestionId(String questionId);
}
