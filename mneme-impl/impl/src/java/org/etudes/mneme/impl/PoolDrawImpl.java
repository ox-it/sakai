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
import java.util.List;
import java.util.Map;

import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Shuffler;

/**
 * PoolDrawImpl implements PoolDraw
 */
public class PoolDrawImpl extends PartDetailImpl implements PoolDraw
{
	/** The assessment context. */
	protected transient Assessment assessment = null;

	/** The number of questions to draw from the pool. */
	protected Integer numQuestions = null;

	/** The original pool id. */
	protected String origPoolId = null;

	/** The actual pool id. */
	protected String poolId = null;

	/** Dependency: PoolService. */
	protected PoolService poolService = null;

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment context.
	 * @param part
	 *        The part.
	 * @param poolService
	 *        The PoolService.
	 * @param id
	 *        the detail id.
	 * @param pool
	 *        The pool to draw from.
	 * @param numQuestions
	 *        The number of questions to draw.
	 * @param points
	 *        The detail points value.
	 */
	public PoolDrawImpl(Assessment assessment, Part part, PoolService poolService, Pool pool, Integer numQuestions, Float points)
	{
		this(part, poolService, points);
		if (pool == null) throw new IllegalArgumentException();
		this.assessment = assessment;
		this.poolId = pool.getId();
		this.origPoolId = pool.getId();
		this.numQuestions = numQuestions;
	}

	/**
	 * Construct.
	 * 
	 * @param part
	 *        The part.
	 * @param other
	 *        The other to copy.
	 */
	public PoolDrawImpl(Part part, PoolDrawImpl other)
	{
		super(part);
		set(other);
	}

	/**
	 * Construct.
	 * 
	 * @param part
	 *        The part.
	 * @param poolService
	 *        The PoolService.
	 * @param points
	 *        The detail points value.
	 */
	public PoolDrawImpl(Part part, PoolService poolService, Float points)
	{
		super(part, points);
		this.poolService = poolService;
	}

