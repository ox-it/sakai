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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.EnumPropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiEnumPropertyReference implements EnumPropertyReference
 */
public class UiEnumPropertyReference extends UiPropertyReference implements EnumPropertyReference
{
	/** The root message selector. */
	protected String selector = null;

	/**
	 * No-arg constructor.
	 */
	public UiEnumPropertyReference()
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
	protected UiEnumPropertyReference(UiServiceImpl service, Element xml)
	{
		// property reference stuff
		super(service, xml);

		// selector
		String selector = StringUtil.trimToNull(xml.getAttribute("messageRoot"));
		if (selector != null) setSelector(selector);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "enum";
	}

	/**
	 * {@inheritDoc}
	 */
	public EnumPropertyReference setSelector(String root)
	{
		this.selector = root;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (value == null) return super.format(context, value);

		// the message formed from the root and the value
		String messageSelector = ((this.selector == null) ? "" : this.selector) + value.toString();
		return context.getMessages().getString(messageSelector);
	}
}
