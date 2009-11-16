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

import java.util.List;

import org.etudes.mneme.api.Attribution;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPoolService;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.Shuffler;
import org.sakaiproject.util.StringUtil;

/**
 * PoolImpl implements Pool
 */
public class PoolImpl implements Pool
{
	/** Track any changes at all. */
	protected transient ChangeableImpl changed = new ChangeableImpl();

	protected String context = "";

	protected Attribution createdBy = null;

	protected String description = null;

	protected Integer difficulty = Integer.valueOf(3);

	protected Boolean historical = Boolean.FALSE;

	protected String id = null;

	/** Stays TRUE until an end-user change to the object occurs, showing it was actually initially set. */
	protected Boolean mint = Boolean.TRUE;

	protected Attribution modifiedBy = null;

	protected Float points = null;

	/** Dependency: QuestionPoolService */
	protected transient QuestionPoolService questionService = null;

	protected String title = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public PoolImpl(PoolImpl other)
	{
		set(other);
	}

	/**
	 * Construct.
	 */
	public PoolImpl()
	{
		this.createdBy = new AttributionImpl(this.changed);
		this.modifiedBy = new AttributionImpl(this.changed);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> drawQuestionIds(Shuffler shuffler, Integer numQuestions, Boolean survey)
	{
		if (numQuestions == null) throw new IllegalArgumentException();
		if (numQuestions.intValue() <= 0) throw new IllegalArgumentException();

		List<String> rv = getAllQuestionIds(survey, Boolean.TRUE);

		// randomize the questions in the copy
		shuffler.shuffle(rv, this.id);

		// cut off the number of questions we want
		if (rv.size() > numQuestions)
		{
			rv = rv.subList(0, numQuestions);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// two PoolImpls are equals if they have the same id
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		if ((this.id == null) || (((PoolImpl) obj).id == null)) return false;
		return this.id.equals(((PoolImpl) obj).id);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> findQuestions(QuestionService.FindQuestionsSort sort, String search, Integer pageNum, Integer pageSize)
	{
		return this.questionService.findQuestions(this, sort, search, null, pageNum, pageSize, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAllQuestionIds(Boolean survey, Boolean valid)
	{
		List<String> rv = this.questionService.getPoolQuestionIds(this, survey, valid);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContext()
	{
		return this.context;
	}

	/**
	 * {@inheritDoc}
	 */
	public Attribution getCreatedBy()
	{
		return this.createdBy;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getDifficulty()
	{
		return this.difficulty;
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
	public Boolean getIsHistorical()
	{
		return this.historical;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMint()
	{
		return this.mint;
	}

	/**
	 * {@inheritDoc}
	 */
	public Attribution getModifiedBy()
	{
		return this.modifiedBy;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumQuestions()
	{
		Integer rv = this.questionService.countQuestions(this, null, null, null, null);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolCounts getNumQuestionsSurvey()
	{
		PoolCounts rv = new PoolCounts();
		rv.assessment = this.questionService.countQuestions(this, null, null, Boolean.FALSE, Boolean.TRUE);
		rv.survey = this.questionService.countQuestions(this, null, null, Boolean.TRUE, Boolean.TRUE);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getPoints()
	{
		return this.points == null ? Float.valueOf(0f) : this.points;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getPointsEdit()
	{
		return this.points;
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
		return getId() == null ? "null".hashCode() : this.getId().hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setContext(String context)
	{
		if (context == null) context = "";
		if (context.length() > 99) throw new IllegalArgumentException();
		if (this.context.equals(context)) return;

		this.context = context;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(String description)
	{
		description = StringUtil.trimToNull(description);

		if (!Different.different(this.description, description)) return;

		this.description = description;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDifficulty(Integer difficulty)
	{
		if (difficulty == null) throw new IllegalArgumentException();

		// massage
		if (difficulty.intValue() < 1) difficulty = Integer.valueOf(1);
		if (difficulty.intValue() > 5) difficulty = Integer.valueOf(5);

		if (this.difficulty.equals(difficulty)) return;

		this.difficulty = difficulty;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPoints(Float points)
	{
		if (points == null) throw new IllegalArgumentException();
		if (points.floatValue() < 0) points = Float.valueOf(0.0f);
		if (points.floatValue() > 10000) points = Float.valueOf(10000.0f);

		// massage points - 2 decimal places
		points = Float.valueOf(((float) Math.round(points.floatValue() * 100.0f)) / 100.0f);

		if (!Different.different(this.points, points)) return;

		this.points = points;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPointsEdit(Float points)
	{
		if ((points != null) && (points.floatValue() < 0)) points = Float.valueOf(0.0f);
		if ((points != null) && (points.floatValue() > 10000)) points = Float.valueOf(10000.0f);

		// massage points - 2 decimal places
		if (points != null)
		{
			points = Float.valueOf(((float) Math.round(points.floatValue() * 100.0f)) / 100.0f);
		}

		if (!Different.different(this.points, points)) return;

		this.points = points;

		this.changed.setChanged();
	}

	/**
	 * Dependency: QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionPoolService service)
	{
		this.questionService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTitle(String title)
	{
		// massage the title
		if (title != null)
		{
			title = title.trim();
			if (title.length() > 255) title = title.substring(0, 255);
			if (title.length() == 0) title = null;
		}

		if (!Different.different(this.title, title)) return;

		this.title = title;

		this.changed.setChanged();
	}

	/**
	 * Clear the changed flag(s).
	 */
	protected void clearChanged()
	{
		this.changed.clearChanged();
	}

	/**
	 * Clear the mint setting.
	 */
	protected void clearMint()
	{
		this.mint = Boolean.FALSE;
	}

	/**
	 * Check if the pool has been changed.
	 * 
	 * @return TRUE if the pool as been changed, FALSE if not.
	 */
	protected Boolean getChanged()
	{
		return this.changed.getChanged();
	}

	/**
	 * Establish the historical settings.
	 * 
	 * @param historical
	 *        The historical setting.
	 */
	protected void initHistorical(Boolean historical)
	{
		this.historical = historical;
	}

	/**
	 * Establish the id.
	 * 
	 * @param id
	 *        The pool id.
	 */
	protected void initId(String id)
	{
		this.id = id;
	}

	/**
	 * Establish the mint setting.
	 * 
	 * @param mint
	 *        The mint setting.
	 */
	protected void initMint(Boolean mint)
	{
		this.mint = mint;
	}

	/**
	 * Set this assessment to be "historical" - used only for history by submissions.
	 * 
	 * @param current
	 *        The current pool this was made from.
	 * @return true if effective, false if ignored.
	 */
	protected boolean makeHistorical(Pool current)
	{
		if (this.historical) return false;

		// mark as historical
		this.historical = Boolean.TRUE;

		return true;
	}

	/**
	 * Set as a copy of the other.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(PoolImpl other)
	{
		this.changed = new ChangeableImpl(other.changed);
		this.createdBy = new AttributionImpl((AttributionImpl) other.createdBy, this.changed);
		this.context = other.context;
		this.description = other.description;
		this.difficulty = other.difficulty;
		this.historical = other.historical;
		this.id = other.id;
		this.mint = other.mint;
		this.modifiedBy = new AttributionImpl((AttributionImpl) other.modifiedBy, this.changed);
		this.points = other.points;
		this.questionService = other.questionService;
		this.title = other.title;
	}
}
