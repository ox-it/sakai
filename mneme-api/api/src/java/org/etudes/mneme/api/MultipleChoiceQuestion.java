/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-api/api/src/java/org/etudes/mneme/api/MultipleChoiceQuestion.java $
 * $Id: MultipleChoiceQuestion.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2011 Etudes, Inc.
 * 
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
import java.util.Set;

/**
 * MultipleChoiceQuestion handles questions for the multiple choice question type.
 */
public interface MultipleChoiceQuestion extends Question
{
	/**
	 * Access the list of choices.
	 * 
	 * @return The list of choices    
	 */
	public List<String> getAnswerChoices();
	
	/**
	 * Access the correct answers as a set.
	 * 
	 * @return The correct answers.
	 */
	public Set<Integer> getCorrectAnswerSet();
	
	/**
	 * Access the shuffle choice.
	 * 
	 * @return The shuffle choice.
	 */
	public boolean getShuffleChoices();
	
	/**
	 * Access the value of single correct.
	 * 
	 * @return The value of single correct.
	 */
	public boolean getSingleCorrect();
	
	/**
	 * Set the entire set of choices to these values.
	 * 
	 * @param choices
	 *        The choice values.
	 */
	public void setAnswerChoices(List<String> choices);

	/**
	 * Sets the correct answers as a set.
	 * 
	 * @param correctAnswers
	 *        The correct answers.
	 */
	public void setCorrectAnswerSet(Set<Integer> correctAnswers);
	
	/**
	 * Set the shuffle choice.
	 * 
	 * @param shuffleChoices
	 *        The shuffle choice.
	 */
	public void setShuffleChoices(boolean shuffleChoices);

	/**
	 * Set single or multiple correct
	 * 
	 * @param singleCorrect
	 *        The value of singleCorrect
	 */
	public void setSingleCorrect(boolean singleCorrect);
}
