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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.etudes.ambrosia.api.UiService;
import org.etudes.ambrosia.api.Value;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificAnswer;
import org.etudes.mneme.impl.MatchQuestionImpl.MatchQuestionPair;

/**
 * MatchAnswerImpl handles answers for the match question type.
 */
public class MatchAnswerImpl implements TypeSpecificAnswer
{
	/** The answer this is a helper for. */
	protected transient Answer answer = null;

	/** The answers: a map between a pair id and a choice id (the choice is stored in the Value). */
	protected Map<String, Value> answerData = new LinkedHashMap<String, Value>();

	/** The answer before possible modification. */
	protected Map<String, Value> priorAnswer = new LinkedHashMap<String, Value>();

	/** Dependency: The UI service (Ambrosia). */
	protected transient UiService uiService = null;

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 * @param other
	 *        The other to copy.
	 */
	public MatchAnswerImpl(Answer answer, MatchAnswerImpl other)
	{
		this.answer = answer;
		this.answerData = new LinkedHashMap<String, Value>(other.answerData.size());
		this.priorAnswer = new LinkedHashMap<String, Value>(other.answerData.size());
		for (Map.Entry entry : other.answerData.entrySet())
		{
			Value v = this.uiService.newValue();
			v.setValue(((Value) entry.getValue()).getValue());
			this.answerData.put((String) entry.getKey(), v);

			v = this.uiService.newValue();
			v.setValue(((Value) entry.getValue()).getValue());
			this.priorAnswer.put((String) entry.getKey(), v);
		}

		this.uiService = other.uiService;
	}

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 */
	public MatchAnswerImpl(Answer answer, UiService uiService)
	{
		this.answer = answer;
		this.uiService = uiService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearIsChanged()
	{
		// deep copy to match the current answerData
		this.priorAnswer = new LinkedHashMap<String, Value>(this.answerData.size());
		for (Map.Entry entry : this.answerData.entrySet())
		{
			Value v = this.uiService.newValue();
			v.setValue(((Value) entry.getValue()).getValue());
			this.priorAnswer.put((String) entry.getKey(), v);
		}
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
			if (this.answerData != null)
			{
				((MatchAnswerImpl) rv).answerData = new LinkedHashMap<String, Value>(this.answerData.size());
				((MatchAnswerImpl) rv).priorAnswer = new LinkedHashMap<String, Value>(this.answerData.size());
				for (Map.Entry entry : this.answerData.entrySet())
				{
					Value v = this.uiService.newValue();
					v.setValue(((Value) entry.getValue()).getValue());
					((MatchAnswerImpl) rv).answerData.put((String) entry.getKey(), v);

					v = this.uiService.newValue();
					v.setValue(((Value) entry.getValue()).getValue());
					((MatchAnswerImpl) rv).priorAnswer.put((String) entry.getKey(), v);
				}
			}

			((MatchAnswerImpl) rv).answer = answer;

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

	public Map<String, Value> getAnswer()
	{
		if (this.answerData.isEmpty())
		{
			// populate with null values for each pair
			List<MatchQuestionPair> pairs = ((MatchQuestionImpl) this.answer.getQuestion().getTypeSpecificQuestion()).getPairs();
			for (MatchQuestionPair pair : pairs)
			{
				this.answerData.put(pair.getId(), this.uiService.newValue());
				this.priorAnswer.put(pair.getId(), this.uiService.newValue());
			}
		}

		return this.answerData;
	}

	/**
	 * {@inheritDoc}
	 */
	public Answer getAnswerObject()
	{
		return this.answer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getAutoScore()
	{
		Question question = this.answer.getQuestion();

		// no point questions and questions that have no correct answer have no score
		if ((!question.getHasPoints()) || (!question.getHasCorrect())) return Float.valueOf(0f);

		// the defined pairs
		List<MatchQuestionPair> pairs = ((MatchQuestionImpl) question.getTypeSpecificQuestion()).getPairs();

		// each correct / incorrect uses a portion of the total points
		float partial = (pairs.size() > 0) ? question.getPoints() / pairs.size() : 0f;

		float total = 0f;
		for (MatchQuestionPair pair : pairs)
		{
			// get the answer for this pair
			Value selection = this.answerData.get(pair.getId());
			if (selection != null)
			{
				String value = selection.getValue();
				if ((value != null) && value.equals(pair.getCorrectChoiceId()))
				{
					total += partial;
				}
				// Note: as of 1.1, MN-565, we no longer penalize for incorrect answers
				// else
				// {
				// total -= partial;
				// }
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
		// if there is no correct
		Question question = this.answer.getQuestion();
		if (!question.getHasCorrect()) return null;

		// if unanswered
		if (!this.getIsAnswered()) return Boolean.FALSE;

		// check each defined pair
		List<MatchQuestionPair> pairs = ((MatchQuestionImpl) question.getTypeSpecificQuestion()).getPairs();
		for (MatchQuestionPair pair : pairs)
		{
			// get the answer for this pair
			Value selection = this.answerData.get(pair.getId());
			if (selection != null)
			{
				String value = selection.getValue();
				if ((value == null) || (!value.equals(pair.getCorrectChoiceId())))
				{
					return Boolean.FALSE;
				}
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPartiallyCorrect()
	{
		// if there is no correct
		Question question = this.answer.getQuestion();
		if (!question.getHasCorrect()) return null;

		// if unanswered
		if (!this.getIsAnswered()) return Boolean.FALSE;

		// check each defined pair
		List<MatchQuestionPair> pairs = ((MatchQuestionImpl) question.getTypeSpecificQuestion()).getPairs();
		for (MatchQuestionPair pair : pairs)
		{
			// get the answer for this pair
			Value selection = this.answerData.get(pair.getId());
			if (selection != null)
			{
				String value = selection.getValue();
				if ((value != null) && (value.equals(pair.getCorrectChoiceId())))
				{
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		int size = this.answerData.size() * 2;

		String[] rv = new String[size];
		int i = 0;
		for (Map.Entry entry : this.answerData.entrySet())
		{
			rv[i++] = (String) entry.getKey();
			rv[i++] = ((Value) entry.getValue()).getValue();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsAnswered()
	{
		for (Object value : this.answerData.values())
		{
			if (((Value) value).getValue() != null)
			{
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsChanged()
	{
		if (this.answerData.size() != this.priorAnswer.size()) return Boolean.TRUE;

		for (Map.Entry entry : this.answerData.entrySet())
		{
			String curValue = ((Value) (entry.getValue())).getValue();
			String priorValue = ((Value) (this.priorAnswer.get(entry.getKey()))).getValue();
			if (Different.different(curValue, priorValue))
			{
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		this.answerData = new LinkedHashMap<String, Value>();
		this.priorAnswer = new LinkedHashMap<String, Value>();
		if ((data != null) && (data.length > 0))
		{
			for (int i = 0; i < data.length; i++)
			{
				String key = data[i++];

				Value value = this.uiService.newValue();
				value.setValue(data[i]);
				this.answerData.put(key, value);

				value = this.uiService.newValue();
				value.setValue(data[i]);
				this.priorAnswer.put(key, value);
			}
		}
	}
}
