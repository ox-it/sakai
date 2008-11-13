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

import java.util.ArrayList;
import java.util.List;

import org.etudes.ambrosia.api.GridRow;

/**
 * UiGridRow implements GridRow
 */
public class UiGridRow implements GridRow
{
	protected List<Object> columns = new ArrayList<Object>();

	/**
	 * {@inheritDoc}
	 */
	public Object getCol01()
	{
		if (this.columns.size() >= 1)
		{
			return this.columns.get(0);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol02()
	{
		if (this.columns.size() >= 2)
		{
			return this.columns.get(1);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol03()
	{
		if (this.columns.size() >= 3)
		{
			return this.columns.get(2);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol04()
	{
		if (this.columns.size() >= 4)
		{
			return this.columns.get(3);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol05()
	{
		if (this.columns.size() >= 5)
		{
			return this.columns.get(4);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol06()
	{
		if (this.columns.size() >= 6)
		{
			return this.columns.get(5);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol07()
	{
		if (this.columns.size() >= 7)
		{
			return this.columns.get(6);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol08()
	{
		if (this.columns.size() >= 8)
		{
			return this.columns.get(7);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getCol09()
	{
		if (this.columns.size() >= 9)
		{
			return this.columns.get(8);
		}

		return null;
	}

	/**
	 * Add an object.
	 * 
	 * @param o
	 *        The object to add.
	 */
	protected void add(Object o)
	{
		this.columns.add(o);
	}
}
