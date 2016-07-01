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

package org.etudes.mneme.impl;

import java.util.HashSet;
import java.util.Set;

import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificAnswer;

/**
 * MultipleChoiceAnswerImpl handles answers for the multiple choice question type.
 */
public class MultipleChoiceAnswerImpl implements TypeSpecificAnswer
{
	/** The answer this is a helper for. */
	protected transient Answer answer = null;

	/** The answers, as index references to the question's choices. */
	protected Set<Integer> answerData = new HashSet<Integer>();

	/** Set when the answer has been changed. */
	protected boolean changed = false;

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 */
	public MultipleChoiceAnswerImpl(Answer answer)
	{
		this.answer = answer;
	}

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 * @param other
	 *        The other to copy.
	 */
	public MultipleChoiceAnswerImpl(Answer answer, MultipleChoiceAnswerImpl other)
	{
		this.answer = answer;
		this.answerData = new HashSet<Integer>(other.answerData);
		this.changed = other.changed;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearIsChanged()
	{
		this.changed = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone(Answer answer)
	{
		try
		{
			// get an exact, bit-by-bit copy
			Object rv = super.clone();

			// deep copy
			((MultipleChoiceAnswerImpl) rv).answerData = new HashSet<Integer>(this.answerData);

			((MultipleChoiceAnswerImpl) rv).answer = answer;

			return rv;
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void consolidate(String destination)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public Answer getAnswerObject()
	{
		return this.answer;
	}

	/**
	 * Access the currently selected answer as a string.
	 * 
	 * @return The answer.
	 */
	public String[] getAnswers()
	{
		String[] rv = new String[answerData.size()];
		int i = 0;
		for (Integer answer : this.answerData)
		{
			rv[i++] = answer.toString();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getAutoScore()
	{
		Question question = this.answer.getQuestion();

		// no point questions and questions that have no correct answer have no score
		if ((!question.getHasPoints()) || (!question.getHasCorrect())) return Float.valueOf(0f);

		// partial credit for each correct answer, partial negative for each incorrect, floor at 0.

		// count the number of correct answers
		Set<Integer> correctAnswers = ((MultipleChoiceQuestionImpl) question.getTypeSpecificQuestion()).getCorrectAnswerSet();

		// each correct / incorrect gets a part of the total points
		float partial = (correctAnswers.size() > 0) ? question.getPoints() / correctAnswers.size() : 0f;

		float total = 0f;
		for (Integer answer : this.answerData)
		{
			// if this is one of the correct answers, give credit
			if (correctAnswers.contains(answer))
			{
				total += partial;
			}

			// otherwise remove credit
			else
			{
				total -= partial;
			}
		}

		// floor at 0
		if (total < 0f) total = 0f;

		// round away bogus decimals
		total = Math.round(total * 100.0f) / 100.0f;

		return Float.valueOf(total);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getCompletelyCorrect()
	{
		// if the question has no correct answer
		Question question = this.answer.getQuestion();
		if (!question.getHasCorrect()) return null;

		// if unanswered
		if (!this.getIsAnswered()) return Boolean.FALSE;

		// count the number of correct answers
		Set<Integer> correctAnswers = ((MultipleChoiceQuestionImpl) question.getTypeSpecificQuestion()).getCorrectAnswerSet();
		
		if (correctAnswers.size() != this.answerData.size()) return Boolean.FALSE;
		for (Integer answer : this.answerData)
		{
			if (!correctAnswers.contains(answer))
			{
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPartiallyCorrect()
	{
		// if the question has no correct answer
		Question question = this.answer.getQuestion();
		if (!question.getHasCorrect()) return null;

		// if unanswered
		if (!this.getIsAnswered()) return Boolean.FALSE;

		// count the number of correct answers
		Set<Integer> correctAnswers = ((MultipleChoiceQuestionImpl) question.getTypeSpecificQuestion()).getCorrectAnswerSet();
		for (Integer answer : this.answerData)
		{
			if (correctAnswers.contains(answer))
			{
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[this.answerData.size()];
		int i = 0;
		for (Integer a : this.answerData)
		{
			rv[i++] = a.toString();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsAnswered()
	{
		return !this.answerData.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsChanged()
	{
		return this.changed;
	}

	/**
	 * Set the answers
	 * 
	 * @param answers
	 *        array of strings
	 */
	public void setAnswers(String[] answers)
	{
		if ((answers == null) || (answers.length == 0)) return;

		Set<Integer> s = new HashSet<Integer>();
		for (String answer : answers)
		{
			s.add(Integer.valueOf(answer));
		}

		// check if the new answers exactly match the answers we already have.
		if (s.equals(this.answerData)) return;

		this.answerData = s;
		this.changed = true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		this.answerData = new HashSet<Integer>();
		if ((data != null) && (data.length > 0))
		{
			for (int i = 0; i < data.length; i++)
			{
				this.answerData.add(Integer.valueOf(data[i]));
			}
		}
	}
}
