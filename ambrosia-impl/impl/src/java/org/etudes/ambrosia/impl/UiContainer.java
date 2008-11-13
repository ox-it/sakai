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

import org.etudes.ambrosia.api.Container;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiContainer implements Container.
 */
public class UiContainer extends UiComponent implements Container
{
	/** Components contained in this container. */
	protected List<Component> contained = new ArrayList<Component>();

	/**
	 * Public no-arg constructor.
	 */
	public UiContainer()
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
	protected UiContainer(UiServiceImpl service, Element xml)
	{
		// do the component thing
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
	public Container add(Component component)
	{
		contained.add(component);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Component> findComponents(String id)
	{
		List<Component> rv = new ArrayList<Component>();

		if (id == null) return rv;

		// search the contained
		for (Component c : this.contained)
		{
			// this one?
			if (id.equals(c.getId())) rv.add(c);

			// if a container, search in there
			if (c instanceof Container)
			{
				List<Component> found = ((Container) c).findComponents(id);
				rv.addAll(found);
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Component> getContained()
	{
		return this.contained;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// render the contained
		for (Component c : this.contained)
		{
			response.println("<div class=\"ambrosiaContainerComponent\">");
			c.render(context, focus);
			response.println("</div>");
		}

		return true;
	}
}
