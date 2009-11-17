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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.etudes.mneme.api.AssessmentParts;
import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPick;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.StringUtil;

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
	public Part addPart()
	{
		// create the new part
		Part rv = new PartImpl(this.assessment, this.poolService, this.questionService, this.submissionService, this.owner, this.messages);

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
	public PartDetail getDetailId(String id)
	{
		for (Part part : getParts())
		{
			for (PartDetail detail : part.getDetails())
			{
				if (id.equals(detail.getId())) return detail;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PartDetail> getDetails()
	{
		List<PartDetail> rv = new ArrayList<PartDetail>();

		for (Part part : getParts())
		{
			rv.addAll(part.getDetails());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PartDetail> getPhantomDetails()
	{
		List<PartDetail> rv = new ArrayList<PartDetail>();

		for (Part part : getParts())
		{
			if (part.getDetails().isEmpty())
			{
				rv.add(new EmptyPartDetailImpl(part));
			}
			else
			{
				rv.addAll(part.getDetails());
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PoolDraw> getDraws(final PoolService.FindPoolsSort sort)
	{
		List<PoolDraw> rv = new ArrayList<PoolDraw>();
		for (PartDetail detail : getDetails())
		{
			if (detail instanceof PoolDraw)
			{
				rv.add((PoolDraw) detail);
			}
		}

		// sort
		if (sort != null)
		{
			Collections.sort(rv, new Comparator<PoolDraw>()
			{
				public int compare(PoolDraw arg0, PoolDraw arg1)
				{
					int rv = 0;
					switch (sort)
					{
						case title_a:
						{
							String s0 = StringUtil.trimToZero(((PoolDraw) arg0).getPool().getTitle());
							String s1 = StringUtil.trimToZero(((PoolDraw) arg1).getPool().getTitle());
							rv = s0.compareToIgnoreCase(s1);
							break;
						}
						case title_d:
						{
							String s0 = StringUtil.trimToZero(((PoolDraw) arg0).getPool().getTitle());
							String s1 = StringUtil.trimToZero(((PoolDraw) arg1).getPool().getTitle());
							rv = -1 * s0.compareToIgnoreCase(s1);
							break;
						}
						case points_a:
						{
							Float f0 = ((PoolDraw) arg0).getPool().getPoints();
							if (f0 == null) f0 = Float.valueOf(0f);
							Float f1 = ((PoolDraw) arg1).getPool().getPoints();
							if (f1 == null) f1 = Float.valueOf(0f);
							rv = f0.compareTo(f1);
							break;
						}
						case points_d:
						{
							Float f0 = ((PoolDraw) arg0).getPool().getPoints();
							if (f0 == null) f0 = Float.valueOf(0f);
							Float f1 = ((PoolDraw) arg1).getPool().getPoints();
							if (f1 == null) f1 = Float.valueOf(0f);
							rv = -1 * f0.compareTo(f1);
							break;
						}
					}

					return rv;
				}
			});
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PoolDraw> getDrawsForPools(String context, PoolService.FindPoolsSort sort, String search)
	{
		// get all the pools we need
		List<Pool> allPools = this.poolService.findPools(context, sort, search);

		List<PoolDraw> rv = new ArrayList<PoolDraw>();

		for (Pool pool : allPools)
		{
			PoolDraw draw = new PoolDrawImpl(this.assessment, null, this.poolService, pool, null, null);
			for (PartDetail detail : getDetails())
			{
				if (detail instanceof PoolDraw)
				{
					PoolDraw myDraw = (PoolDraw) detail;

					if (myDraw.getPoolId().equals(pool.getId()))
					{
						draw.setNumQuestions(myDraw.getNumQuestions());
					}
				}
			}

			rv.add(draw);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Part getFirst()
	{
		if (this.parts.isEmpty()) return null;

		return this.parts.get(0);
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
			for (PartDetail detail : part.getDetails())
			{
				if (detail instanceof PoolDraw)
				{
					PoolDraw draw = (PoolDraw) detail;

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
			for (PartDetail detail : part.getDetails())
			{
				if (detail instanceof QuestionPick)
				{
					QuestionPick pick = (QuestionPick) detail;

					if (questionIds.contains(pick.getQuestionId()))
					{
						return Boolean.FALSE;
					}

					questionIds.add(pick.getQuestionId());
				}
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumDrawQuestions()
	{
		int rv = 0;
		for (PartDetail detail : getDetails())
		{
			if (detail instanceof PoolDraw)
			{
				rv += ((PoolDraw) detail).getNumQuestions();
			}
		}

		return rv;
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
	public PoolDraw getVirtualDraw(Pool pool)
	{
		PoolDraw rv = new PoolDrawImpl(this.assessment, null, this.poolService, pool, null, null);
		for (PartDetail detail : getDetails())
		{
			if (detail instanceof PoolDraw)
			{
				PoolDraw myDraw = (PoolDraw) detail;

				if (myDraw.getPoolId().equals(pool.getId()))
				{
					rv.setNumQuestions(myDraw.getNumQuestions());
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveDetails(String[] detailIds, Part destination)
	{
		if (detailIds == null) throw new IllegalArgumentException();
		if (destination == null) throw new IllegalArgumentException();
		if (destination.getAssessment() != this.assessment) throw new IllegalArgumentException();

		for (String detailId : detailIds)
		{
			// get the detail to move
			PartDetail detail = getDetailId(detailId);
			if (detail == null) continue;

			// if it is in the desired part already
			if (detail.getPart() == destination) continue;

			// remove it - but not so it gets deleted
			((PartImpl) detail.getPart()).setChanged();
			for (Iterator<PartDetail> i = detail.getPart().getDetails().iterator(); i.hasNext();)
			{
				PartDetail d = i.next();
				if (d == detail)
				{
					i.remove();
					break;
				}
			}

			// add it
			((PartImpl) destination).setChanged();
			destination.getDetails().add(detail);

			((PartDetailImpl) detail).setChanged();
		}

		// this is a change that cannot be made to live tests
		this.assessment.lockedChanged = Boolean.TRUE;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDetail(String id)
	{
		for (Part part : this.parts)
		{
			part.removeDetail(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeEmptyParts()
	{
		for (Iterator<Part> i = this.parts.iterator(); i.hasNext();)
		{
			Part part = i.next();

			if ((part.getTitle() == null) && (part.getPresentation().getText() == null) && (part.getPresentation().getAttachments().isEmpty()))
			{
				if (part.getDetails().isEmpty())
				{
					i.remove();
					this.owner.setChanged();
					this.deleted.add(part);
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
		List<String> ids = new ArrayList<String>(Arrays.asList(partIds));

		// remove anything from the new list not in our parts
		for (Iterator<String> i = ids.iterator(); i.hasNext();)
		{
			String id = i.next();
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
	 * {@inheritDoc}
	 */
	public void updateDraws(List<PoolDraw> draws, Part partForNewDraws)
	{
		for (PoolDraw newDraw : draws)
		{
			// if it is empty, make sure it is removed from any part
			if ((newDraw.getNumQuestions() == null) || (newDraw.getNumQuestions().intValue() == 0))
			{
				for (Part part : this.parts)
				{
					part.removeDrawDetail(newDraw.getPool());
				}
			}

			// if it has count, see if we need to update its presence in a part
			else
			{
				boolean used = false;
				for (Part part : this.parts)
				{
					for (PartDetail detail : part.getDetails())
					{
						if (detail instanceof PoolDraw)
						{
							PoolDraw oldDraw = (PoolDraw) detail;

							// is this pool drawn in the part?
							if (oldDraw.getPoolId().equals(newDraw.getPoolId()))
							{
								used = true;

								// is the count different?
								if (oldDraw.getNumQuestions().intValue() != newDraw.getNumQuestions().intValue())
								{
									// update the part
									part.addDrawDetail(newDraw.getPool(), newDraw.getNumQuestions());
								}
							}
						}
					}
				}

				// if we didn't find that it was used, add it to part 1, creating the part if needed
				if ((!used) && (partForNewDraws != null) && (partForNewDraws.getAssessment() == assessment))
				{
					if (getParts().isEmpty())
					{
						addPart();
					}
					partForNewDraws.addDrawDetail(newDraw.getPool(), newDraw.getNumQuestions());
				}
			}
		}
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
	 * Get the question ids that are manually selected from this pool, from all parts. Only include valid questions.
	 * 
	 * @param pool
	 *        The pool.
	 * @param survey
	 *        if null, consider all questions; else consider only questions that match survey in their survey setting.
	 * @return The question ids that are manually selected from this pool, from all parts.
	 */
	protected List<String> getQuestionPicksFromPool(Pool pool, Boolean survey)
	{
		List<String> rv = new ArrayList<String>();

		for (Part part : this.parts)
		{
			for (PartDetail detail : part.getDetails())
			{
				// only consider picks
				if (detail instanceof QuestionPick)
				{
					QuestionPick pick = (QuestionPick) detail;
					Question question = this.questionService.getQuestion(pick.getQuestionId());
					if (question == null) continue;
					if (!question.getIsValid().booleanValue()) continue;
					if ((survey != null) && (!question.getIsSurvey().equals(survey))) continue;
					if (!question.getPool().getId().equals(pool.getId())) continue;

					rv.add(pick.getQuestionId());
				}
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
			PartImpl newPart = new PartImpl((PartImpl) part, this.assessment, this.owner);
			newPart.initContainer(this.parts);
			newPart.initAssessment(this.assessment);
			this.parts.add(newPart);
		}
	}

	/**
	 * Update the sequence numbers for all the parts' details.
	 */
	protected void setDetailSeq()
	{
		for (Part part : this.parts)
		{
			int seq = 1;
			for (PartDetail detail : part.getDetails())
			{
				if (((PartDetailImpl) detail).getSeq() != seq)
				{
					((PartDetailImpl) detail).initSeq(seq);
					((PartDetailImpl) detail).setChanged();
				}
				seq++;
			}
		}
	}
}
