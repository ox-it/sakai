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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.EntityDisplay;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.EntityDisplayRow;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiEntityDisplay implements EntityDisplay.
 */
public class UiEntityDisplay extends UiComponent implements EntityDisplay
{
	/** The reference to the entity to display. */
	protected PropertyReference entityReference = null;

	/** Rows for this list. */
	protected List<EntityDisplayRow> rows = new ArrayList<EntityDisplayRow>();

	/** The message for the title. */
	protected Message title = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiEntityDisplay()
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
	protected UiEntityDisplay(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			this.entityReference = pRef;
		}

		// short form for title
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// model
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) this.entityReference = pRef;
		}

		// title
		settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			this.title = new UiMessage(service, settingsXml);
		}

		// rows
		settingsXml = XmlHelper.getChildElementNamed(xml, "rows");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element containedXml = (Element) node;

					// let the service parse this as a column
					EntityDisplayRow row = service.parseEntityDisplayRow(containedXml);
					if (row != null) this.rows.add(row);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplay addRow(EntityDisplayRow row)
	{
		this.rows.add(row);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		PrintWriter response = context.getResponseWriter();

		// title
		if (this.title != null)
		{
			response.println("<div class =\"ambrosiaInstructions\">" + this.title.getMessage(context, focus) + "</div>");
		}

		// the object
		Object entity = null;
		if (this.entityReference != null)
		{
			entity = this.entityReference.readObject(context, focus);
		}

		// start the table
		response.println("<table class=\"ambrosiaEntityDisplay\">");

		// rows
		for (EntityDisplayRow r : this.rows)
		{
			if (!r.isIncluded(context, focus)) continue;

			// row header
			response.print("<tr><th>");
			if (r.getTitle() != null)
			{
				response.print(r.getTitle().getMessage(context, entity));
			}
			response.print("</th><td>");

			// get the row's value for display
			r.render(context, entity);

			response.println("</td></tr>");
		}

		response.println("</table>");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplay setEntityReference(PropertyReference entityReference)
	{
		this.entityReference = entityReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplay setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}
}
