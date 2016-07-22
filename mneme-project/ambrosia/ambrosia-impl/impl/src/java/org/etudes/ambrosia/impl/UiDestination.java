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
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiDestination forms a tool destination, from a template, with possible embedded fill-in-the-blanks, and property references to fill them in.<br />
 * The format is the same as for international messages, i.e. text {0} more text {1} etc
 */
public class UiDestination implements Destination
{
	/** A set of additional properties to put in the message. */
	protected PropertyReference[] references = null;

	/** The template. */
	protected String template = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiDestination()
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
	protected UiDestination(UiServiceImpl service, Element xml)
	{
		this.template = StringUtil.trimToNull(xml.getAttribute("template"));
		
		// short for a single reference
		String ref = StringUtil.trimToNull(xml.getAttribute("model"));
		
		// use all the direct model references
		List<PropertyReference> refs = new ArrayList<PropertyReference>();
		if (ref != null) refs.add(service.newPropertyReference().setReference(ref));
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

		if (!refs.isEmpty())
		{
			this.references = refs.toArray(new PropertyReference[refs.size()]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Destination setDestination(String template, PropertyReference... references)
	{
		this.template = template;
		this.references = references;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDestination(Context context, Object focus)
	{
		if ((this.references == null) || (this.references.length == 0)) return this.template;

		// put the property reference into args for the message
		int i = 0;
		String rv = this.template;
		for (PropertyReference ref : references)
		{
			// read the value
			String value = ref.read(context, focus);
			if (value == null) value = "";

			// replace
			rv = rv.replace("{" + Integer.toString(i++) + "}", value);
		}

		return rv;
	}
}
