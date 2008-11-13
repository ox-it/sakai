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

import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.ComponentPropertyReference;
import org.etudes.ambrosia.api.Context;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiComponentPropertyReference implements ComponentPropertyReference
 */
public class UiComponentPropertyReference extends UiPropertyReference implements ComponentPropertyReference
{
	/** Components contained in this container. */
	protected List<Component> contained = new ArrayList<Component>();

	/**
	 * No-arg constructor.
	 */
	public UiComponentPropertyReference()
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
	protected UiComponentPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// find the first container child node
		Element container = XmlHelper.getChildElementNamed(xml, "container");

		if (container != null)
		{
			NodeList contained = container.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element componentXml = (Element) node;

					// create a component from each node in the container
					Component c = service.parseComponent(componentXml);
					if (c != null)
					{
						add(c);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ComponentPropertyReference add(Component component)
	{
		contained.add(component);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "component";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		// set the context to capture instead of adding to the output
		context.setCollecting();

		// render the contained
		for (Component c : this.contained)
		{
			c.render(context, focus);
		}

		// get the captured text, resetting to output mode
		String rv = context.getCollected();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readObject(Context context, Object focus)
	{
		// if the name is not set, see if we can get a value from the ref
		if (this.contained.isEmpty())
		{
			return super.readObject(context, focus);
		}

		return read(context, focus);
	}
}
