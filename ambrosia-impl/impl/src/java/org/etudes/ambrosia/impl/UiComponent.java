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
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Section;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiComponent implements Component.
 */
public class UiComponent implements Component
{
	/** Sync. object for id generation. */
	static Object idSync = new Object();

	/** next automatic id to generate. */
	static long nextId = 0;

	/** The entity actions defined related to this column. */
	protected List<Component> actions = new ArrayList<Component>();

	/** The id of this element - can be referenced by an alias, for instance. */
	protected String id = null;

	/** The include decision. */
	protected Decision included = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiComponent()
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
	protected UiComponent(UiServiceImpl service, Element xml)
	{
		// included decisions
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "included");
		if (settingsXml != null)
		{
			this.included = service.parseDecisions(settingsXml);
		}

		// id - may override the auto-generated one
		String id = StringUtil.trimToNull(xml.getAttribute("id"));
		if (id != null) setId(id);

		// actions
		settingsXml = XmlHelper.getChildElementNamed(xml, "actions");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
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
						this.actions.add(c);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Component addAction(Component action)
	{
		this.actions.add(action);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isIncluded(Context context, Object focus)
	{
		if (this.included == null) return true;
		return this.included.decide(context, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component setId(String id)
	{
		// may override the auto-generated id
		this.id = id;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component setIncluded(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.included = decision[0];
			}

			else
			{
				this.included = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * Auto-generate an id.
	 */
	protected void autoId()
	{
		// allocate an id
		synchronized (idSync)
		{
			this.id = "a" + nextId++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getId(Context context)
	{
		String iteration = (String) context.get("ambrosia_iteration_index");

		return this.id + ((iteration != null) ? ("_" + iteration) : "");
	}

	/**
	 * Render an action bar if any actions are defined for the section
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus.
	 */
	public void renderActions(Context context, Object focus)
	{
		// if we have none, do nothing
		if (this.actions.isEmpty()) return;

		PrintWriter response = context.getResponseWriter();

		// the bar
		response.println("<div class=\"ambrosiaComponentActionBar\">");

		// the actions
		boolean needDivider = false;
		for (Component c : this.actions)
		{
			// render into a buffer
			context.setCollecting();
			boolean rendered = c.render(context, focus);
			String rendering = context.getCollected();

			if (rendered)
			{
				// add a divider if needed
				if (needDivider)
				{
					response.println("<span class=\"ambrosiaDivider\">&nbsp;</span>");
				}

				response.print(rendering);

				// if rendered, we need a divider
				needDivider = true;
			}
		}

		response.println("</div>");
	}
}
