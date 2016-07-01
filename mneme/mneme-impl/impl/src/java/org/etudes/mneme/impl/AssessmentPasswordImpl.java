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

import org.etudes.mneme.api.AssessmentPassword;
import org.etudes.mneme.api.Changeable;

/**
 * AssessmentPasswordImpl implements AssessmentPassword
 */
public class AssessmentPasswordImpl implements AssessmentPassword
{
	protected transient Changeable owner = null;

	protected String password = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public AssessmentPasswordImpl(AssessmentPasswordImpl other, Changeable owner)
	{
		this(owner);
		set(other);
	}

	/**
	 * Construct.
	 */
	public AssessmentPasswordImpl(Changeable owner)
	{
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean checkPassword(String password)
	{
		if (password == null) return Boolean.FALSE;
		return password.equals(this.password);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPassword()
	{
		return this.password;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPassword(String password)
	{
		// massage the password
		if (password != null)
		{
			password = password.trim();
			if (password.length() > 255) password = password.substring(0, 255);
			if (password.length() == 0) password = null;
		}

		if (!Different.different(this.password, password)) return;

		this.password = password;

		this.owner.setChanged();
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(AssessmentPasswordImpl other)
	{
		this.password = other.password;
	}
}
