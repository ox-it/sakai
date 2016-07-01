/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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

import java.util.Map;

import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.Pool;

/**
 * EmptyPartDetailImpl extends PartDetailImpl.
 */
public class EmptyPartDetailImpl extends PartDetailImpl implements PartDetail
{
	/**
	 * Construct.
	 * 
	 * @param part
	 *        The part.
	 */
	public EmptyPartDetailImpl(Part part)
	{
		super(part);
	}

	/**
	 * Construct.
	 * 
	 * @param part
	 *        The part.
	 * @param other
	 *        The other to copy.
	 */
	public EmptyPartDetailImpl(Part part, EmptyPartDetailImpl other)
	{
		super(part);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		// TODO: ?
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return ((PartImpl) this.part).messages.getFormattedMessage("phantom-description", null);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasPoints()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInvalidMessage()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsPhantom()
	{
		return Boolean.TRUE;
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
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getNonOverridePoints()
	{
		return Float.valueOf(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumQuestions()
	{
		return Integer.valueOf(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool getPool()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPoolId()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getQuestionPoints()
	{
		return Float.valueOf(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "?";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean restoreToOriginal(Map<String, String> poolIdMap, Map<String, String> questionIdMap)
	{
		return true;
	}
}
