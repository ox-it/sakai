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

import java.io.PrintWriter;

import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Fragment;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.w3c.dom.Element;

/**
 * UiFragment implements Fragment.
 */
public class UiFragment extends UiContainer implements Fragment
{
	/** The message bundle to use while rendering this fragment. */
	protected InternationalizedMessages messages = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiFragment()
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
	protected UiFragment(UiServiceImpl service, Element xml)
	{
		// do the container thing
		super(service, xml);
	}

	/**
	 * {@inheritDoc}
	 */
	public Fragment add(Component component)
	{
		super.add(component);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		if (this.contained.isEmpty()) return false;

		// setup the custom messages
		if (this.messages != null)
		{
			context.addMessages(this.messages);
		}

		// render the contained
		for (Component c : this.contained)
		{
			c.render(context, focus);
		}

		// remove the custom messages
		if (this.messages != null)
		{
			context.popMessages();
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Fragment setMessages(InternationalizedMessages messages)
	{
		this.messages = messages;
		return this;
	}
}
