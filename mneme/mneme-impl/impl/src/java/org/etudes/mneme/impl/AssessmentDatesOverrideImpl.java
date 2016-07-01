/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.AssessmentDates;

/**
 * AssessmentDatesOverrideImpl implements AssessmentDates, merging a main dates impl with an AssesmentAccess impl.
 */
public class AssessmentDatesOverrideImpl extends AssessmentDatesBaseImpl implements AssessmentDates
{
	protected AssessmentAccess access = null;

	protected AssessmentDates main = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public AssessmentDatesOverrideImpl(Assessment main, AssessmentAccess access)
	{
		super(main);
		this.main = main.getDates();
		this.access = access;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getAcceptUntilDate()
	{
		if ((this.access != null) && (this.access.getOverrideAcceptUntilDate()))
		{
			return this.access.getAcceptUntilDate();
		}

		return this.main.getAcceptUntilDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getArchivedDate()
	{
		return this.main.getArchivedDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getDueDate()
	{
		if ((this.access != null) && (this.access.getOverrideDueDate()))
		{
			return this.access.getDueDate();
		}

		return this.main.getDueDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHideUntilOpen()
	{
		if (this.access != null)
		{
			return this.access.getHideUntilOpen();
		}

		return this.main.getHideUntilOpen();
	}		

	/**
	 * {@inheritDoc}
	 */
	public Date getOpenDate()
	{
		if ((this.access != null) && (this.access.getOverrideOpenDate()))
		{
			return this.access.getOpenDate();
		}
		return this.main.getOpenDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initHideUntilOpen(Boolean hideUntilOpen)
	{
		throw new IllegalArgumentException();
	}		

	/**
	 * {@inheritDoc}
	 */
	public void setAcceptUntilDate(Date date)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDueDate(Date date)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHideUntilOpen(Boolean hideUntilOpen)
	{
		throw new IllegalArgumentException();
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public void setOpenDate(Date date)
	{
		throw new IllegalArgumentException();
	}
}
