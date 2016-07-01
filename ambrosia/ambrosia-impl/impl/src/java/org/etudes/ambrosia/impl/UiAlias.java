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

import org.etudes.ambrosia.api.Alias;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Fragment;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiAlias presents implements Alias.
 */
public class UiAlias extends UiComponent implements Alias
{
	/** The id of the component to render. */
	protected String to = null;

	/** The tool id of the component to render. If set, this is global alias. */
	protected String toolId = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiAlias()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiAlias(UiServiceImpl service, Element xml)
	{
		super(service, xml);

		// to
		String to = StringUtil.trimToNull(xml.getAttribute("to"));
		if (to != null) setTo(to);

		// toolId
		String toolId = StringUtil.trimToNull(xml.getAttribute("toolId"));
		if (toolId != null) setToolId(toolId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		if (this.to == null) return false;

		// included?
		if (!isIncluded(context, focus)) return false;

		// for local access, look at siblings
		if (toolId == null)
		{
			// find the component(s)
			List<Component> components = context.findComponents(this.to);

			// render
			for (Component c : components)
			{
				// block the infinite loop of rendering me!
				if (c != this)
				{
					c.render(context, focus);
				}
			}
		}

		// for global access
		else
		{
			// get the fragment
			Fragment fragment = context.getGlobalFragment(this.to, this.toolId, focus);
			if (fragment != null)
			{
				// render
				fragment.render(context, focus);

				// clear global fragment messages
				context.clearGlobalFragment(this.to, this.toolId);
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Alias setTo(String to)
	{
		this.to = to;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Alias setToolId(String toolId)
	{
		this.toolId = toolId;
		return this;
	}
}
