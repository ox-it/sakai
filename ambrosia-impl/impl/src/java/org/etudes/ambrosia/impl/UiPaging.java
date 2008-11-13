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

import org.etudes.ambrosia.api.Paging;
import org.sakaiproject.util.StringUtil;

/**
 * UiPaging implements Paging.
 */
public class UiPaging implements Paging
{
	/** The current page number (1 based). */
	protected Integer current = new Integer(1);

	/** The max number of items. */
	protected Integer maxItems = new Integer(0);

	/** the items-per-page size. */
	protected Integer size = new Integer(0);

	/**
	 * Construct.
	 */
	public UiPaging()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param current
	 *        The current page position
	 * @param maxItems
	 *        The max number of items.
	 * @param size
	 *        The per-page size.
	 */
	public UiPaging(Integer current, Integer maxItems, Integer size)
	{
		this.current = current;
		this.maxItems = maxItems;
		this.size = size;
		validate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getCurFirstItem()
	{
		int rv = ((this.current - 1) * this.size) + 1;
		if (rv <= 0) rv = 1;

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getCurLastItem()
	{
		if (this.size == 0) return this.maxItems;

		int rv = this.current * this.size;
		if (rv > this.maxItems) rv = this.maxItems;

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getCurrent()
	{
		return this.current;
	}

	/**
	 * {@inheritDoc}
	 */
	public Paging getFirst()
	{
		return new UiPaging(1, this.maxItems, this.size);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsFirst()
	{
		return this.current == 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsLast()
	{
		return this.current == getMax();
	}

	/**
	 * {@inheritDoc}
	 */
	public Paging getLast()
	{
		return new UiPaging(getMax(), this.maxItems, this.size);
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getMax()
	{
		if ((this.size == 0) || (this.maxItems == 0)) return 1;
		return ((this.maxItems - 1) / this.size) + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getMaxItems()
	{
		return this.maxItems;
	}

	/**
	 * {@inheritDoc}
	 */
	public Paging getNext()
	{
		return new UiPaging(this.current + 1, this.maxItems, this.size);
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getPageForItem(Integer item)
	{
		if (this.size == 0) return 1;
		return ((item - 1) / this.size) + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public Paging getPrev()
	{
		return new UiPaging(this.current - 1, this.maxItems, this.size);
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getSize()
	{
		return this.size;
	}

	/**
	 * {@inheritDoc}
	 */
	public Paging resize(Integer size)
	{
		if (size == null) throw new IllegalArgumentException();

		Paging rv = new UiPaging(1, this.maxItems, size);
		rv.setCurrent(rv.getPageForItem(getCurFirstItem()));

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCurrent(Integer current)
	{
		if (current == null) throw new IllegalArgumentException();
		this.current = current;
		validate();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCurrentAndSize(String currentDashSize)
	{
		try
		{
			String[] parts = StringUtil.split(currentDashSize, "-");
			this.current = Integer.parseInt(parts[0]);
			this.size = Integer.parseInt(parts[1]);
		}
		catch (Throwable e)
		{
			throw new IllegalArgumentException();
		}
		validate();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxItems(Integer max)
	{
		if (max == null) throw new IllegalArgumentException();
		this.maxItems = max;
		validate();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPageSize(Integer size)
	{
		if (size == null) throw new IllegalArgumentException();
		this.size = size;
		validate();
	}

	protected void validate()
	{
		if (this.size < 0) this.size = 0;
		if (this.maxItems < 0) this.maxItems = 0;
		if (this.current < 1) this.current = 1;
		if (this.current > getMax()) this.current = getMax();
	}
}
