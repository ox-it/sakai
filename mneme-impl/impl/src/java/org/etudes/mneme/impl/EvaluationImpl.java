/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	/**
	 * Format the text - any special evaluation marks are translated into html for display
	 * 
	 * @param data
	 *        The evaluation text.
	 * @return The html formatted evaluation text.
	 */
	public static String formatEvaluation(String data)
	{
		if (data == null) return data;

		Pattern p = Pattern.compile("\\{\\{(.|\\s)*?\\}\\}");
		Matcher m = p.matcher(data);
		StringBuffer sb = new StringBuffer();
		while (m.find())
		{
			if (m.groupCount() == 1)
			{
				String comment = m.group(0);
				comment = comment.substring(2, comment.length() - 2);
				m.appendReplacement(sb, "<span style=\"color:#C11B17;font-weight:bold\">" + comment + "</span>");
			}
		}
		m.appendTail(sb);

		return sb.toString();
	}

	protected List<Reference> attachments = new ArrayList<Reference>();

	protected AttributionImpl attribution = new AttributionImpl(null);

	/** Track any changes - all but evaluated. */
	protected transient Changeable changed = new ChangeableImpl();

	protected String comment = null;

	protected Boolean evaluated = Boolean.FALSE;

	/** Track changes to the evaluated setting. */
	protected transient Changeable evaluatedChanged = new ChangeableImpl();

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

	/**
	 * {@inheritDoc}
	 */
	public String getCommentFormatted()
	{
		return formatEvaluation(this.comment);
	}

	/**
	 * {@inheritDoc}
	 */
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
		for (Iterator<Reference> i = this.attachments.iterator(); i.hasNext();)
		{
			Reference ref = i.next();
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

		this.evaluatedChanged.setChanged();
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
		this.evaluatedChanged.clearChanged();
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
	 * Check if there was any change to evaluated.
	 * 
	 * @return TRUE if changed, FALSE if not.
	 */
	protected Boolean getIsEvaluatedChanged()
	{
		return this.evaluatedChanged.getChanged();
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
		this.evaluatedChanged = new ChangeableImpl(other.evaluatedChanged);
		this.comment = other.comment;
		this.evaluated = other.evaluated;
		this.score = other.score;
	}
}
