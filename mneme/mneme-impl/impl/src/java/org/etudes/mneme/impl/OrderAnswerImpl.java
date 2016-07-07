/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/OrderAnswerImpl.java $
 * $Id: OrderAnswerImpl.java 6402 2013-11-27 22:00:33Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2015 Etudes, Inc.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.etudes.ambrosia.api.UiService;
import org.etudes.ambrosia.api.Value;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificAnswer;
import org.etudes.mneme.impl.OrderQuestionImpl.OrderQuestionChoice;

import org.sakaiproject.util.StringUtil;

/**
 * OrderAnswerImpl handles answers for the order question type.
 */
public class OrderAnswerImpl implements TypeSpecificAnswer
{
	/** The answer this is a helper for. */
	protected transient Answer answer = null;

	/** The answers: a map between an item id and user response. */
	protected Map<String, Value> answerData = new LinkedHashMap<String, Value>();
	
	/** The answer before possible modification. */
	protected Map<String, Value> priorAnswer = new LinkedHashMap<String, Value>();

	/** Set when the answer has been changed. */
	protected boolean changed = false;

	/** Set with the new item index order for a map reordering. */
	transient String newOrder = null;

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
	public OrderAnswerImpl(Answer answer, OrderAnswerImpl other)
	{
		this.answer = answer;
		this.answerData =  new LinkedHashMap<String, Value>(other.answerData);
		this.changed = other.changed;
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
	 * @param other
	 *        The other to copy.
	 */
	public OrderAnswerImpl(Answer answer, UiService uiService)
	{
		this.answer = answer;
		this.uiService = uiService;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void applyNewOrder()
	{
		// if no new order set, nothing to do
		if (this.newOrder == null) return;

		// parse the numbers
		String[] indexStrs = StringUtil.split(this.newOrder, " ");
		int size = indexStrs.length;
		List<OrderQuestionChoice> oqcList = ((OrderQuestionImpl) this.answer.getQuestion().getTypeSpecificQuestion()).getChoices();

		// if we don't have one for each item, something has gone wrong
		if (size != oqcList.size()) return;

		int[] indexes = new int[size];
		for (int i = 0; i < size; i++)
		{
			try
			{
				indexes[i] = Integer.parseInt(indexStrs[i]);
			}
			catch (NumberFormatException e)
			{
				// this is not good
				return;
			}
		}

		// if the new order is just the old order (numbers from 0..size-1), then nothing to do
		boolean changed = false;
		for (int i = 0; i < size; i++)
		{
			if (indexes[i] != i)
			{
				changed = true;
				break;
			}
		}
		if (!changed) return;

		// ok, we have work to do
		this.changed = true;

		String[] answerChoices = new String[oqcList.size() * 2];

		int k = 0;
		for (int i = 0; i < size; i++)
		{
			answerChoices[k++] = oqcList.get(indexes[i]).getId();
			answerChoices[k++] = String.valueOf(i);
		}
		setData(answerChoices);

	}

	/**
	 * {@inheritDoc}
	 */
	public void clearIsChanged()
	{
		this.changed = false;
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
			((OrderAnswerImpl) rv).answerData = new LinkedHashMap<String, Value>(this.answerData);
			
			// deep copy
			if (this.answerData != null)
			{
				((OrderAnswerImpl) rv).answerData = new LinkedHashMap<String, Value>(this.answerData.size());
				((OrderAnswerImpl) rv).priorAnswer = new LinkedHashMap<String, Value>(this.answerData.size());
				for (Map.Entry entry : this.answerData.entrySet())
				{
					Value v = this.uiService.newValue();
					v.setValue(((Value) entry.getValue()).getValue());
					((OrderAnswerImpl) rv).answerData.put((String) entry.getKey(), v);

					v = this.uiService.newValue();
					v.setValue(((Value) entry.getValue()).getValue());
					((OrderAnswerImpl) rv).priorAnswer.put((String) entry.getKey(), v);
				}
			}

			((OrderAnswerImpl) rv).answer = answer;

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
		applyNewOrder();
	}

	/**
	 * {@inheritDoc}
	 */
	public Answer getAnswerObject()
	{
		return this.answer;
	}

	public Map<String, Value> getAnswer()
	{
		if (this.answerData.isEmpty())
		{
			// populate with null values for each pair
			List<OrderQuestionChoice> oqcList = ((OrderQuestionImpl) this.answer.getQuestion().getTypeSpecificQuestion()).getChoices();
			for (OrderQuestionChoice oqc : oqcList)
			{
				this.answerData.put(oqc.getId(), this.uiService.newValue());
				this.priorAnswer.put(oqc.getId(), this.uiService.newValue());
			}
		}

		return this.answerData;
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

		List<OrderQuestionChoice> orderList = ((OrderQuestionImpl) question.getTypeSpecificQuestion()).getChoicesAsAuthored();

		// each correct / incorrect gets a part of the total points
		float partial = (orderList.size() > 0) ? question.getPoints() / orderList.size() : 0f;

		float total = 0f;
		int i = 0;
		for (OrderQuestionChoice oqc : orderList)
		{
			// get the answer for this pair
			Value selection = this.answerData.get(oqc.getId());
			if (selection != null)
			{
				String value = selection.getValue();
				if ((value != null) && value.equals(String.valueOf(i)))
				{
					total += partial;
				}
				i++;
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

		List<OrderQuestionChoice> orderList = ((OrderQuestionImpl) question.getTypeSpecificQuestion()).getChoicesAsAuthored();

		if (orderList.size() != this.answerData.size()) return Boolean.FALSE;
		int i = 0;
		for (OrderQuestionChoice oqc : orderList)
		{
			// get the answer for this pair
			Value selection = this.answerData.get(oqc.getId());
			if (selection != null)
			{
				String value = selection.getValue();
				if ((value == null) || (!value.equals(String.valueOf(i))))
				{
					return Boolean.FALSE;
				}
				i++;
			}
		}

		return Boolean.TRUE;
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
		boolean oneAnswered = false;
		for (Map.Entry entry : this.answerData.entrySet())
		{
			if (((Value)entry.getValue()).getValue() != null) 
			{	
				oneAnswered = true; 
				break;
			}
		}
		return !this.answerData.isEmpty() && oneAnswered;
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
		if (this.newOrder != null) return Boolean.TRUE;
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNewOrder()
	{
		return this.newOrder;
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

		List<OrderQuestionChoice> orderList = ((OrderQuestionImpl) question.getTypeSpecificQuestion()).getChoicesAsAuthored();
		
		int i = 0;
		for (OrderQuestionChoice oqc : orderList)
		{
			// get the answer for this pair
			Value selection = this.answerData.get(oqc.getId());
			if (selection != null)
			{
				String value = selection.getValue();
				if ((value != null) && (value.equals(String.valueOf(i))))
				{
					return Boolean.TRUE;
				}
				i++;
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

	/**
	 * {@inheritDoc}
	 */
	public void setNewOrder(String newOrder)
	{
		this.newOrder = newOrder;
		// no change yet...
	}

}
