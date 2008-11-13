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

package org.etudes.ambrosia.util;

import org.etudes.ambrosia.api.FormatDelegate;
import org.etudes.ambrosia.api.UiService;

/**
 * A FormatDelegate
 */
public abstract class FormatDelegateImpl implements FormatDelegate
{
	/** The id. */
	protected String id = null;

	/** The tool id. */
	protected String toolId = null;

	/** ui service reference. */
	protected UiService uiService = null;

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// register
		this.uiService.registerFormatDelegate(this, this.id, this.toolId);
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 *        The id.
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Set the tool id.
	 * 
	 * @param id
	 *        The tool id.
	 */
	public void setToolId(String id)
	{
		this.toolId = id;
	}

	/**
	 * Set the UI service.
	 * 
	 * @param service
	 *        The UI service.
	 */
	public void setUi(UiService service)
	{
		this.uiService = service;
	}
}
