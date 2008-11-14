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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.etudes.mneme.api.AssessmentParts;
import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.DrawPart;
import org.etudes.mneme.api.ManualPart;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * AssessmentPartsImpl implements AssessmentParts
 */
public class AssessmentPartsImpl implements AssessmentParts
{
	protected transient AssessmentImpl assessment = null;

	protected Boolean continuousNumbering = Boolean.TRUE;

	protected List<Part> deleted = new ArrayList<Part>();

	protected transient InternationalizedMessages messages = null;

	protected Changeable owner = null;

	protected List<Part> parts = new ArrayList<Part>();

	protected PoolService poolService = null;

	protected QuestionService questionService = null;

	protected Boolean showPresentation = null;

	protected SubmissionService submissionService = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public AssessmentPartsImpl(AssessmentImpl assessment, AssessmentPartsImpl other, Changeable owner)
	{
		this.owner = owner;
		set(assessment, other);
	}

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment this is the parts for.
	 * @param assessmentService
	 *        The AssessmentService.
	 * @param questionService
	 *        The QuestionService.
	 * @param poolService
	 *        The PoolService.
	 */
	public AssessmentPartsImpl(AssessmentImpl assessment, QuestionService questionService, SubmissionService submissionService,
			PoolService poolService, Changeable owner, InternationalizedMessages messages)
	{
		this.owner = owner;
		this.assessment = assessment;
		this.questionService = questionService;
		this.submissionService = submissionService;
		this.poolService = poolService;
		this.messages = messages;
	}

