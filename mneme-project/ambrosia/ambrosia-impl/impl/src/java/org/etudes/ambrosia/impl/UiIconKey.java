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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.IconKey;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiIconKey presents a key to icon use for some other part of the interface using icons. Each icon is shown with a description.
 */
public class UiIconKey extends UiComponent implements IconKey
{
	/** The reference to the description property in each entity. */
	protected PropertyReference descriptionReference = null;

	/** The list of icon descriptions pre-set into the tool (used instead of the reference if defined. */
	protected List<Message> iconDescriptions = new ArrayList<Message>();

	/** The reference to the icon URL property in each entity. */
	protected PropertyReference iconReference = null;

	/** The list of icon URLs pre-set into the tool (used instead of the reference if defined. */
	protected List<String> iconUrls = new ArrayList<String>();

	/** The reference to the Collection of entities to list. */
	protected PropertyReference keysReference = null;

	/** The message for the title. */
	protected Message title = null;

	/** The column width. */
	protected String width = "16px";

	/**
	 * No-arg constructor.
	 */
	public UiIconKey()
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
	protected UiIconKey(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// icons
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "icons");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element iconXml = (Element) node;
					if ("icon".equals(iconXml.getTagName()))
					{
						String icon = StringUtil.trimToNull(iconXml.getAttribute("icon"));
						Message message = null;

						String messageStr = StringUtil.trimToNull(iconXml.getAttribute("message"));
						if (messageStr != null)
						{
							message = new UiMessage().setMessage(messageStr);
						}

						Element messageXml = XmlHelper.getChildElementNamed(iconXml, "message");
						if (messageXml != null)
						{
							message = new UiMessage(service, messageXml);
						}

						addIcon(icon, message);
					}
				}
			}
		}

		// description reference
		settingsXml = XmlHelper.getChildElementNamed(xml, "descriptionModel");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.descriptionReference = service.parsePropertyReference(innerXml);
			}
		}

		// icon reference
		settingsXml = XmlHelper.getChildElementNamed(xml, "iconsModel");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.iconReference = service.parsePropertyReference(innerXml);
			}
		}

		// keys reference
		settingsXml = XmlHelper.getChildElementNamed(xml, "keysModel");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.keysReference = service.parsePropertyReference(innerXml);
			}
		}

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// title
		settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			this.title = new UiMessage(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IconKey addIcon(String url, Message description)
	{
		this.iconUrls.add(url);
		this.iconDescriptions.add(description);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		if (this.iconUrls.size() > 0)
		{
			renderInternal(context, focus);
		}
		else
		{
			renderReferences(context, focus);
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public IconKey setDescriptionReference(PropertyReference descriptionReference)
	{
		this.descriptionReference = descriptionReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public IconKey setIconReference(PropertyReference iconReference)
	{
		this.iconReference = iconReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public IconKey setKeysReference(PropertyReference keysReference)
	{
		this.keysReference = keysReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public IconKey setTitle(String selector, PropertyReference... properties)
	{
		this.title = new UiMessage().setMessage(selector, properties);
		return this;
	}

	/**
	 * Render from the internally set data
	 */
	protected void renderInternal(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// the set of objects describing the icons are in this.iconUrls and this.iconDescriptions
		boolean empty = this.iconUrls.isEmpty();

		// title, if there is one and there is data
		if ((this.title != null) && (!empty))
		{
			response.println("<div class =\"ambrosiaInstructions\">" + this.title.getMessage(context, focus) + "</div>");
		}

		// start the table
		response.println("<table class=\"ambrosiaIconKey\" cellpadding=\"0\" cellspacing=\"0\" >");

		// data
		if (!empty)
		{
			for (int i = 0; i < this.iconUrls.size(); i++)
			{
				response.print("<tr><td style=\"width:" + this.width + "; text-align:center; vertical-align:middle;\">");

				// the icon URL
				String icon = this.iconUrls.get(i);

				// the description
				Message description = this.iconDescriptions.get(i);
				String descriptionText = null;
				if (description != null) descriptionText = description.getMessage(context, focus);
				if (descriptionText == null) descriptionText = "";

				// Note: since the description follows, don't put it on the icon as well.
				if (icon != null)
				{
					response.print("<img src=\"" + context.getUrl(icon) + "\" style=\"border-style: none;\" title=\"\" alt=\"\" />");
				}

				response.print("</td><td>");

				// the description
				response.print(descriptionText);

				response.println("</td></tr>");
			}
		}

		response.println("</table>");
	}

	/**
	 * Render from data in the references.
	 */
	protected void renderReferences(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// the set of objects describing the icons
		Collection data = (Collection) this.keysReference.readObject(context, focus);
		boolean empty = ((data == null) || (data.isEmpty()));

		// title, if there is one and there is data
		if ((this.title != null) && (!empty))
		{
			response.println("<div class =\"ambrosiaInstructions\">" + this.title.getMessage(context, focus) + "</div>");
		}

		// start the table
		response.println("<table class=\"ambrosiaIconKey\" cellpadding=\"0\" cellspacing=\"0\" >");

		// data
		if (!empty)
		{
			for (Object entity : data)
			{
				response.print("<tr><td>");

				// the icon
				String icon = null;
				if (this.iconReference != null) icon = this.iconReference.read(context, entity);

				// the description
				String description = null;
				if (this.descriptionReference != null) description = this.descriptionReference.read(context, entity);
				if (description == null) description = "";

				// Note: since the description follows, don't put it on the icon as well.
				if (icon != null)
				{
					response.print("<img src=\"" + context.getUrl(icon) + "\" style=\"border-style: none;\" title=\"\" alt=\"\" />");
				}

				response.print("</td><td>");

				// the description
				response.print(description);

				response.println("</td></tr>");
			}
		}

		response.println("</table>");
	}
}
