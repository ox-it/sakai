/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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
import org.etudes.mneme.api.Expiration;
import org.etudes.mneme.api.MnemeService;

/**
 * AssessmentDatesBasempl implements logic for AssessmentDates.
 */
public abstract class AssessmentDatesBaseImpl implements AssessmentDates
{
	/** Link back to the assessment, to get at published and archived settings. */
	protected Assessment assessment = null;

	public AssessmentDatesBaseImpl(Assessment assessment)
	{
		this.assessment = assessment;
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getDurationTillDue()
	{
		// if no due date
		if (getDueDate() == null) return null;

		// if we have started, the clock is running - compute how long from NOW the end is
		Date now = new Date();
		long tillDue = getDueDate().getTime() - now.getTime();
		if (tillDue <= 0) return new Long(0);

		return new Long(tillDue);
	}

	/**
	 * {@inheritDoc}
	 */
	public Expiration getExpiration()
	{
		ExpirationImpl rv = new ExpirationImpl();

		// see if the assessment has a hard due date (no submissions allowed)
		Date closedDate = this.getSubmitUntilDate();
		if (closedDate == null) return null;

		// compute an end time based on the assessment's closed date
		Date now = new Date();

		// if we are past it already
		if (closedDate.before(now)) return null;

		rv.time = closedDate;

		// the closeDate is the end time
		long endTime = closedDate.getTime();

		// if this closed date is more than 2 hours from now, ignore it and say we have no expiration
		if (endTime > now.getTime() + (2l * 60l * 60l * 1000l)) return null;

		// set the limit to 2 hours
		rv.limit = 2l * 60l * 60l * 1000l;

		rv.cause = Expiration.Cause.closedDate;

		// how long from now till endTime?
		long tillExpires = endTime - now.getTime();
		if (tillExpires <= 0) tillExpires = 0;

		rv.duration = new Long(tillExpires);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsClosed()
	{
		if (this.assessment.getArchived()) return Boolean.TRUE;
		if (!this.assessment.getPublished()) return Boolean.TRUE;

		// if there is no end to submissions, we are never closed
		if (getSubmitUntilDate() == null) return Boolean.FALSE;

		// we are closed if after the submit until date
		Date now = new Date();
		if (now.after(getSubmitUntilDate())) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsLate()
	{
		Date acceptUntil = getAcceptUntilDate();
		Date due = getDueDate();
		if ((acceptUntil != null) && (due != null))
		{
			Date now = new Date();
			return Boolean.valueOf(now.after(due) && now.before(acceptUntil));
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsOpen(Boolean withGrace)
	{
		if (this.assessment.getArchived()) return Boolean.FALSE;
		if (!this.assessment.getPublished()) return Boolean.FALSE;

		Date now = new Date();
		long grace = withGrace ? MnemeService.GRACE : 0l;

		// if we have an open date and we are not there yet
		if ((getOpenDate() != null) && (now.before(getOpenDate()))) return Boolean.FALSE;

		// if we have a submit-until date and we are past it, considering grace
		if ((getSubmitUntilDate() != null) && (now.getTime() > (getSubmitUntilDate().getTime() + grace))) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// open, if defined, must be before acceptUntil and due, if defined
		if ((getOpenDate() != null) && (getDueDate() != null) && (!getOpenDate().before(getDueDate()))) return Boolean.FALSE;
		if ((getOpenDate() != null) && (getAcceptUntilDate() != null) && (!getOpenDate().before(getAcceptUntilDate()))) return Boolean.FALSE;

		// due, if defined, must be not after acceptUntil, if defined
		if ((getDueDate() != null) && (getAcceptUntilDate() != null) && (getDueDate().after(getAcceptUntilDate()))) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getSubmitUntilDate()
	{
		// this is the acceptUntil date, if defined, or the due date.
		Date closedDate = getAcceptUntilDate();
		if (closedDate == null) closedDate = getDueDate();
		return closedDate;
	}
}
