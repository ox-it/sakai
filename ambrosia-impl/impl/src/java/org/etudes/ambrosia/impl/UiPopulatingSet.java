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

package org.etudes.ambrosia.impl;

import java.util.HashSet;
import java.util.Set;

import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;

/**
 * UiPopulatingSet implements PopulatingSet.
 */
public class UiPopulatingSet implements PopulatingSet
{
	protected Factory factory = null;

	protected Id id = null;

	protected Set set = new HashSet();

	/**
	 * Construct
	 */
	public UiPopulatingSet(Factory factory, Id id)
	{
		this.factory = factory;
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object assure(String id)
	{
		for (Object entity : this.set)
		{
			if (this.id.getId(entity).equals(id))
			{
				return entity;
			}
		}

		// not found - get one from the factory
		Object entity = this.factory.get(id);

		if (entity != null)
		{
			this.set.add(entity);
		}

		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getSet()
	{
		return this.set;
	}
}
