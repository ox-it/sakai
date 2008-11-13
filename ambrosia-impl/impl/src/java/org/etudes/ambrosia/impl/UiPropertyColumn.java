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
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyColumn;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiPropertyColumn describes one column of a UiEntityList...
 */
public class UiPropertyColumn extends UiEntityListColumn implements PropertyColumn
{
	/** An alternate source for the column display values - formatted with a message and properties. */
	protected Message propertyMessage = null;

	/** The PropertyReference for this column. */
	protected PropertyReference propertyReference = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiPropertyColumn()
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
	protected UiPropertyColumn(UiServiceImpl service, Element xml)
	{
		// EntityListColumn stuff
		super(service, xml);

		// short for model
		String ref = StringUtil.trimToNull(xml.getAttribute("model"));
		if (ref != null)
		{
			setProperty(service.newPropertyReference().setReference(ref));
		}

		// model
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}

		// message
		settingsXml = XmlHelper.getChildElementNamed(xml, "message");
		if (settingsXml != null)
		{
			this.propertyMessage = new UiMessage(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayText(Context context, Object entity, int row, String id, int size)
	{
		String rv = "";

		// use the formatted property
		if (this.propertyMessage != null)
		{
			rv = this.propertyMessage.getMessage(context, entity);
		}

		// or the plain property
		else if (this.propertyReference != null)
		{
			rv = this.propertyReference.read(context, entity);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyColumn setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyColumn setProperty(String selector, PropertyReference... references)
	{
		this.propertyMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
