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
 *
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.ManualPart;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.Shuffler;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * ManualPartImpl implements ManualPart
 */
public class ManualPartImpl extends PartImpl implements ManualPart
{
	protected List<PoolPick> questions = new ArrayList<PoolPick>();

	protected Boolean randomize = Boolean.FALSE;

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment this is the parts for.
	 * @param questionService
	 *        The QuestionService.
	 */
	public ManualPartImpl(AssessmentImpl assessment, QuestionService questionService, SubmissionService submissionService, Changeable owner,
			InternationalizedMessages messages)
	{
		super(assessment, questionService, submissionService, owner, messages);
	}

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 * @param assessment
	 *        The assessment this is the parts for.
	 */
	public ManualPartImpl(ManualPartImpl other, AssessmentImpl assessment, Changeable owner)
	{
		super(other, assessment, owner);
		this.questions = new ArrayList<PoolPick>(other.questions.size());
		for (PoolPick pick : other.questions)
		{
			this.questions.add(new PoolPick(pick));
		}
		this.randomize = other.randomize;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addQuestion(Question question)
	{
		if (question == null) throw new IllegalArgumentException();
		// TODO: do we already have this? ignore it?
		this.questions.add(new PoolPick(this.questionService, question.getId()));

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getFirstQuestion()
	{
		List<PoolPick> order = getQuestionPickOrder();
		PoolPick pick = order.get(0);
		QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(pick.getQuestionId());

		// set the assessment, part and submission context
		question.initSubmissionContext(this.assessment.getSubmissionContext());
		question.initPartContext(this);

		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInvalidMessage()
	{
		Object[] args = new Object[1];
		args[0] = this.getOrdering().getPosition().toString();

		// we must have questions
		if (this.questions.isEmpty())
		{
			return messages.getFormattedMessage("invalid-part-empty", args);
		}

		// the questions must exist and be valid
		for (PoolPick pick : this.questions)
		{
			Question question = this.questionService.getQuestion(pick.getQuestionId());
			if (question == null) return messages.getFormattedMessage("invalid-manual-part-deleted-question", args);
			if (!question.getIsValid()) return messages.getFormattedMessage("invalid-manual-part-invalid-question", args);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// we must have questions
		if (this.questions.isEmpty()) return Boolean.FALSE;

		// the questions must exist and be valid
		for (PoolPick pick : this.questions)
		{
			Question question = this.questionService.getQuestion(pick.getQuestionId());
			if (question == null) return Boolean.FALSE;
			if (!question.getIsValid()) return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getLastQuestion()
	{
		List<PoolPick> order = getQuestionPickOrder();
		PoolPick pick = order.get(order.size() - 1);
		QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(pick.getQuestionId());

		// set the assessment, part and submission context
		question.initSubmissionContext(this.assessment.getSubmissionContext());
		question.initPartContext(this);

		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumQuestions()
	{
		int count = 0;
		for (PoolPick pick : this.questions)
		{
			if (this.questionService.existsQuestion(pick.getQuestionId())) count++;
		}

		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> getQuestionsAsAuthored()
	{
		List<Question> rv = new ArrayList<Question>(this.questions.size());
		for (PoolPick pick : this.questions)
		{
			QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(pick.getQuestionId());
			if (question != null)
			{
				// set the assessment, part and submission context
				question.initSubmissionContext(this.assessment.getSubmissionContext());
				question.initPartContext(this);

				rv.add(question);
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getRandomize()
	{
		return this.randomize;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getTotalPoints()
	{
		// no point assessments have no points
		if (!this.assessment.getHasPoints()) return Float.valueOf(0f);

		float total = 0f;
		for (PoolPick pick : this.questions)
		{
			QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(pick.getQuestionId());
			if (question != null)
			{
				total += question.getPoints();
			}
		}

		// round away bogus decimals
		total = Math.round(total * 100.0f) / 100.0f;

		return Float.valueOf(total);
	}

	/**
	 * Re-establish a pick
	 * 
	 * @param questionId
	 *        The question id.
	 * @param origQuestionId
	 *        The orig question id.
	 * @param poolId
	 *        The pool id.
	 */
	public void initPick(String questionId, String origQuestionId, String poolId)
	{
		this.questions.add(new PoolPick(this.questionService, questionId, origQuestionId, poolId));
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeQuestion(Question question)
	{
		if (question == null) throw new IllegalArgumentException();

		PoolPick remove = null;
		for (PoolPick pick : this.questions)
		{
			if (pick.getQuestionId().equals(question.getId()))
			{
				remove = pick;
				break;
			}
		}

		if (remove != null)
		{
			this.questions.remove(remove);

			// this is a change that cannot be made to live tests
			this.assessment.lockedChanged = Boolean.TRUE;

			setChanged();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setQuestionOrder(String[] questionIds)
	{
		if (questionIds == null) return;

		List<PoolPick> ids = new ArrayList<PoolPick>();
		for (String id : questionIds)
		{
			ids.add(new PoolPick(this.questionService, id));
		}

		// make a copy of our current list
		List<PoolPick> current = new ArrayList<PoolPick>(this.questions);

		// remove anything from the new list not in our questions
		ids.retainAll(current);

		// remove these from our current list
		current.removeAll(ids);

		// add to the end of the new list any remaining quesitions from our current list
		ids.addAll(current);

		// if the order is the same as when we started, ignore it.
		boolean changed = false;
		for (int i = 0; i < ids.size(); i++)
		{
			if (!this.questions.get(i).equals(ids.get(i)))
			{
				changed = true;
				break;
			}
		}

		// ignore if no changes
		if (!changed) return;

		// take the new list
		this.questions = ids;

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRandomize(Boolean setting)
	{
		if (setting == null) throw new IllegalArgumentException();
		if (this.randomize.equals(setting)) return;

		this.randomize = setting;

		setChanged();
	}

	/**
	 * Get the question ids that are manually selected from this pool.
	 * 
	 * @param pool
	 *        The pool.
	 * @param survey
	 *        if null, consider all questions; else consider only questions that match survey in their survey setting.
	 * @return The question ids that are manually selected from this pool.
	 */
	protected List<String> getPoolPicks(Pool pool, Boolean survey)
	{
		List<String> rv = new ArrayList<String>();
		for (PoolPick pick : this.questions)
		{
			Question question = this.questionService.getQuestion(pick.getQuestionId());
			if (question == null) continue;
			if ((survey != null) && (!question.getIsSurvey().equals(survey))) continue;
			if (!question.getPool().getId().equals(pool.getId())) continue;

			rv.add(pick.getQuestionId());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	protected List<PoolPick> getPossibleQuestionPicks()
	{
		return this.questions;
	}

	/**
	 * {@inheritDoc}
	 */
	protected List<PoolPick> getQuestionPickOrder()
	{
		if ((!this.randomize) || (this.assessment == null) || (this.assessment.getSubmissionContext() == null)) return this.questions;

		// copy the questions
		List<PoolPick> rv = new ArrayList<PoolPick>(this.questions);

		// randomize the questions in the copy
		Shuffler shuffler = new ShufflerImpl(this);
		shuffler.shuffle(rv, this.id);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setOrig(Map<String, String> idMap)
	{
		for (Iterator i = this.questions.iterator(); i.hasNext();)
		{
			PoolPick pick = (PoolPick) i.next();

			// if we cannot restore the original values, remove the pick
			if (!pick.setOrig(idMap))
			{
				i.remove();
			}
		}
	}
}
