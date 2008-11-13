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
import java.util.ArrayList;
import java.util.List;

import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.EntityDisplayRow;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiEntityDisplayRow implements EntityDisplayRow.
 */
public class UiEntityDisplayRow extends UiContainer implements EntityDisplayRow
{
	/** The PropertyReference for this row. */
	protected PropertyReference propertyReference = null;

	/** The message for the column title. */
	protected Message title = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiEntityDisplayRow()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        The UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiEntityDisplayRow(UiServiceImpl service, Element xml)
	{
		// do the container stuff
		super(service, xml);

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
		}

		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.title = new UiMessage(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object entity)
	{
		// start with the property
		if (getProperty() != null)
		{
			PrintWriter response = context.getResponseWriter();
			String value = getProperty().read(context, entity);
			response.print(value);
		}

		// render the contained
		for (Component c : this.contained)
		{
			c.render(context, entity);
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference getProperty()
	{
		return this.propertyReference;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getTitle()
	{
		return this.title;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplayRow setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplayRow setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}
}
