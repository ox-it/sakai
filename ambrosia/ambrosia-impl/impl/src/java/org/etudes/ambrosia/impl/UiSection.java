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
import java.util.Collection;

import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Section;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiSection implements Section.
 */
public class UiSection extends UiContainer implements Section
{
	/** The message for the anchor. */
	protected Message anchor = null;

	/** To have the section not stand out from the surroundings. */
	protected boolean blended = false;

	/** To start out with the section body collapsed, expandable. */
	protected boolean collapsed = false;

	/** The inclusion decision for each entity. */
	protected Decision entityIncluded = null;

	/** The reference to an entity to focus on. */
	protected PropertyReference focusReference = null;

	/** Initial icon while collapsed. */
	protected String icon1 = "!/ambrosia_library/icons/expand.gif";

	/** Icon while expanded. */
	protected String icon2 = "!/ambrosia_library/icons/collapse.gif";

	/** Message to use if the iterator is empty. */
	protected Message iteratorEmpty = null;

	/** The context name for the current iteration object. */
	protected String iteratorName = null;

	/** The reference to an entity to iterate over. */
	protected PropertyReference iteratorReference = null;

	/** The maximum pixel height of the section (scrolls if needed). */
	protected Integer maxHeight = null;

	/** The minimum pixel height of the section (if collapsed). */
	protected Integer minHeight = null;

	/** The message for the title. */
	protected Message title = null;

	/** The highlight decision for the title. */
	protected Decision titleHighlighted = null;

	/** The include decision for the title. */
	protected Decision titleIncluded = null;

	/** The 'plain' decision for the title. */
	protected Decision titlePlain = null;

	/** The treatment. */
	protected String treatment = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiSection()
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
	protected UiSection(UiServiceImpl service, Element xml)
	{
		// do the container thing
		super(service, xml);

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
			String highlighted = StringUtil.trimToNull(settingsXml.getAttribute("highlighted"));
			if ((highlighted != null) && ("TRUE".equals(highlighted)))
			{
				this.titleHighlighted = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "highlighted");
			if (innerXml != null)
			{
				this.titleHighlighted = service.parseDecisions(innerXml);
			}

			String plain = StringUtil.trimToNull(settingsXml.getAttribute("plain"));
			if ((plain != null) && ("TRUE".equals(plain)))
			{
				this.titlePlain = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "plain");
			if (innerXml != null)
			{
				this.titlePlain = service.parseDecisions(innerXml);
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "included");
			if (innerXml != null)
			{
				this.titleIncluded = service.parseDecisions(innerXml);
			}

			this.title = new UiMessage(service, settingsXml);
		}

		// anchor
		settingsXml = XmlHelper.getChildElementNamed(xml, "anchor");
		if (settingsXml != null)
		{
			this.anchor = new UiMessage(service, settingsXml);
		}

		// treatment
		String treatment = xml.getAttribute("treatment");
		if (treatment != null)
		{
			setTreatment(treatment);
		}

		// entity included
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityIncluded");
		if (settingsXml != null)
		{
			Decision decision = service.parseDecisions(settingsXml);
			this.entityIncluded = decision;
		}

		// collapsed
		String collapsed = StringUtil.trimToNull(xml.getAttribute("collapsed"));
		if (collapsed != null)
		{
			setCollapsed(Boolean.parseBoolean(collapsed));
		}

		// blended
		String blended = StringUtil.trimToNull(xml.getAttribute("blended"));
		if (blended != null)
		{
			setBlended(Boolean.parseBoolean(blended));
		}

		// focus
		settingsXml = XmlHelper.getChildElementNamed(xml, "focusOn");
		if (settingsXml != null)
		{
			// short for model
			String model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			if (model != null)
			{
				this.focusReference = service.newPropertyReference().setReference(model);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.focusReference = service.parsePropertyReference(innerXml);
			}
		}

