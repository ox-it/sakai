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

import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.TypeSpecificAnswer;

/**
 * LikertScaleAnswerImpl handles answers for the Likert question type.
 */
public class LikertScaleAnswerImpl implements TypeSpecificAnswer
{
	/** The answer this is a helper for. */
	protected transient Answer answer = null;

	/** The answer is the selected option value stored as an Integer. */
	protected Integer answerData = null;

	/** Set when the answer has been changed. */
	protected boolean changed = false;

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 */
	public LikertScaleAnswerImpl(Answer answer)
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
	public LikertScaleAnswerImpl(Answer answer, LikertScaleAnswerImpl other)
	{
		this.answer = answer;
		this.answerData = other.answerData;
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

			// nothing to deep copy

			((LikertScaleAnswerImpl) rv).answer = answer;

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
	 * Access the currently selected answer as a string.
	 * 
	 * @return The answer.
	 */
	public String getAnswer()
	{
		if (this.answerData == null) return null;

		return this.answerData.toString();
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
		// never has points
		return Float.valueOf(0f);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getCompletelyCorrect()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPartiallyCorrect()
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = null;
		if (this.answerData == null)
		{
			rv = new String[0];
		}
		else
		{
			rv = new String[1];
			rv[0] = answerData.toString();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsAnswered()
	{
		return this.answerData != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsChanged()
	{
		return this.changed;
	}

	/**
	 * Set the answer - any boolean string value will work.
	 * 
	 * @param answer
	 *        The answer.
	 */
	public void setAnswer(String answer)
	{
		if ((answer == null) || (answer.trim().length() == 0)) return;

		Integer i = Integer.valueOf(answer.trim());
		if (i.equals(this.answerData)) return;

		this.answerData = i;
		this.changed = true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		this.answerData = null;
		if ((data != null) && (data.length == 1))
		{
			this.answerData = Integer.valueOf(data[0]);
		}
	}
}