	/**
	 * Construct.
	 * 
	 * @param part
	 *        The part.
	 * @param poolService
	 *        The PoolService.
	 * @param id
	 *        the detail id.
	 * @param poolId
	 *        The pool to draw from.
	 * @param origPoolId
	 *        The original pool id.
	 * @param numQuestions
	 *        The number of questions to draw.
	 * @param points
	 *        The detail points value.
	 */
	public PoolDrawImpl(Part part, PoolService poolService, String id, String poolId, String origPoolId, Integer numQuestions, Float points)
	{
		this(part, poolService, points);
		if (poolId == null) throw new IllegalArgumentException();
		if (origPoolId == null) throw new IllegalArgumentException();
		this.id = id;
		this.poolId = poolId;
		this.origPoolId = origPoolId;
		this.numQuestions = numQuestions;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> drawQuestionIds(Shuffler shuffler)
	{
		Pool pool = getPool();
		if (pool == null) return new ArrayList<String>();

		Assessment assessment = this.assessment;
		if ((assessment == null) && (this.part != null)) assessment = this.part.getAssessment();

		// for a uniform pool, draw from any survey or not; otherwise match the draw to the assessment type
		Pool.PoolCounts counts = pool.getNumQuestionsSurvey();
		Boolean survey = null;
		if ((counts.assessment != 0) && (counts.survey != 0) && (assessment != null))
		{
			survey = Boolean.valueOf(assessment.getType() == AssessmentType.survey);
		}

		// we need to overdraw by the number of manual questions this assessment uses from the pool
		List<String> manualQuestionIds = (assessment != null) ? ((AssessmentPartsImpl) assessment.getParts()).getQuestionPicksFromPool(pool, survey)
				: new ArrayList<String>();

		int size = this.numQuestions + manualQuestionIds.size();

		List<String> rv = pool.drawQuestionIds(shuffler, size, survey);

		// we need to remove from rv any manual questions used in the assessment
		rv.removeAll(manualQuestionIds);

		// we need just our count
		if (rv.size() > this.numQuestions)
		{
			rv = rv.subList(0, this.numQuestions);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// equal if they have the same pool
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		return this.poolId.equals(((PoolDrawImpl) obj).poolId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAllQuestionIds()
	{
		Pool pool = getPool();
		if (pool == null) return new ArrayList<String>();

		return pool.getAllQuestionIds(null, Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return ((PartImpl) this.part).messages.getFormattedMessage("random-draw-description", null);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasPoints()
	{
		Pool pool = getPool();
		if (pool == null) return Boolean.FALSE;

		// if the assessment is non-survey, and the draw pool has non-survey questions, we have points
		Assessment assessment = this.assessment;
		if ((assessment == null) && (this.part != null)) assessment = this.part.getAssessment();
		if (assessment == null) return Boolean.FALSE;
		if (assessment.getType() == AssessmentType.survey) return Boolean.FALSE;

		Pool.PoolCounts counts = pool.getNumQuestionsSurvey();
		if (counts.assessment != 0) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInvalidMessage()
	{
		// we need a valid pool and a positive count that is within the pool's question limit
		if (this.poolId == null) return ((PartImpl) this.part).messages.getFormattedMessage("invalid-detail-missing-pool", null);
		if (this.numQuestions == null) return ((PartImpl) this.part).messages.getFormattedMessage("invalid-detail-missing-count", null);
		if (this.numQuestions.intValue() <= 0) return ((PartImpl) this.part).messages.getFormattedMessage("invalid-detail-missing-count", null);

		Pool p = getPool();
		if (p == null) return ((PartImpl) this.part).messages.getFormattedMessage("invalid-detail-missing-pool", null);

		// each draw must have enough questions in the pool to draw
		if (getPoolNumAvailableQuestions() < getNumQuestions())
		{
			Object[] args = new Object[1];
			args[0] = p.getTitle();
			return ((PartImpl) this.part).messages.getFormattedMessage("invalid-detail-overdraw", args);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsPhantom()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsSpecific()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// we need a valid pool and a positive count that is within the pool's question limit
		if (this.poolId == null) return Boolean.FALSE;
		if (this.numQuestions == null) return Boolean.FALSE;
		if (this.numQuestions.intValue() <= 0) return Boolean.FALSE;

		Pool p = getPool();
		if (p == null) return Boolean.FALSE;

		// each draw must have enough questions in the pool to draw
		if (getPoolNumAvailableQuestions() < getNumQuestions())
		{
			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getNonOverridePoints()
	{
		if ((this.numQuestions == null) || (this.numQuestions == 0)) return Float.valueOf(0);
		if (!getHasPoints().booleanValue()) return Float.valueOf(0);

		// pool's point value * num questions
		Pool pool = this.poolService.getPool(this.origPoolId);
		if (pool == null) return Float.valueOf(0);

		float poolPoints = pool.getPoints().floatValue();
		return Float.valueOf(poolPoints * this.numQuestions.intValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumQuestions()
	{
		return this.numQuestions;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOrigPoolId()
	{
		return this.origPoolId;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool getPool()
	{
		return poolService.getPool(this.poolId);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPoolId()
	{
		return this.poolId;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getPoolNumAvailableQuestions()
	{
		Pool pool = getPool();
		if (pool != null)
		{
			Assessment assessment = this.assessment;
			if ((assessment == null) && (this.part != null)) assessment = this.part.getAssessment();

			// for a uniform pool, draw from any survey or not; otherwise match the draw to the assessment type
			Pool.PoolCounts counts = pool.getNumQuestionsSurvey();
			Boolean survey = null;
			if ((counts.assessment != 0) && (counts.survey != 0) && (assessment != null))
			{
				survey = Boolean.valueOf(assessment.getType() == AssessmentType.survey);
			}

			int size = 0;

			// if uniform, count them all
			if (survey == null)
			{
				size = counts.assessment + counts.survey;
			}

			// if not uniform, use the count that matches the assessment
			else
			{
				if (survey)
				{
					size = counts.survey;
				}
				else
				{
					size = counts.assessment;
				}
			}

			// reduce by the number of picks from this pool in the assessment
			if (assessment != null)
			{
				size -= ((AssessmentPartsImpl) assessment.getParts()).getQuestionPicksFromPool(pool, survey).size();
			}

			return Integer.valueOf(size);
		}

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getQuestionPoints()
	{
		if ((this.numQuestions == null) || (this.numQuestions <= 0)) return Float.valueOf(0);
		if (!getHasPoints().booleanValue()) return Float.valueOf(0);

		float rv = getTotalPoints() / this.numQuestions.intValue();

		// round away bogus decimals
		rv = Math.round(rv * 100.0f) / 100.0f;

		return Float.valueOf(rv);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return ((PartImpl) this.part).messages.getFormattedMessage("random-draw-type", null);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		if (this.poolId == null) return "null".hashCode();
		return this.poolId.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean restoreToOriginal(Map<String, String> poolIdMap, Map<String, String> questionIdMap)
	{
		// if the map is present, translate to another pool id
		if (poolIdMap != null)
		{
			String translated = poolIdMap.get(this.origPoolId);
			if (translated != null)
			{
				this.origPoolId = translated;
			}
		}

		// if there has been no change, we are done.
		if (this.poolId.equals(this.origPoolId)) return true;

		// check that the original pool is available
		Pool pool = this.poolService.getPool(this.origPoolId);
		if ((pool == null) || (pool.getIsHistorical())) return false;

		// set it
		this.poolId = this.origPoolId;
		setChanged();

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNumQuestions(Integer numQuestions)
	{
		if (!Different.different(numQuestions, this.numQuestions)) return;

		this.numQuestions = numQuestions;
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPool(Pool pool)
	{
		if (pool == null) throw new IllegalArgumentException();
		if (!Different.different(pool.getId(), this.poolId)) return;

		this.poolId = pool.getId();

		// set the original only once
		if (this.origPoolId == null)
		{
			this.origPoolId = pool.getId();
		}

		setChanged();
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(PoolDrawImpl other)
	{
		super.set(other);
		this.numQuestions = other.numQuestions;
		this.origPoolId = other.origPoolId;
		this.poolId = other.poolId;
		this.poolService = other.poolService;
	}
}
