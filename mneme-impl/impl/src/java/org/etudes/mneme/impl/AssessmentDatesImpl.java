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

import java.util.Date;

import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentDates;
import org.etudes.mneme.api.Changeable;

/**
 * AssessmentDatesImpl implements AssessmentDates
 */
public class AssessmentDatesImpl extends AssessmentDatesBaseImpl implements AssessmentDates
{
	protected Date acceptUntil = null;

	protected Date archived = null;

	protected Date due = null;

	/** Track the original due value. */
	protected Date dueWas = null;

	protected Date open = null;

	protected transient Changeable owner = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public AssessmentDatesImpl(Assessment assessment, AssessmentDatesImpl other, Changeable owner)
	{
		this(assessment, owner);
		set(other);
	}

	/**
	 * Construct.
	 */
	public AssessmentDatesImpl(Assessment assessment, Changeable owner)
	{
		super(assessment);
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getAcceptUntilDate()
	{
		return this.acceptUntil;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getArchivedDate()
	{
		return this.archived;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getDueDate()
	{
		return this.due;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getOpenDate()
	{
		return this.open;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAcceptUntilDate(Date date)
	{
		if (!Different.different(this.acceptUntil, date)) return;

		this.acceptUntil = date;

		this.owner.setChanged();

		// this is a change that cannot be made to locked assessments if set to a formal course evaluation
		if (this.assessment.getFormalCourseEval()) ((AssessmentImpl) this.assessment).lockedChanged = Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDueDate(Date date)
	{
		if (!Different.different(this.due, date)) return;

		this.due = date;

		this.owner.setChanged();

		// this is a change that cannot be made to locked assessments if set to a formal course evaluation
		if (this.assessment.getFormalCourseEval()) ((AssessmentImpl) this.assessment).lockedChanged = Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOpenDate(Date date)
	{
		if (!Different.different(this.open, date)) return;

		this.open = date;

		this.owner.setChanged();
	}

	/**
	 * Check if the due date was changed.
	 * 
	 * @return TRUE if changed, FALSE if not.
	 */
	protected Boolean getDueDateChanged()
	{
		Boolean rv = Boolean.valueOf(Different.different(this.due, this.dueWas));
		return rv;
	}

	/**
	 * Initialze the due date.
	 * 
	 * @param date
	 *        The due date.
	 */
	protected void initDueDate(Date date)
	{
		this.due = date;
		this.dueWas = (date == null) ? date : new Date(date.getTime());
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(AssessmentDatesImpl other)
	{
		this.acceptUntil = other.acceptUntil;
		this.archived = other.archived;
		this.due = other.due;
		this.dueWas = other.dueWas;
		this.open = other.open;
	}
}
