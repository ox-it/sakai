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

import org.etudes.mneme.api.Changeable;

/**
 * ChangelableImpl implements Changeable
 */
public class ChangeableImpl implements Changeable
{
	/** Track any changes at all. */
	protected transient Boolean changed = Boolean.FALSE;

	/**
	 * Construct.
	 */
	public ChangeableImpl()
	{

	}

	/**
	 * Construct from another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public ChangeableImpl(Changeable other)
	{
		this.changed = other.getChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearChanged()
	{
		this.changed = Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getChanged()
	{
		return this.changed;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChanged()
	{
		this.changed = Boolean.TRUE;
	}
}