		// iterator
		settingsXml = XmlHelper.getChildElementNamed(xml, "iterator");
		if (settingsXml != null)
		{
			String name = StringUtil.trimToNull(settingsXml.getAttribute("name"));
			if (name != null) this.iteratorName = name;

			// short for model
			String model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			if (model != null)
			{
				this.iteratorReference = service.newPropertyReference().setReference(model);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.iteratorReference = service.parsePropertyReference(innerXml);
			}

			// if iterator is empty
			innerXml = XmlHelper.getChildElementNamed(settingsXml, "empty");
			if (innerXml != null)
			{
				this.iteratorEmpty = new UiMessage(service, innerXml);
			}
		}

		// max height
		String maxHeight = StringUtil.trimToNull(xml.getAttribute("maxHeight"));
		if (maxHeight != null)
		{
			try
			{
				this.maxHeight = Integer.parseInt(maxHeight);
			}
			catch (NumberFormatException e)
			{
			}
		}

		// min height
		String minHeight = StringUtil.trimToNull(xml.getAttribute("minHeight"));
		if (minHeight != null)
		{
			try
			{
				this.minHeight = Integer.parseInt(minHeight);
			}
			catch (NumberFormatException e)
			{
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// the focus
		if (this.focusReference != null)
		{
			Object f = this.focusReference.readObject(context, focus);
			if (f != null)
			{
				focus = f;
			}
		}

		// included?
		if (!isIncluded(context, focus)) return false;

		// the iterator
		Object iterator = null;
		if (this.iteratorReference != null)
		{
			iterator = this.iteratorReference.readObject(context, focus);
		}

		// if iterating over a Collection, we will repeat our contents once for each one
		if ((iterator != null) && (iterator instanceof Collection))
		{
			Collection c = (Collection) iterator;
			int index = -1;
			if (c.isEmpty())
			{
				if (this.iteratorEmpty != null)
				{
					response.println("<div class =\"ambrosiaInstructions\">" + this.iteratorEmpty.getMessage(context, focus) + "</div>");
				}

				return true;
			}

			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

				// place the iteration index
				context.put("ambrosia_iteration_index", Integer.toString(index));

				// check if this entity is to be included
				if ((this.entityIncluded == null) || (this.entityIncluded.decide(context, o)))
				{
					renderContents(context, o);
				}

				// remove the context item
				if (this.iteratorName != null)
				{
					context.remove(this.iteratorName);
				}

				context.remove("ambrosia_iteration_index");
			}

			return true;
		}

		// if iterating over an array, we will repeat our contents once for each one
		if ((iterator != null) && (iterator.getClass().isArray()))
		{
			Object[] c = (Object[]) iterator;
			int index = -1;

			if (c.length == 0)
			{
				if (this.iteratorEmpty != null)
				{
					response.println("<div class =\"ambrosiaInstructions\">" + this.iteratorEmpty.getMessage(context, focus) + "</div>");
				}

				return true;
			}

			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

				// place the iteration index
				context.put("ambrosia_iteration_index", Integer.toString(index));

				// check if this entity is to be included
				if ((this.entityIncluded == null) || (this.entityIncluded.decide(context, o)))
				{
					renderContents(context, o);
				}

				// remove the context item
				if (this.iteratorName != null)
				{
					context.remove(this.iteratorName);
				}

				context.remove("ambrosia_iteration_index");
			}

			return true;
		}