	/**
	 * {@inheritDoc}
	 */
	public DrawPart addDrawPart()
	{
		// create the new part
		DrawPart rv = new DrawPartImpl(this.assessment, this.questionService, this.submissionService, this.poolService, this.owner, this.messages);

		// add it to the list
		this.parts.add(rv);
		((PartImpl) rv).initContainer(this.parts);

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		this.owner.setChanged();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public ManualPart addManualPart()
	{
		// create the new part
		ManualPart rv = new ManualPartImpl(this.assessment, this.questionService, this.submissionService, this.owner, this.messages);

		// add it to the list
		this.parts.add(rv);
		((PartImpl) rv).initContainer(this.parts);

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		this.owner.setChanged();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getContinuousNumbering()
	{
		return this.continuousNumbering;
	}

	/**
	 * {@inheritDoc}
	 */
	public Part getFirst()
	{
		return this.parts.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasZeroPointParts()
	{
		for (Part part : this.parts)
		{
			if ((part.getTotalPoints().floatValue() == 0f) && (part.getIsValid()))
			{
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// we must have some parts defined
		if (this.parts.isEmpty()) return Boolean.FALSE;

		// each part must be valid
		for (Part part : this.parts)
		{
			if (!part.getIsValid()) return Boolean.FALSE;
		}

		// we must only draw from a pool once across all draw parts
		List<String> poolIds = new ArrayList<String>();
		for (Part part : this.parts)
		{
			if (part instanceof DrawPart)
			{
				for (PoolDraw draw : ((DrawPart) part).getDraws())
				{
					if (poolIds.contains(draw.getPoolId()))
					{
						return Boolean.FALSE;
					}

					poolIds.add(draw.getPoolId());
				}
			}
		}

		// we must pick a question only once each across all manual parts
		List<String> questionIds = new ArrayList<String>();
		for (Part part : this.parts)
		{
			if (part instanceof ManualPart)
			{
				for (Question question : ((ManualPart) part).getQuestionsAsAuthored())
				{
					if (questionIds.contains(question.getId()))
					{
						return Boolean.FALSE;
					}

					questionIds.add(question.getId());
				}
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumParts()
	{
		int rv = this.parts.size();
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumQuestions()
	{
		int rv = 0;
		for (Part part : this.parts)
		{
			rv += part.getNumQuestions();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Part getPart(String id)
	{
		if (id == null) throw new IllegalArgumentException();
		for (Part part : this.parts)
		{
			if (part.getId().equals(id)) return part;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Part> getParts()
	{
		return this.parts;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getQuestion(String questionId)
	{
		if (questionId == null) throw new IllegalArgumentException();
		for (Part part : this.parts)
		{
			Question question = part.getQuestion(questionId);
			if (question != null) return question;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> getQuestions()
	{
		List<Question> rv = new ArrayList<Question>();
		for (Part part : this.parts)
		{
			rv.addAll(part.getQuestions());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getShowPresentation()
	{
		if (this.showPresentation != null) return this.showPresentation;

		// compute it by checking the continuous numbering and the parts titles and presentation text
		if (!getContinuousNumbering()) return Boolean.TRUE;

		for (Part part : this.parts)
		{
			if ((part.getTitle() != null) || (part.getPresentation().getText() != null)) return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getSize()
	{
		return this.parts.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getTotalPoints()
	{
		// no point assessments have no points
		if (!this.assessment.getHasPoints()) return Float.valueOf(0f);

		float rv = 0f;
		for (Part part : this.parts)
		{
			rv += part.getTotalPoints();
		}

		// round away bogus decimals
		rv = Math.round(rv * 100.0f) / 100.0f;

		return Float.valueOf(rv);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeEmptyParts()
	{
		for (Iterator i = this.parts.iterator(); i.hasNext();)
		{
			Part part = (Part) i.next();

			if ((part.getTitle() == null) && (part.getPresentation().getText() == null) && (part.getPresentation().getAttachments().isEmpty()))
			{
				if (part instanceof ManualPart)
				{
					ManualPart mpart = (ManualPart) part;
					if (mpart.getQuestionsAsAuthored().isEmpty())
					{
						i.remove();
						this.owner.setChanged();
						this.deleted.add(part);
					}
				}
				else if (part instanceof DrawPart)
				{
					DrawPart dpart = (DrawPart) part;
					if (dpart.getDraws().isEmpty())
					{
						i.remove();
						this.owner.setChanged();
						this.deleted.add(part);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePart(Part part)
	{
		this.parts.remove(part);
		((PartImpl) part).initContainer(null);

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		this.owner.setChanged();
		this.deleted.add(part);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setContinuousNumbering(Boolean setting)
	{
		if (setting == null) throw new IllegalArgumentException();
		if (this.continuousNumbering.equals(setting)) return;

		this.continuousNumbering = setting;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOrder(String[] partIds)
	{
		if (partIds == null) return;
		List<String> ids = new ArrayList(Arrays.asList(partIds));

		// remove anything from the new list not in our parts
		for (Iterator i = ids.iterator(); i.hasNext();)
		{
			String id = (String) i.next();
			Part part = getPart(id);
			if (part == null)
			{
				i.remove();
			}
		}

		// start with these
		List<Part> newParts = new ArrayList<Part>();
		for (String id : ids)
		{
			newParts.add(getPart(id));
		}

		// pick up the rest
		for (Part part : this.parts)
		{
			if (!ids.contains(part.getId()))
			{
				newParts.add(part);
			}
		}

		// see if the new list and the old line up - i.e. no changes
		boolean changed = false;
		for (int i = 0; i < newParts.size(); i++)
		{
			if (!this.parts.get(i).equals(newParts.get(i)))
			{
				changed = true;
				break;
			}
		}

		// ignore if no changes
		if (!changed) return;

		// take the new list
		this.parts = newParts;

		// mark the parts as changed to pick up the new order
		// mark the parts as living in this new container
		for (Part part : this.parts)
		{
			((PartImpl) part).setChanged();
			((PartImpl) part).initContainer(this.parts);
		}

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShowPresentation(Boolean setting)
	{
		if (!Different.different(this.showPresentation, setting)) return;

		this.showPresentation = setting;

		this.owner.setChanged();
	}

	/**
	 * Clear the deleted parts.
	 */
	protected void clearDeleted()
	{
		this.deleted.clear();
	}

	/**
	 * Access the deleted parts.
	 * 
	 * @return The List of deleted parts.
	 */
	protected List<Part> getDeleted()
	{
		return this.deleted;
	}

	/**
	 * Get the question ids that are manually selected from this pool, from all parts.
	 * 
	 * @param pool
	 *        The pool.
	 * @param survey
	 *        if null, consider all questions; else consider only questions that match survey in their survey setting.
	 * @return The question ids that are manually selected from this pool, from all parts.
	 */
	protected List<String> getPoolPicks(Pool pool, Boolean survey)
	{
		List<String> rv = new ArrayList<String>();

		// only for manual parts
		for (Part part : this.parts)
		{
			if (part instanceof ManualPartImpl)
			{
				rv.addAll(((ManualPartImpl) part).getPoolPicks(pool, survey));
			}
		}

		return rv;
	}

	/**
	 * Set as a copy of another (deep copy).
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(AssessmentImpl assessment, AssessmentPartsImpl other)
	{
		this.assessment = assessment;
		this.continuousNumbering = other.continuousNumbering;
		this.parts = new ArrayList<Part>(other.parts.size());
		this.showPresentation = other.showPresentation;
		this.questionService = other.questionService;
		this.submissionService = other.submissionService;
		this.poolService = other.poolService;
		this.messages = other.messages;

		for (Part part : other.parts)
		{
			if (part instanceof ManualPartImpl)
			{
				PartImpl newPart = new ManualPartImpl((ManualPartImpl) part, this.assessment, this.owner);
				newPart.initContainer(this.parts);
				newPart.initAssessment(this.assessment);
				this.parts.add(newPart);
			}
			else if (part instanceof DrawPartImpl)
			{
				PartImpl newPart = new DrawPartImpl((DrawPartImpl) part, this.assessment, this.owner);
				newPart.initContainer(this.parts);
				newPart.initAssessment(this.assessment);
				this.parts.add(newPart);
			}

			// TODO: else?
		}
	}
}
