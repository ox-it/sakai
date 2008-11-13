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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiMessage is a message from the message bundle that can have property reference parameters.
 */
public class UiMessage implements Message
{
	/** A list of alternate selector decisions. */
	protected List<Decision> alternateSelectorDecisions = new ArrayList<Decision>();

	/** A list of alternate selectors. */
	protected List<String> alternateSelectors = new ArrayList<String>();

	/** A set of additional properties to put in the message. */
	protected PropertyReference[] references = null;

	/** The message selector. */
	protected String selector = null;

	/** The message pattern itself. */
	protected String template = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiMessage()
	{

	}

	/**
	 * Construct from a dom element. The definition may be in the element or in a child.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiMessage(UiServiceImpl service, Element xml)
	{
		// xml may be tag name "message". If not, find one in the children
		if (!xml.getTagName().equals("message"))
		{
			xml = XmlHelper.getChildElementNamed(xml, "message");
		}

		if (xml != null)
		{
			List<PropertyReference> refs = new ArrayList<PropertyReference>();

			String selector = StringUtil.trimToNull(xml.getAttribute("selector"));
			String template = StringUtil.trimToNull(xml.getAttribute("template"));

			// short for model
			String ref = StringUtil.trimToNull(xml.getAttribute("model"));
			if (ref != null)
			{
				PropertyReference pRef = service.newPropertyReference().setReference(ref);
				if (pRef != null) refs.add(pRef);
			}

			// use all the direct model references
			NodeList settings = xml.getChildNodes();
			for (int i = 0; i < settings.getLength(); i++)
			{
				Node node = settings.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element settingsXml = (Element) node;
					PropertyReference pRef = service.parsePropertyReference(settingsXml);
					if (pRef != null) refs.add(pRef);
				}
			}

			// alternate selectors
			Element settingsXml = XmlHelper.getChildElementNamed(xml, "selectors");
			if (settingsXml != null)
			{
				NodeList contained = settingsXml.getChildNodes();
				for (int i = 0; i < contained.getLength(); i++)
				{
					Node node = contained.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE)
					{
						Element containedXml = (Element) node;
						if ("selector".equals(containedXml.getTagName()))
						{
							Decision d = service.parseDecisions(containedXml);
							String altSelector = StringUtil.trimToNull(containedXml.getAttribute("selector"));
							addSelector(altSelector, d);
						}
					}
				}
			}

			// convert the refs into an array
			PropertyReference[] refsArray = new PropertyReference[0];
			refsArray = refs.toArray(refsArray);

			// set
			this.selector = selector;
			this.template = template;
			this.references = refsArray;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Message addSelector(String selector, Decision decision)
	{
		this.alternateSelectors.add(selector);
		this.alternateSelectorDecisions.add(decision);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage(Context context, Object focus)
	{
		return getMessage(context, focus, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage(Context context, Object focus, Object[] extraArgs)
	{
		int extraLength = (extraArgs == null) ? 0 : extraArgs.length;

		// pick the selector
		String sel = this.selector;
		for (int i = 0; i < this.alternateSelectorDecisions.size(); i++)
		{
			Decision d = this.alternateSelectorDecisions.get(i);
			if (d.decide(context, focus))
			{
				sel = this.alternateSelectors.get(i);
				break;
			}
		}

		// if no references, use just the selector message
		if (((this.references == null) || (this.references.length == 0)) && (extraLength == 0))
		{
			if (this.template != null)
			{
				return StringUtil.trimToZero(this.template);
			}
			if (sel != null)
			{
				return StringUtil.trimToZero(context.getMessages().getString(sel));
			}
			return "";
		}

		// if there is no selector (and no template), just read the first reference as the value
		if ((sel == null) && (this.template == null))
		{
			if ((this.references != null) && (this.references.length == 1))
			{
				return StringUtil.trimToZero(this.references[0].read(context, focus));
			}
			else if (extraLength == 1)
			{
				return StringUtil.trimToZero(extraArgs[0].toString());
			}
			return "";
		}

		// put the property reference into args for the message
		Object args[] = new Object[this.references.length + extraLength];
		int i = 0;
		for (PropertyReference reference : this.references)
		{
			String value = reference.read(context, focus);

			// null becomes ""
			if (value == null) value = "";

			args[i++] = value;
		}

		// add in the extras
		if (extraLength > 0)
		{
			for (Object extra : extraArgs)
			{
				args[i++] = (extra == null) ? "" : extra.toString();
			}
		}

		// String msg = StringUtil.trimToZero(context.getMessages().getFormattedMessage(sel, args));
		// get the message with no args replaced
		// Note: this is to avoid a possible bug with certain complex arguments, perhaps -ggolden
		String msg = null;
		if (this.template != null)
		{
			msg = StringUtil.trimToZero(this.template);
		}
		else
		{
			msg = StringUtil.trimToZero(context.getMessages().getString(sel));
		}

		// replace the args
		for (int arg = 0; arg < this.references.length + extraLength; arg++)
		{
			msg = msg.replace("{" + arg + "}", (String) args[arg]);
		}

		return msg;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message setMessage(String selector, PropertyReference... references)
	{
		this.selector = selector;
		this.references = references;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message setTemplate(String template, PropertyReference... references)
	{
		this.template = template;
		this.references = references;
		return this;
	}
}
