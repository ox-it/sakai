/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Text;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiText implements Text.
 */
public class UiText extends UiComponent implements Text
{
	/** The message that will provide text to display. */
	protected Message message = null;

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/**
	 * Construct.
	 */
	public UiText()
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
	protected UiText(UiServiceImpl service, Element xml)
	{
		// do the component stuff
		super(service, xml);

		// short for for text - attribute "selector" for the selector, and attribute "model" for a single reference.
		String selector = StringUtil.trimToNull(xml.getAttribute("selector"));
		String ref = StringUtil.trimToNull(xml.getAttribute("model"));
		if ((selector != null) || (ref != null))
		{
			if (ref == null)
			{
				setText(selector);
			}
			else
			{
				setText(selector, service.newPropertyReference().setReference(ref));
			}
		}

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// title
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.titleMessage = new UiMessage(service, settingsXml);
		}

		// message
		settingsXml = XmlHelper.getChildElementNamed(xml, "message");
		if (settingsXml != null)
		{
			this.message = new UiMessage(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		String msg = null;
		String titleMsg = null;
		if (this.message != null)
		{
			msg = this.message.getMessage(context, focus);
		}
		if (this.titleMessage != null)
		{
			titleMsg = this.titleMessage.getMessage(context, focus);
		}

		if ((msg == null) && (titleMsg == null)) return false;

		// set some ids
		int idRoot = context.getUniqueId();
		String id = (this.id != null) ? this.id : this.getClass().getSimpleName() + "_" + idRoot;

		PrintWriter response = context.getResponseWriter();

		// title
		if (titleMsg != null)
		{
			response.print("<div class=\"ambrosiaComponentTitle\">");
			response.print(titleMsg);
			response.println("</div>");
		}

		if (msg != null)
		{
			response.print("<span id=\"" + id + "\">");
			response.println(msg);
			response.println("</span>");
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Text setText(String selector, PropertyReference... references)
	{
		this.message = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Text setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
