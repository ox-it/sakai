/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.Ordering;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Presentation;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPick;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.Shuffler;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * PartImpl implements Part
 */
public class PartImpl implements Part, Changeable
{
	public class MyOrdering implements Ordering<Part>
	{
		protected PartImpl part = null;

		protected List<Part> parts = null;

		public MyOrdering(PartImpl part)
		{
			this.part = part;
		}

		/**
		 * {@inheritDoc}
		 */
		public Boolean getIsFirst()
		{
			if (parts == null) return true;

			if (part.equals(parts.get(0))) return true;

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Boolean getIsLast()
		{
			if (parts == null) return true;

			if (part.equals(parts.get(parts.size() - 1))) return true;

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Part getNext()
		{
			if (parts == null) return null;

			int index = parts.indexOf(part);
			if (index == parts.size() - 1) return null;

			return parts.get(index + 1);
		}

		/**
		 * {@inheritDoc}
		 */
		public Integer getPosition()
		{
			if (parts == null) return new Integer(1);

			int index = parts.indexOf(part);

			return index + 1;
		}

		/**
		 * {@inheritDoc}
		 */
		public Part getPrevious()
		{
			if (parts == null) return null;

			int index = parts.indexOf(part);
			if (index == 0) return null;

			return parts.get(index - 1);
		}

		/**
		 * Initialize the parts list that contains this part.
		 * 
		 * @param parts
		 *        The parts list that contains this part.
		 */
		protected void initParts(List<Part> parts)
		{
			this.parts = parts;
		}
	}

	protected class ShufflerImpl implements Shuffler
	{
		protected boolean old = false;

		protected String seedRoot = null;

		public ShufflerImpl(PartImpl part)
		{
			if (part.assessment.getSubmissionContext() != null)
			{
				String crossoverIdStr = ((SubmissionServiceImpl) part.submissionService).get106ShuffleCrossoverId();
				if (crossoverIdStr != null)
				{
					Long crossoverId = Long.valueOf(crossoverIdStr);
					Long subId = Long.valueOf(part.assessment.getSubmissionContext().getId());
					if (subId <= crossoverId) this.old = true;
				}
			}

			// use the submission id as the seed root if available
			if (part.assessment.getSubmissionContext() != null)
			{
				this.seedRoot = part.assessment.getSubmissionContext().getId();
			}

			// if no submission context, just the part id
			else
			{
				this.seedRoot = part.id;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void shuffle(List<? extends Object> source, String seedExtension)
		{
			// the old, 1.0.5 and before way of shuffling (ignore the seed extension, nothing fancy)
			if (this.old)
			{
				Collections.shuffle(source, new Random(this.seedRoot.hashCode()));
			}

			else
			{
				// use the root and the extension, if given
				String seed = this.seedRoot + ((seedExtension != null) ? ("_" + seedExtension) : "");

				// we get much better results with 3 than 2 - use a null to pad it out, then remove it after shuffle
				if (source.size() == 2)
				{
					source.add(null);
					Collections.shuffle(source, new Random(seed.hashCode()));
					source.remove(null);
				}
				else
				{
					Collections.shuffle(source, new Random(seed.hashCode()));
				}
			}
		}
	}

	/** Logger. */
	private static Log M_log = LogFactory.getLog(PartImpl.class);

	/** My assessment. */
	protected transient AssessmentImpl assessment = null;

	/** True if I've been changed. */
	protected boolean changed = false;

	/** My id. */
	protected String id = null;

	/** Messages. */
	protected transient InternationalizedMessages messages = null;

	/** My ordering. */
	protected MyOrdering ordering = new MyOrdering(this);

	/** My owner. */
	protected transient Changeable owner = null;

	/** The pool service dependency. */
	protected PoolService poolService = null;

	/** Part presentation. */
	protected PresentationImpl presentation = null;

	/** Dependency: QuestionService. */
	protected QuestionService questionService = null;

	/** Should we shuffle the questions from the as-authored order for delivery? */
	protected Boolean randomize = Boolean.FALSE;

	/** Dependency: SubmissionService. */
	protected SubmissionService submissionService = null;

	/** Part title. */
	protected String title = null;

	/** List of part details. */
	List<PartDetail> details = new ArrayList<PartDetail>();

	/** Keep track of deleted details. */
	protected List<PartDetail> deletedDetails = new ArrayList<PartDetail>();

	/**
	 * Clear the deleted details.
	 */
	protected void clearDeleted()
	{
		this.deletedDetails.clear();
	}

	/**
	 * Access the deleted details.
	 * 
	 * @return The List of deleted details.
	 */
	protected List<PartDetail> getDeleted()
	{
		return this.deletedDetails;
	}

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment this is the parts for.
	 * @param poolService
	 *        The PoolService.
	 * @param questionService
	 *        The QuestionService.
	 * @param submissionService
	 *        The SubmissionService.
	 * @param owner
	 *        A Changeable to report changes to.
	 * @param messages
	 *        A messages bundle.
	 */
	public PartImpl(AssessmentImpl assessment, PoolService poolService, QuestionService questionService, SubmissionService submissionService,
			Changeable owner, InternationalizedMessages messages)
	{
		this.owner = owner;
		this.assessment = assessment;
		this.poolService = poolService;
		this.questionService = questionService;
		this.submissionService = submissionService;
		this.presentation = new PresentationImpl(this);
		this.messages = messages;
	}

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 * @param assessment
	 *        The assessment this is the parts for.
	 * @param owner
	 *        A Changeable to report changes to.
	 */
	public PartImpl(PartImpl other, AssessmentImpl assessment, Changeable owner)
	{
		this.owner = owner;
		this.assessment = assessment;
		set(other);
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolDraw addDrawDetail(Pool pool, Integer numQuestions)
	{
		// do we have this pool already?
		for (PartDetail detail : getDetails())
		{
			if (detail instanceof PoolDraw)
			{
				PoolDraw draw = (PoolDraw) detail;

				if (draw.getPoolId().equals(pool.getId()))
				{
					if (!Different.different(draw.getNumQuestions(), numQuestions))
					{
						// no change, we are done
						return draw;
					}

					// change the count
					draw.setNumQuestions(numQuestions);

					// this is a change that cannot be made to live tests
					this.assessment.lockedChanged = Boolean.TRUE;

					setChanged();

					return draw;
				}
			}
		}

		// add this to the details
		PoolDraw rv = new PoolDrawImpl(this.assessment, this, this.poolService, pool, numQuestions, null);
		getDetails().add(rv);

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		setChanged();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public QuestionPick addPickDetail(Question question)
	{
		// do we have this already?
		for (PartDetail detail : getDetails())
		{
			if (detail instanceof QuestionPick)
			{
				QuestionPick pick = (QuestionPick) detail;

				if (pick.getQuestionId().equals(question.getId()))
				{
					// no change, we are done
					return pick;
				}
			}
		}

		// add this to the details
		QuestionPick rv = new QuestionPickImpl(this, this.questionService, question.getId());
		getDetails().add(rv);

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		setChanged();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearChanged()
	{
		this.changed = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// equal if they have the same id
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		if ((this.id == null) || (((PartImpl) obj).id == null)) return false;
		return this.id.equals(((PartImpl) obj).id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Assessment getAssessment()
	{
		return this.assessment;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getChanged()
	{
		return this.changed;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PartDetail> getDetails()
	{
		return this.details;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getFirstQuestion()
	{
		List<QuestionPick> order = getQuestionPickOrder();
		QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(order.get(0).getQuestionId());

		// set the assessment, part and submission context
		question.initSubmissionContext(this.assessment.getSubmissionContext());
		question.initPartContext(this);
		question.initPartDetailContext(((QuestionPickImpl) order.get(0)).getRelatedDetail());
		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInvalidMessage()
	{
		// we must have details
		if (getDetails().isEmpty())
		{
			Object[] args = new Object[1];
			args[0] = this.getOrdering().getPosition().toString();
			return messages.getFormattedMessage("invalid-part-empty", args);
		}

		// each draw must have enough questions to draw
		StringBuilder buf = new StringBuilder();
		for (PartDetail detail : getDetails())
		{
			String msg = detail.getInvalidMessage();
			if (msg != null)
			{
				if (buf.length() > 0)
				{
					buf.append("; ");
				}
				buf.append(msg);
			}
		}
		if (buf.length() > 0)
		{
			Object[] args = new Object[2];
			args[0] = this.getOrdering().getPosition().toString();
			args[1] = buf.toString();
			return messages.getFormattedMessage("invalid-part-details", args);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// we must have details
		if (getDetails().isEmpty()) return Boolean.FALSE;

		// each detail must be valid
		for (PartDetail detail : getDetails())
		{
			if (!detail.getIsValid()) return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getLastQuestion()
	{
		List<QuestionPick> order = getQuestionPickOrder();
		QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(order.get(order.size() - 1).getQuestionId());

		// set the assessment, part and submission context
		question.initSubmissionContext(this.assessment.getSubmissionContext());
		question.initPartContext(this);
		question.initPartDetailContext(((QuestionPickImpl) order.get(order.size() - 1)).getRelatedDetail());

		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumQuestions()
	{
		int count = 0;
		for (PartDetail detail : getDetails())
		{
			count += detail.getNumQuestions();
		}

		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	public Ordering<Part> getOrdering()
	{
		return this.ordering;
	}

	/**
	 * {@inheritDoc}
	 */
	public Presentation getPresentation()
	{
		return this.presentation;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getQuestion(String questionId)
	{
		// collect the questions
		List<QuestionPick> questions = null;

		// if under a submission context, use the actual set of questions for this submission
		if (this.assessment.getSubmissionContext() != null)
		{
			// get the actual list of question picks
			questions = getQuestionPickOrder();
		}

		// else use all possible questions
		else
		{
			questions = getPossibleQuestionPicks();
		}

		// make sure this is one of our questions
		QuestionPick found = null;
		for (QuestionPick pick : questions)
		{
			if (pick.getQuestionId().equals(questionId))
			{
				found = pick;
				break;
			}
		}
		if (found == null) return null;

		QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(questionId);
		if (question == null)
		{
			M_log.warn("getQuestion: question not defined: " + questionId);
			return null;
		}

		// set the question contexts
		question.initSubmissionContext(this.assessment.getSubmissionContext());
		question.initPartContext(this);
		question.initPartDetailContext(((QuestionPickImpl) found).getRelatedDetail());

		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> getQuestions()
	{
		List<QuestionPick> order = getQuestionPickOrder();
		List<Question> rv = new ArrayList<Question>(order.size());
		for (QuestionPick pick : order)
		{
			QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(pick.getQuestionId());
			if (question != null)
			{
				// set the question contexts
				question.initSubmissionContext(this.assessment.getSubmissionContext());
				question.initPartContext(this);
				question.initPartDetailContext(((QuestionPickImpl) pick).getRelatedDetail());

				rv.add(question);
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> getQuestionsUsed()
	{
		return this.submissionService.findPartQuestions(this);
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
	public String getTag()
	{
		if ((this.title != null) && (this.title.length() > 0)) return this.title;
		Object[] args = new Object[1];
		args[0] = this.getOrdering().getPosition().toString();
		return this.messages.getFormattedMessage("part-tag", args);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTitle()
	{
		return this.title;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getTotalPoints()
	{
		// no point assessments have no points
		if (!this.assessment.getHasPoints()) return Float.valueOf(0f);

		float total = 0f;
		for (PartDetail detail : getDetails())
		{
			total += detail.getTotalPoints().floatValue();
		}

		// round away bogus decimals
		total = Math.round(total * 100.0f) / 100.0f;

		return Float.valueOf(total);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return this.id == null ? "null".hashCode() : this.id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDetail(String id)
	{
		if (id == null) return;

		for (Iterator<PartDetail> i = getDetails().iterator(); i.hasNext();)
		{
			PartDetail detail = i.next();
			if (id.equals(detail.getId()))
			{
				i.remove();
				this.deletedDetails.add(detail);
			}
		}

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDrawDetail(Pool pool)
	{
		for (Iterator<PartDetail> i = getDetails().iterator(); i.hasNext();)
		{
			PartDetail detail = i.next();
			if (detail instanceof PoolDraw)
			{
				PoolDraw draw = (PoolDraw) detail;

				if (draw.getPoolId().equals(pool.getId()))
				{
					i.remove();
					this.deletedDetails.add(detail);
				}
			}
		}

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePickDetail(Question question)
	{
		for (Iterator<PartDetail> i = getDetails().iterator(); i.hasNext();)
		{
			PartDetail detail = i.next();
			if (detail instanceof QuestionPick)
			{
				QuestionPick pick = (QuestionPick) detail;

				if (pick.getQuestionId().equals(question.getId()))
				{
					i.remove();
					this.deletedDetails.add(detail);
				}
			}
		}

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChanged()
	{
		this.changed = true;
		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRandomize(Boolean randomize)
	{
		if (!Different.different(this.randomize, randomize)) return;

		this.randomize = randomize;

		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTitle(String title)
	{
		if (!Different.different(this.title, title)) return;

		this.title = title;

		setChanged();
	}

	/**
	 * Get the list of possible question picks.
	 * 
	 * @return The list of possible question picks.
	 */
	protected List<QuestionPick> getPossibleQuestionPicks()
	{
		List<QuestionPick> rv = new ArrayList<QuestionPick>();
		for (PartDetail detail : getDetails())
		{
			if (detail instanceof QuestionPick)
			{
				rv.add((QuestionPick) detail);
			}
			else if (detail instanceof PoolDraw)
			{
				PoolDraw draw = (PoolDraw) detail;

				List<String> draws = draw.getAllQuestionIds();
				for (String id : draws)
				{
					QuestionPick pick = new QuestionPickImpl(this, this.questionService, null, id, id, null);
					((QuestionPickImpl) pick).initRelatedDetail(draw);
					rv.add(pick);
				}
			}
		}

		return rv;
	}

	/**
	 * Get the list of question picks as they should be presented for the submission context.
	 * 
	 * @return The list of question picks as they should be presented for the submission context.
	 */
	protected List<QuestionPick> getQuestionPickOrder()
	{
		Shuffler shuffler = new ShufflerImpl(this);

		// Note: old DrawPart or ManualPart on conversion will be uniform draws (with shuffle) or picks (no shuffle).
		// Nothing special to do here with the shuffler to preserve question order -ggolden.

		// random draws from the pools
		List<QuestionPick> rv = new ArrayList<QuestionPick>();
		for (PartDetail detail : getDetails())
		{
			if (detail instanceof PoolDraw)
			{
				PoolDraw draw = (PoolDraw) detail;

				List<String> draws = draw.drawQuestionIds(shuffler);
				for (String id : draws)
				{
					QuestionPick pick = new QuestionPickImpl(this, this.questionService, null, id, id, null);
					((QuestionPickImpl) pick).initRelatedDetail(draw);
					rv.add(pick);
				}
			}
			else if (detail instanceof QuestionPick)
			{
				rv.add((QuestionPick) detail);
			}
		}

		// randomize the questions if set
		if (getRandomize())
		{
			shuffler.shuffle(rv, this.id);
		}

		return rv;
	}

	/**
	 * Establish the assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 */
	protected void initAssessment(AssessmentImpl assessment)
	{
		this.assessment = assessment;
	}

	/**
	 * Set the parts list that contains this part.
	 * 
	 * @param parts
	 *        The parts list that contains this part.
	 */
	protected void initContainer(List<Part> parts)
	{
		this.ordering.initParts(parts);
	}

	/**
	 * Reconstruct a draw.
	 * 
	 * @param id
	 *        The detail id.
	 * @param poolId
	 *        The poolId value.
	 * @param origPoolId
	 *        The origPoolId value
	 * @param numQuestions
	 *        The number of questions.
	 * @param points
	 *        The detail points value.
	 * @return the new part detail.
	 */
	protected PartDetail initDraw(String id, String poolId, String origPoolId, Integer numQuestions, Float points)
	{
		PoolDraw draw = new PoolDrawImpl(this, this.poolService, id, poolId, origPoolId, numQuestions, points);
		getDetails().add(draw);
		return draw;
	}

	/**
	 * Establish the id.
	 * 
	 * @param id
	 *        The part id.
	 */
	protected void initId(String id)
	{
		this.id = id;
	}

	/**
	 * Reconstruct a pick.
	 * 
	 * @param id
	 *        The detail id.
	 * @param questionId
	 *        The questionId value.
	 * @param origQuestionId
	 *        The origQuestionId value
	 * @param points
	 *        The detail points value.
	 * @return the new part detail.
	 */
	protected PartDetail initPick(String id, String questionId, String origQuestionId, Float points)
	{
		QuestionPick pick = new QuestionPickImpl(this, this.questionService, id, questionId, origQuestionId, points);
		getDetails().add(pick);
		return pick;
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(PartImpl other)
	{
		this.changed = other.changed;
		this.id = other.id;
		this.poolService = other.poolService;
		this.presentation = new PresentationImpl(other.presentation, this);
		this.questionService = other.questionService;
		this.randomize = other.randomize;
		this.submissionService = other.submissionService;
		this.title = other.title;
		this.messages = other.messages;

		this.details = new ArrayList<PartDetail>(other.details.size());
		for (PartDetail detail : other.getDetails())
		{
			if (detail instanceof PoolDraw)
			{
				getDetails().add(new PoolDrawImpl(this, (PoolDrawImpl) detail));
			}
			else if (detail instanceof QuestionPick)
			{
				getDetails().add(new QuestionPickImpl(this, (QuestionPickImpl) detail));
			}
		}
	}
}
