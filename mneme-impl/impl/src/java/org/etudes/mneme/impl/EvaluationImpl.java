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
import java.util.Iterator;
import java.util.List;

import org.etudes.mneme.api.Attribution;
import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.Evaluation;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.util.StringUtil;

/**
 * EvaluationImpl implements Evaluation
 */
public abstract class EvaluationImpl implements Evaluation
{
	protected List<Reference> attachments = new ArrayList<Reference>();

	protected AttributionImpl attribution = new AttributionImpl(null);

	/** Track any changes. */
	protected transient Changeable changed = new ChangeableImpl();

	protected String comment = null;

	protected Boolean evaluated = Boolean.FALSE;

	protected Float score = null;

	/**
	 * Construct.
	 */
	public EvaluationImpl()
	{
	}

	/**
	 * Construct as a copy of other.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public EvaluationImpl(EvaluationImpl other)
	{
		set(other);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAttachment(Reference reference)
	{
		this.attachments.add(reference);
		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Reference> getAttachments()
	{
		return this.attachments;
	}

	/**
	 * {@inheritDoc}
	 */
	public Attribution getAttribution()
	{
		return this.attribution;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getComment()
	{
		return this.comment;
	}

	public Boolean getDefined()
	{
		if (this.score != null) return Boolean.TRUE;
		if (this.comment != null) return Boolean.TRUE;
		if (!this.attachments.isEmpty()) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getEvaluated()
	{
		return this.evaluated;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getScore()
	{
		return this.score;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAttachment(Reference reference)
	{
		for (Iterator i = this.attachments.iterator(); i.hasNext();)
		{
			Reference ref = (Reference) i.next();
			if (ref.getReference().equals(reference.getReference()))
			{
				i.remove();
				this.changed.setChanged();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttachments(List<Reference> references)
	{
		this.attachments = new ArrayList<Reference>();
		if (references != null)
		{
			this.attachments.addAll(references);
		}
		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setComment(String comment)
	{
		comment = StringUtil.trimToNull(comment);

		if (!Different.different(this.comment, comment)) return;

		this.comment = comment;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEvaluated(Boolean evaluated)
	{
		if (evaluated == null) throw new IllegalArgumentException();
		if (this.evaluated.equals(evaluated)) return;

		this.evaluated = evaluated;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setScore(Float s)
	{
		if (s == null)
		{
			if (!Different.different(this.score, s)) return;

			this.score = s;

			this.changed.setChanged();
		}

		else
		{
			// round
			Float newScore = Float.valueOf(((float) Math.round(s.floatValue() * 100.0f)) / 100.0f);

			if (!Different.different(this.score, newScore)) return;

			this.score = newScore;

			this.changed.setChanged();
		}
	}

	/**
	 * Clear the is-changed flag.
	 */
	protected void clearIsChanged()
	{
		this.changed.clearChanged();
	}

	/**
	 * Check if there was any change.
	 * 
	 * @return TRUE if changed, FALSE if not.
	 */
	protected Boolean getIsChanged()
	{
		return this.changed.getChanged();
	}

	/**
	 * Initialize the comment.
	 * 
	 * @param comment
	 *        The comment.
	 */
	protected void initComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * Initialize evaluated.
	 * 
	 * @param evaluated
	 *        The evaluated setting.
	 */
	protected void initEvaluated(Boolean evaluated)
	{
		this.evaluated = evaluated;
	}

	/**
	 * Initialize the score.
	 * 
	 * @param score
	 *        The score.
	 */
	protected void initScore(Float score)
	{
		this.score = (score == null) ? null : Float.valueOf(((float) Math.round(score.floatValue() * 100.0f)) / 100.0f);
	}

	/**
	 * Set values to a copy of other.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(EvaluationImpl other)
	{
		this.attachments = new ArrayList<Reference>(other.attachments.size());
		this.attachments.addAll(other.attachments);
		this.attribution = new AttributionImpl(other.attribution, null);
		this.changed = new ChangeableImpl(other.changed);
		this.comment = other.comment;
		this.evaluated = other.evaluated;
		this.score = other.score;
	}
}
