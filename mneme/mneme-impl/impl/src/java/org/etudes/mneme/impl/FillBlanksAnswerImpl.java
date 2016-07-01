/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2013 Etudes, Inc.
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
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificAnswer;
import org.sakaiproject.util.StringUtil;

/**
 * FillBlanksAnswerImpl handles answers for the true/false question type.
 */
public class FillBlanksAnswerImpl implements TypeSpecificAnswer
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FillBlanksAnswerImpl.class);

	/**
	 * Check if this answer is correct.
	 * 
	 * @param answer
	 *        The answer
	 * @param correctPattern
	 *        The corresponding correct answer pattern, if order matters.
	 * @param caseSensitive
	 *        if we are to be case sensitive.
	 * @param anyOrder
	 *        if order does not matter.
	 * @param textual
	 *        if the response is to be textual, not numeric.
	 * @param availableCorrectAnswers
	 *        The set of available correct answer patterns. If anyOrder, and this answer is correct, the entry it matched is removed from this set.
	 * @return TRUE if the answer is correct, FALSE if not.
	 */
	protected static Boolean answerCorrect(String answer, String correctPattern, boolean caseSensitive, boolean anyOrder, boolean textual,
			List<String> availableCorrectAnswers)
	{
		String workingAnswer = fullTrim(answer);

		if (!anyOrder)
		{
			// answer must match correctPattern
			if (textual)
			{
				return isFillInAnswerCorrect(workingAnswer, correctPattern, caseSensitive);
			}
			else
			{
				return isNumericAnswerCorrect(workingAnswer, correctPattern);
			}
		}

		else
		{
			// answer must match one of the available correct answers
			for (String pattern : availableCorrectAnswers)
			{
				// answer must match correctPattern
				if (textual)
				{
					if (isFillInAnswerCorrect(workingAnswer, pattern, caseSensitive))
					{
						availableCorrectAnswers.remove(pattern);
						return Boolean.TRUE;
					}
				}
				else
				{
					if (isNumericAnswerCorrect(workingAnswer, pattern))
					{
						availableCorrectAnswers.remove(pattern);
						return Boolean.TRUE;
					}
				}
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * Trim the target outside and inside.
	 * 
	 * @param target
	 *        The string to trim.
	 * @return The trimmed string.
	 */
	protected static String fullTrim(String target)
	{
		String working = target;
		if (working != null)
		{
			working = working.trim();

			// trim interior white space from the answer
			String[] tokens = StringUtil.split(working, " ");
			StringBuilder buf = new StringBuilder();
			buf.append(tokens[0]);
			for (int i = 1; i < tokens.length; i++)
			{
				if ((tokens[i] != null) && (tokens[i].length() > 0))
				{
					buf.append(" ");
					buf.append(tokens[i]);
				}
			}
			working = buf.toString();
		}

		return working;
	}

	/**
	 * Figure out if a fill-in answer is correct.
	 * 
	 * @param answer
	 *        The given answer.
	 * @param correct
	 *        The correct answer pattern (with option bars and wild cards).
	 * @param caseSensitive
	 *        if we should be case sensitive.
	 * @return true if the answer is correct, false if not
	 */
	protected static boolean isFillInAnswerCorrect(String answer, String correct, boolean caseSensitive)
	{
		// get the set of valid answers from the correct answer pattern (each one may have wild cards)
		String[] valid = correct.split("\\|");
		for (String test : valid)
		{
			// ignore leading and trailing white space
			test = trim(test);

			// prepare the test as a regex, quoting all non-wildcards, changing the wildcard "*" into a regex ".+"
			StringBuffer regex = new StringBuffer();
			String[] parts = test.replaceAll("\\*", "|*|").split("\\|");
			for (String part : parts)
			{
				if ("*".equals(part))
				{
					regex.append(".+");
				}
				else
				{
					regex.append(Pattern.quote(part));
				}
			}
			Pattern p = Pattern.compile(regex.toString(), ((!caseSensitive) ? Pattern.CASE_INSENSITIVE : 0));

			// test
			Matcher m = p.matcher(answer);
			boolean result = m.matches();

			if (result) return true;
		}

		return false;
	}

	/**
	 * Figure out if a fill-in numeric answer is correct.
	 * 
	 * @param answer
	 *        The given answer.
	 * @param correct
	 *        The correct answer pattern (with option bars).
	 * @return true if the answer is correct, false if not
	 */
	protected static boolean isNumericAnswerCorrect(String answer, String correct)
	{
		try
		{
			// allow dot or comma for decimal point
			// TODO: this needs to be changed to respect locale, not just replace commas! -ggolden
			answer = answer.replace(',', '.');
			correct = correct.replace(',', '.');

			// answer needs to become a float (allow dot or comma for decimal point)
			float answerValue = Float.parseFloat(answer);

			// form the range of correct answers
			Float[] range = new Float[2];

			// if there's a bar in the correct pattern, split and use the first two as the range
			if (correct.indexOf("|") != -1)
			{
				String[] parts = correct.split("\\|");
				range[0] = Float.parseFloat(trim(parts[0]));
				range[1] = Float.parseFloat(trim(parts[1]));

				// make sure [0] <= [1]
				if (range[0].floatValue() > range[1].floatValue())
				{
					Float hold = range[0];
					range[0] = range[1];
					range[1] = hold;
				}
			}

			// otherwise use the single value for both sides of the range
			else
			{
				range[0] = range[1] = Float.parseFloat(trim(correct));
			}

			// test
			if ((answerValue >= range[0].floatValue()) && (answerValue <= range[1].floatValue()))
			{
				return true;
			}
		}
		catch (NumberFormatException e)
		{
		}

		return false;
	}

	/**
	 * Trim the source from any blanks, and convert any html blanks to real ones.
	 * 
	 * @param source
	 *        The source string.
	 * @return The trimmed source.
	 */
	protected static String trim(String source)
	{
		String rv = source.replace("&nbsp;", " ").trim();
		return rv;
	}

	/** The answer this is a helper for. */
	protected transient Answer answer = null;

	/** String array of user answers */
	protected String[] answers = null;

	/** Set when the answer has been changed. */
	protected boolean changed = false;

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 */
	public FillBlanksAnswerImpl(Answer answer)
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
	public FillBlanksAnswerImpl(Answer answer, FillBlanksAnswerImpl other)
	{
		this.answer = answer;
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

			// nothing to deep copy TODO: answers?

			((FillBlanksAnswerImpl) rv).answer = answer;

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
	public String[] getAnswers()
	{
		return this.answers;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getAutoScore()
	{
		Question question = this.answer.getQuestion();

		// no point questions and questions that have no correct answer have no score
		if ((!question.getHasPoints()) || (!question.getHasCorrect())) return Float.valueOf(0f);

		// partial credit for each correct answer, 0 for each incorrect, floor at 0.
		List<Boolean> corrects = getEntryCorrects();

		// each correct gets a part of the total points
		float partial = (corrects.size() > 0) ? question.getPoints() / corrects.size() : 0f;

		float total = 0f;
		for (Boolean correct : corrects)
		{
			if (correct) total += partial;
		}

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

		// if any part incorrect
		List<Boolean> corrects = getEntryCorrects();
		for (Boolean correct : corrects)
		{
			if ((correct == null) || (!correct)) return Boolean.FALSE;
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

		// if any part incorrect
		List<Boolean> corrects = getEntryCorrects();
		for (Boolean correct : corrects)
		{
			if ((correct != null) && (correct)) return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	/**
	 * Checks if this answer is correct (taking position into account) and returns true if it is,
	 * false if not
	 * @param answer Answer to check on
	 * @param i Position of answer
	 * @return True if answer is correct, false if not
	 */
	public boolean correctFillAnswer(String answer, int i)
	{
		if (answer == null || answer.trim().length() == 0) return false;
		Question question = this.answer.getQuestion();
		List<String> correctAnswers = ((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getCorrectAnswers();
		List<String> availableCorrectAnswers = new ArrayList<String>(correctAnswers);
		
		// Get all other question properties
		boolean caseSensitive = Boolean.parseBoolean(((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getCaseSensitive());
		boolean anyOrder = Boolean.parseBoolean(((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getAnyOrder());
		boolean textual = Boolean.parseBoolean(((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getResponseTextual());

		String correctAnswer = correctAnswers.get(i);
		return answerCorrect(answer, correctAnswer, caseSensitive, anyOrder, textual, availableCorrectAnswers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		int size = (this.answers == null) ? 0 : this.answers.length;

		String[] rv = new String[size];
		for (int i = 0; i < size; i++)
		{
			rv[i] = this.answers[i];
		}

		return rv;
	}

	/**
	 * Get an Boolean for each possible fill-in blank.
	 * 
	 * @return A list of Boolean, one for each possible fill-in blank, TRUE if the entry was made and is correct, FALSE if not.
	 */
	public List<Boolean> getEntryCorrects()
	{
		// this.answers has an entry for each blank - null or filled in. Or is null if we have not been answered.

		// we need an answer for each fill-in. The correct answers will give us that size
		Question question = answer.getQuestion();
		List<String> correctAnswers = ((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getCorrectAnswers();
		List<String> availableCorrectAnswers = new ArrayList<String>(correctAnswers);
		int size = correctAnswers.size();

		// Get all other question properties
		boolean caseSensitive = Boolean.parseBoolean(((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getCaseSensitive());
		boolean anyOrder = Boolean.parseBoolean(((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getAnyOrder());
		boolean textual = Boolean.parseBoolean(((FillBlanksQuestionImpl) question.getTypeSpecificQuestion()).getResponseTextual());

		List<Boolean> rv = new ArrayList<Boolean>(size);

		// if not answered
		if (this.answers == null)
		{
			for (int i = 0; i < size; i++)
			{
				rv.add(Boolean.FALSE);
			}

			return rv;
		}

		for (int i = 0; i < size; i++)
		{
			// get this answer - it might be that the answers are shorter than the corrects, with some answers missing
			String answer = null;
			if (i < answers.length)
			{
				answer = answers[i];
			}

			if (answer == null)
			{
				rv.add(Boolean.FALSE);
			}
			else
			{
				String correctAnswer = correctAnswers.get(i);
				rv.add(answerCorrect(answer, correctAnswer, caseSensitive, anyOrder, textual, availableCorrectAnswers));
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsAnswered()
	{
		return this.answers != null;
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

		// massage the answers
		for (int i = 0; i < answers.length; i++)
		{
			answers[i] = StringUtil.trimToNull(answers[i]);
		}

		// if we have no answers yet, and we have none from these, ignore
		if (this.answers == null)
		{
			boolean allNull = true;
			for (String s : answers)
			{
				if (s != null)
				{
					allNull = false;
					break;
				}
			}

			if (allNull) return;
		}

		// check for a change
		if ((this.answers != null) && (answers.length == this.answers.length))
		{
			boolean changed = false;
			for (int i = 0; i < this.answers.length; i++)
			{
				if (Different.different(answers[i], this.answers[i]))
				{
					changed = true;
					break;
				}
			}

			if (!changed) return;
		}

		this.answers = answers;
		this.changed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		this.answers = null;
		if ((data != null) && (data.length > 0))
		{
			this.answers = new String[data.length];
			for (int i = 0; i < data.length; i++)
			{
				this.answers[i] = data[i];
			}
		}
	}
}
