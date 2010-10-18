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

import org.etudes.mneme.api.Ent;

/**
 * EntImpl implements Ent
 */
public class EntImpl implements Ent
{
	protected String description = null;

	protected String id = null;

	protected Boolean marked = Boolean.FALSE;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public EntImpl(EntImpl other)
	{
		set(other);
	}

	/**
	 * Construct.
	 */
	public EntImpl(String id, String description)
	{
		setId(id);
		setDescription(description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// two EntImpl are equals if they have the same id
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		if ((this.id == null) || (((EntImpl) obj).id == null)) return false;
		return this.id.equals(((EntImpl) obj).id);
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
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMarked()
	{
		return this.marked;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMarked(Boolean marked)
	{
		this.marked = marked;
	}

	/**
	 * Set as a copy of the other.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(EntImpl other)
	{
		this.description = other.description;
		this.id = other.id;
		this.marked = other.marked;
	}
}
