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

import org.etudes.mneme.api.Attribution;
import org.etudes.mneme.api.Changeable;

/**
 * AttributionImpl implements Attribution
 */
public class AttributionImpl implements Attribution
{
	protected Date date = null;

	protected transient Changeable owner = null;

	protected String userId = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public AttributionImpl(AttributionImpl other, Changeable owner)
	{
		this(owner);
		set(other);
	}

	/**
	 * Construct.
	 */
	public AttributionImpl(Changeable owner)
	{
		this.owner = owner;
	}

	/**
	 * Construct.
	 * 
	 * @param date
	 *        The date.
	 * @param userId
	 *        The user id.
	 */
	public AttributionImpl(Date date, String userId, Changeable owner)
	{
		this(owner);
		this.date = date;
		this.userId = userId;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getDate()
	{
		return this.date;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserId()
	{
		return this.userId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDate(Date date)
	{
		if (!Different.different(this.date, date)) return;

		this.date = date;

		if (this.owner != null) this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUserId(String userId)
	{
		if ((userId != null) && (userId.length() > 99)) throw new IllegalArgumentException();
		if (!Different.different(this.userId, userId)) return;
		this.userId = userId;

		if (this.owner != null) this.owner.setChanged();
	}

	/**
	 * Init the date.
	 * 
	 * @param date
	 *        The date.
	 */
	protected void initDate(Date date)
	{
		this.date = date;
	}

	/**
	 * Init the user id.
	 * 
	 * @param userId
	 *        The user id.
	 */
	protected void initUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(AttributionImpl other)
	{
		this.date = other.date;
		this.userId = other.userId;
	}
}
