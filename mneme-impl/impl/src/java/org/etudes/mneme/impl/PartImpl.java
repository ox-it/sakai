/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.Ordering;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Presentation;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.Shuffler;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * PartImpl implements Part
 */
public abstract class PartImpl implements Part, Changeable
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

	private static Log M_log = LogFactory.getLog(PartImpl.class);

	protected transient AssessmentImpl assessment = null;

	protected boolean changed = false;

	protected String id = null;

	protected transient InternationalizedMessages messages = null;

	protected MyOrdering ordering = new MyOrdering(this);

	protected transient Changeable owner = null;

	protected PresentationImpl presentation = null;

	protected QuestionService questionService = null;

	protected SubmissionService submissionService = null;

	protected String title = null;

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment this is the parts for.
	 * @param questionService
	 *        The QuestionService.
	 */
	public PartImpl(AssessmentImpl assessment, QuestionService questionService, SubmissionService submissionService, Changeable owner,
			InternationalizedMessages messages)
	{
		this.owner = owner;
		this.assessment = assessment;
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
	public void clearChanged()
	{
		this.changed = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// two PartImpls are equals if they have the same id
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
	public String getId()
	{
		return this.id;
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
		List<PoolPick> questions = null;

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
		PoolPick found = null;
		for (PoolPick pick : questions)
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

		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> getQuestions()
	{
		List<PoolPick> order = getQuestionPickOrder();
		List<Question> rv = new ArrayList<Question>(order.size());
		for (PoolPick pick : order)
		{
			QuestionImpl question = (QuestionImpl) this.questionService.getQuestion(pick.getQuestionId());
			if (question != null)
			{
				// set the question contexts
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
	public List<Question> getQuestionsUsed()
	{
		return this.submissionService.findPartQuestions(this);
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
	public int hashCode()
	{
		return this.id == null ? "null".hashCode() : this.id.hashCode();
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
	protected abstract List<PoolPick> getPossibleQuestionPicks();

	/**
	 * Get the list of question picks as they should be presented for the submission context.
	 * 
	 * @return The list of question picks as they should be presented for the submission context.
	 */
	protected abstract List<PoolPick> getQuestionPickOrder();

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
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(PartImpl other)
	{
		this.changed = other.changed;
		this.id = other.id;
		this.presentation = new PresentationImpl(other.presentation, this);
		this.questionService = other.questionService;
		this.submissionService = other.submissionService;
		this.title = other.title;
		this.messages = other.messages;
	}

	/**
	 * Restore the part pool and question references to their original values.
	 * 
	 * @param idMap
	 *        A map of pool and question ids (old id to new id) to change original references to.
	 */
	protected abstract void setOrig(Map<String, String> idMap);
}
