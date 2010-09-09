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

package org.etudes.ambrosia.impl;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.IconPropertyReference;
import org.etudes.ambrosia.api.Message;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiIconPropertyReference implements IconPropertyReference
 */
public class UiIconPropertyReference extends UiPropertyReference implements IconPropertyReference
{
	protected String name = null;

	protected Message titleMessage = null;

	/**
	 * No-arg constructor.
	 */
	public UiIconPropertyReference()
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
	protected UiIconPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// icon
		String icon = StringUtil.trimToNull(xml.getAttribute("icon"));
		if (icon != null) setIcon(icon);

		// title message
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.titleMessage = new UiMessage(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "icon";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		String iconName = this.name;

		// if the name is not set, see if we can get a value from the ref
		if (iconName == null)
		{
			iconName = super.read(context, focus);
		}

		// if nothing found, return nothing
		if (iconName == null) return null;

		String title = "";
		if (this.titleMessage != null)
		{
			title = this.titleMessage.getMessage(context, focus);
			if (title == null) title = "";
		}

		return "<img src=\"" + context.getUrl(iconName) + "\" alt=\"" + title + "\" style=\"border-style: none;\" title=\"" + title + "\" />";
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readObject(Context context, Object focus)
	{
		// if the name is not set, see if we can get a value from the ref
		if (this.name == null)
		{
			return super.readObject(context, focus);
		}

		return read(context, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public IconPropertyReference setIcon(String name)
	{
		this.name = name;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public IconPropertyReference setTitle(Message title)
	{
		this.titleMessage = title;
		return this;
	}
}