		// if no repeating entity, just render once
		renderContents(context, focus);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setAnchor(String selection, PropertyReference... references)
	{
		this.anchor = new UiMessage().setMessage(selection, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public UiSection setBlended(boolean setting)
	{
		this.blended = setting;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public UiSection setCollapsed(boolean setting)
	{
		this.collapsed = setting;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setEntityIncluded(Decision inclusionDecision)
	{
		this.entityIncluded = inclusionDecision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setFocus(PropertyReference entityReference)
	{
		this.focusReference = entityReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setIterator(PropertyReference reference, String name, Message empty)
	{
		this.iteratorReference = reference;
		this.iteratorName = name;
		this.iteratorEmpty = empty;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setMaxHeight(int maxHeight)
	{
		this.maxHeight = Integer.valueOf(maxHeight);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setMinHeight(int minHeight)
	{
		this.minHeight = Integer.valueOf(minHeight);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTitle(String selection, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selection, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTitleHighlighted(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.titleHighlighted = decision[0];
			}
			else
			{
				this.titleHighlighted = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTitleIncluded(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.titleIncluded = decision[0];
			}
			else
			{
				this.titleIncluded = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTitlePlain(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.titlePlain = decision[0];
			}
			else
			{
				this.titlePlain = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTreatment(String treatment)
	{
		this.treatment = treatment;
		return this;
	}

	/**
	 * Check if this title is highlighted.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if highlighted, false if not.
	 */
	protected boolean isTitleHighlighted(Context context, Object focus)
	{
		if (this.titleHighlighted == null) return false;
		return this.titleHighlighted.decide(context, focus);
	}

	/**
	 * Check if this title is included.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if included, false if not.
	 */
	protected boolean isTitleIncluded(Context context, Object focus)
	{
		if (this.titleIncluded == null) return true;
		return this.titleIncluded.decide(context, focus);
	}

	/**
	 * Check if this title is plain.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if plain, false if not.
	 */
	protected boolean isTitlePlain(Context context, Object focus)
	{
		if (this.titlePlain == null) return false;
		return this.titlePlain.decide(context, focus);
	}

	/**
	 * Render the section for a single entity
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus object.
	 */
	protected void renderContents(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// blend if in columns
		if ("columns".equals(this.treatment)) setBlended(true);

		// generate id
		String id = this.getClass().getSimpleName() + "_" + context.getUniqueId();

		String idString = "";
		String title1IdString = "";
		String title2IdString = "";
		if (this.collapsed)
		{
			int maxHeight = 0;
			if (this.maxHeight != null)
			{
				maxHeight = this.maxHeight.intValue();
			}
			int minHeight = 0;
			if (this.minHeight != null)
			{
				minHeight = this.minHeight.intValue();
			}

			// id for the div that expands (start with 0 height, but we will get rendered, hidden, so we can check the scrollHeight to know the actual size)
			String targetId = this.getClass().getSimpleName() + "_" + context.getUniqueId();
			idString = " id=\"" + targetId + "\" style=\"height:" + Integer.toString(minHeight) + "px;overflow:hidden;\"";

			// id for the two titles
			String title1Id = this.getClass().getSimpleName() + "_" + context.getUniqueId();
			title1IdString = " id=\"" + title1Id + "\"";

			String title2Id = this.getClass().getSimpleName() + "_" + context.getUniqueId();
			title2IdString = " id=\"" + title2Id + "\"";

			// the script
			context.addScript("function act_" + id + "()\n{\n\tambrosiaToggleSection(\"" + targetId + "\",\"" + title1Id + "\",\"" + title2Id + "\","
					+ maxHeight + "," + minHeight + ")\n}\n");
		}

		// if not collapsed, but there's a max height
		else if ((this.maxHeight != null) && (this.maxHeight > 0))
		{
			// id for the div that expands (start with 0 height, but we will get rendered, hidden, so we can check the scrollHeight to know the actual size)
			String targetId = this.getClass().getSimpleName() + "_" + context.getUniqueId();
			idString = " id=\"" + targetId + "\" style=\"height:0px;overflow:hidden;\"";
			context.addScript("ambrosiaExpandSectionNow(\"" + targetId + "\"," + maxHeight + ");\n");
		}

		// start the section - if blended, we don't include this so we fit into the surroundings beter
		if (!this.blended)
		{
			response.println("<div class=\"ambrosiaSection\">");
		}

		// anchor
		if (this.anchor != null)
		{
			String anchorStr = this.anchor.getMessage(context, focus);
			response.println("<a id=\"" + anchorStr + "\" name=\"" + anchorStr + "\"></a>");
		}

		// title - initially visible
		if ((this.title != null) && (isTitleIncluded(context, focus)))
		{
			if (isTitleHighlighted(context, focus))
			{
				response.print("<div" + title1IdString + " class=\"ambrosiaSectionHeaderHighlight\">");
			}
			else if (isTitlePlain(context, focus))
			{
				response.print("<div" + title1IdString + " class=\"ambrosiaSectionHeaderPlain\">");
			}
			else
			{
				response.print("<div" + title1IdString + " class=\"ambrosiaSectionHeader\">");
			}

			// wrap the title in an anchor to expand / contract
			if (this.collapsed)
			{
				response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
			}

			// icon, if collapsed
			if (this.collapsed)
			{
				response.print("<img style=\"vertical-align:text-bottom; padding-right:0.3em;border-style: none; \" src=\""
						+ context.getUrl(this.icon1) + "\" "
						// "title=\"" + description + "\" " + "alt=\"" + description
						+ "/>");
			}

			response.print(this.title.getMessage(context, focus));

			if (this.collapsed)
			{
				response.print("</a>");
			}

			response.println("</div>");
		}

		// the title while expanded, if we start collapsed
		if (this.collapsed)
		{
			if ((this.title != null) && (isTitleIncluded(context, focus)))
			{
				if (isTitleHighlighted(context, focus))
				{
					response.print("<div" + title2IdString + " style=\"display:none;\" class=\"ambrosiaSectionHeaderHighlight\">");
				}
				else if (isTitlePlain(context, focus))
				{
					response.print("<div" + title2IdString + " style=\"display:none;\" class=\"ambrosiaSectionHeaderPlain\">");
				}
				else
				{
					response.print("<div" + title2IdString + " style=\"display:none;\" class=\"ambrosiaSectionHeader\">");
				}

				response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");

				// icon
				response.print("<img style=\"vertical-align:text-bottom; padding-right:0.3em; border-style: none;\" src=\""
						+ context.getUrl(this.icon2) + "\" "
						// "title=\"" + description + "\" " + "alt=\"" + description
						+ "/>");

				response.print(this.title.getMessage(context, focus));
				response.print("</a>");
				response.println("</div>");
			}
		}

		boolean rendered = false;

		// special multi-column sections only rendering
		// Note: anything contained that is not a section is ignored ???
		if ("columns".equals(this.treatment))
		{
			// count the sections
			int numSections = countContainedSections();
			if (numSections > 0)
			{
				int pct = 100 / numSections;

				// put in a table with this many columns
				response.println("<table style=\"width:80%; border-collapse:collapse;\"><tr>");

				// render each section in a column
				for (Component c : this.contained)
				{
					if (c instanceof UiSection)
					{
						// set the section as blended to better fit in the table
						((UiSection) c).setBlended(true);

						response.println("<td style=\"vertical-align:top; margin-top:0px; margin-bottom:0px; padding-top:0px; padding-bottom:0px; width:" + pct + "%\">");
						c.render(context, focus);
						response.println("</td>");
					}
				}

				response.println("</tr></table>");
			}

			rendered = true;
		}

		// normal rendering
		// body... being a container, let the base class render the contained
		if (!rendered)
		{
			boolean closeDiv = false;
			if ("evaluation".equals(this.treatment))
			{
				response.println("<div" + idString + " class=\"ambrosiaSectionEvaluation\">");
				closeDiv = true;
			}
			else if ("indented".equals(this.treatment))
			{
				response.println("<div" + idString + " class=\"ambrosiaSectionIndented\">");
				closeDiv = true;
			}
			else if ("inlay".equals(this.treatment))
			{
				response.println("<div" + idString + " class=\"ambrosiaSectionInlay\">");
				closeDiv = true;
			}
			else if (this.collapsed)
			{
				response.println("<div" + idString + ">");
				closeDiv = true;
			}

			super.render(context, focus);

			if (closeDiv)
			{
				response.println("</div>");
			}

			rendered = true;
		}

		// end the section
		if (!this.blended) response.println("</div>");
	}

	/**
	 * @return a count of the number of section elements contained in this section - only counts those that are directly contained.
	 */
	protected int countContainedSections()
	{
		int rv = 0;
		for (Component c : this.contained)
		{
			if (c instanceof UiSection) rv++;
		}

		return rv;
	}
}
