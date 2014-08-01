/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/MultipleChoiceQuestionTypeImpl.java $
 * $Id: MultipleChoiceQuestionTypeImpl.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2011 Etudes, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.etudes.mneme.api.MultipleChoiceQuestion;
import org.etudes.mneme.impl.MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice;

/**
 * MultipleChoiceQuestionImpl handles questions for the essay question type
 */
public class MultipleChoiceQuestionTypeImpl extends QuestionImpl implements MultipleChoiceQuestion
{
	/**
	 * {@inheritDoc}
	 */
	public List<String> getAnswerChoices()
	{
		List<MultipleChoiceQuestionChoice> mcChoiceList = ((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).getChoicesAsAuthored();
		List<String> answerChoices = new ArrayList();

		if (mcChoiceList != null && mcChoiceList.size() > 0)
		{
			answerChoices = new ArrayList<String>(mcChoiceList.size());

			for (MultipleChoiceQuestionChoice mcChoice : mcChoiceList)
			{
				answerChoices.add(mcChoice.getText());
			}
		}

		return answerChoices;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Integer> getCorrectAnswerSet()
	{
		return ((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).getCorrectAnswerSet();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getShuffleChoices()
	{
		return Boolean.parseBoolean(((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).getShuffleChoices());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getSingleCorrect()
	{
		return Boolean.parseBoolean(((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).getSingleCorrect());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnswerChoices(List<String> choices)
	{
		((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).setAnswerChoices(choices);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCorrectAnswerSet(Set<Integer> correctAnswers)
	{
		((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).setCorrectAnswerSet(correctAnswers);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShuffleChoices(boolean shuffleChoices)
	{
		((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).setShuffleChoices(String.valueOf(shuffleChoices));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSingleCorrect(boolean singleCorrect)
	{
		((MultipleChoiceQuestionImpl) getTypeSpecificQuestion()).setSingleCorrect(String.valueOf(singleCorrect));
	}

}
