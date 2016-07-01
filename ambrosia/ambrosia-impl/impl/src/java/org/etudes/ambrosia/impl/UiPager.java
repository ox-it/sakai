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
import java.util.Collection;
import java.util.List;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.Pager;
import org.etudes.ambrosia.api.PagingPropertyReference;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiPager implements Pager.
 */
public class UiPager extends UiComponent implements Pager
{
	/** The current report message. */
	protected Message curMessage = new UiMessage().setMessage("pager-message", new UiPropertyReference().setReference("paging.curFirstItem"),
			new UiPropertyReference().setReference("paging.curLastItem"), new UiPropertyReference().setReference("paging.maxItems"));

	/** The tool destination for clicks. */
	protected Destination destination = null;

	/** The first page icon. */
	protected String firstIcon = "!/ambrosia_library/icons/pager_first.png";

	/** The first page message. */
	protected Message firstMessage = new UiMessage().setMessage("pager-first");

	/** The last page icon. */
	protected String lastIcon = "!/ambrosia_library/icons/pager_last.png";

	/** The last page message. */
	protected Message lastMessage = new UiMessage().setMessage("pager-last");

	/** The next page icon. */
	protected String nextIcon = "!/ambrosia_library/icons/pager_next.png";

	/** The next page message. */
	protected Message nextMessage = new UiMessage().setMessage("pager-next");

	/** The no selected message. */
	protected Message noneMessage = new UiMessage().setMessage("pager-none");

	/** The model reference for the size options. */
	protected PropertyReference pageSizeModel = null;

	/** The list of page sizes we offer. */
	protected List<Integer> pageSizes = new ArrayList<Integer>();

	/** Message to show the page size options for 'all'. */
	protected Message pageSizesAllMessage = new UiMessage().setMessage("pager-all");

	/** Message to show the page size options - the {0} field is reserved for the count. */
	protected Message pageSizesMessage = new UiMessage().setMessage("pager-sizes", new UiPropertyReference().setReference("ambrosia:option"));

	/** The model reference for the current page. */
	protected PropertyReference pagingModel = null;

	/** The prev page icon. */
	protected String prevIcon = "!/ambrosia_library/icons/pager_prev.png";

	/** The prev page message. */
	protected Message prevMessage = new UiMessage().setMessage("pager-prev");

	/** If true, we need to submit the form on the press. */
	protected boolean submit = false;

	/**
	 * Public no-arg constructor.
	 */
	public UiPager()
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
	protected UiPager(UiServiceImpl service, Element xml)
	{
		// do the component stuff
		super(service, xml);

		// short form for submit
		String submit = StringUtil.trimToNull(xml.getAttribute("submit"));
		if ((submit != null) && ("TRUE".equals(submit)))
		{
			setSubmit();
		}

		// icons - short form
		String icon = StringUtil.trimToNull(xml.getAttribute("first"));
		if (icon != null) this.firstIcon = icon;
		icon = StringUtil.trimToNull(xml.getAttribute("prev"));
		if (icon != null) this.prevIcon = icon;
		icon = StringUtil.trimToNull(xml.getAttribute("next"));
		if (icon != null) this.nextIcon = icon;
		icon = StringUtil.trimToNull(xml.getAttribute("last"));
		if (icon != null) this.lastIcon = icon;

		// short form for destination
		String destination = StringUtil.trimToNull(xml.getAttribute("destination"));
		if (destination != null)
		{
			setDestination(service.newDestination().setDestination(destination));
		}

		// icons
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "first");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.firstIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.firstMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.firstMessage = new UiMessage(service, innerXml);
			}
		}
		settingsXml = XmlHelper.getChildElementNamed(xml, "prev");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.prevIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.prevMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.prevMessage = new UiMessage(service, innerXml);
			}
		}
		settingsXml = XmlHelper.getChildElementNamed(xml, "next");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.nextIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.nextMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.nextMessage = new UiMessage(service, innerXml);
			}
		}
		settingsXml = XmlHelper.getChildElementNamed(xml, "last");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.lastIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.lastMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.lastMessage = new UiMessage(service, innerXml);
			}
		}

		// destination
		settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
		if (settingsXml != null)
		{
			// let Destination parse this
			this.destination = new UiDestination(service, settingsXml);
		}

		// short for paging model reference
		String model = StringUtil.trimToNull(xml.getAttribute("paging"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setPagingProperty(pRef);
		}

		// model
		settingsXml = XmlHelper.getChildElementNamed(xml, "paging");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setPagingProperty(pRef);
			}
		}

		// text
		settingsXml = XmlHelper.getChildElementNamed(xml, "message");
		if (settingsXml != null)
		{
			this.curMessage = new UiMessage(service, settingsXml);
		}

		// size options
		settingsXml = XmlHelper.getChildElementNamed(xml, "sizeOptions");
		if (settingsXml != null)
		{
			// size options message
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.pageSizesMessage = new UiMessage(service, innerXml);
			}

			// model reference
			innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setPageSizeProperty(pRef);
			}

			// the size options
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					innerXml = (Element) node;
					if ("sizeOption".equals(innerXml.getTagName()))
					{
						try
						{
							String size = StringUtil.trimToNull(innerXml.getAttribute("size"));
							this.pageSizes.add(Integer.valueOf(size));
						}
						catch (Throwable e)
						{
						}
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager addSizeOption(Integer option)
	{
		pageSizes.add(option);
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

		response.print("<span style=\"white-space:nowrap\">");

		String pagingRef = this.pagingModel.getFullReference(context);

		Navigation first = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.firstMessage).setDestination(this.destination)
				.setIcon(this.firstIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isFirst")));
		Navigation prev = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.prevMessage).setDestination(this.destination)
				.setIcon(this.prevIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isFirst")));
		Navigation next = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.nextMessage).setDestination(this.destination)
				.setIcon(this.nextIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isLast")));
		Navigation last = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.lastMessage).setDestination(this.destination)
				.setIcon(this.lastIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isLast")));

		if (this.submit)
		{
			first.setSubmit();
			prev.setSubmit();
			next.setSubmit();
			last.setSubmit();
		}

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.FIRST);
		first.render(context, focus);

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.PREV);
		prev.render(context, focus);

		// render the message - the "viewing" one or the "none" one if there are no items
		PropertyReference maxRef = new UiPropertyReference().setReference("paging.maxItems");
		Object o = maxRef.readObject(context, focus);
		if ((o != null) && (o instanceof Integer) && (((Integer) o) == 0))
		{
			if (this.noneMessage != null)
			{
				response.println(this.noneMessage.getMessage(context, focus));
			}
		}
		else
		{
			if (this.curMessage != null)
			{
				response.println(this.curMessage.getMessage(context, focus));
			}
		}

		List<Integer> sizes = new ArrayList<Integer>(this.pageSizes);
		if (this.pageSizeModel != null)
		{
			o = this.pageSizeModel.readObject(context, focus);

			// add these to the static ones
			if (o instanceof Collection)
			{
				for (Object obj : (Collection) o)
				{
					if (obj instanceof Integer)
					{
						sizes.add((Integer) obj);
					}
				}
			}
		}

		// TODO: formatting the message and the dropdown
		// render the page size dropdown
		if (!sizes.isEmpty())
		{
			renderPageSizeDropdown(sizes, context, focus, response);
		}

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.NEXT);
		next.render(context, focus);

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.LAST);
		last.render(context, focus);

		response.print("</span>");

		context.remove(PagingPropertyReference.SELECTOR);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setDestination(Destination destination)
	{
		this.destination = destination;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setFirstIcon(String url, String selector, PropertyReference... references)
	{
		this.firstIcon = url;
		this.firstMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setLastIcon(String url, String selector, PropertyReference... references)
	{
		this.lastIcon = url;
		this.lastMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setMessage(String selector, PropertyReference... references)
	{
		this.curMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setNextIcon(String url, String selector, PropertyReference... references)
	{
		this.nextIcon = url;
		this.nextMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setPageSizeProperty(PropertyReference propertyReference)
	{
		this.pageSizeModel = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setPageSizesMessage(String selector, PropertyReference... references)
	{
		this.pageSizesMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setPagingProperty(PropertyReference propertyReference)
	{
		this.pagingModel = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setPrevIcon(String url, String selector, PropertyReference... references)
	{
		this.prevIcon = url;
		this.prevMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setSubmit()
	{
		this.submit = true;
		return this;
	}

	protected void renderPageSizeDropdown(List<Integer> sizes, Context context, Object focus, PrintWriter response)
	{
		// TODO: support validate setting, size
		boolean validate = false;
		int size = 1;

		String id = this.getClass().getSimpleName() + context.getUniqueId();

		String pagingRef = this.pagingModel.getFullReference(context);
		PropertyReference sizePropRef = new UiPropertyReference().setReference(pagingRef + ".size");
		String sizeValue = sizePropRef.read(context, focus);

		// the dropdown
		response.println("<select id=\"" + id + "\" size=\"" + size + "\" name=\"" + id + "\" onchange='act_" + id + "(this.value)'>");
		for (Integer sizeOption : sizes)
		{
			String msg = null;
			if (sizeOption.intValue() != 0)
			{
				// setup the option for the message
				context.put("ambrosia:option", sizeOption.toString());
				msg = this.pageSizesMessage.getMessage(context, focus);
				context.remove("ambrosia:option");
			}
			else
			{
				msg = this.pageSizesAllMessage.getMessage(context, focus);
			}

			// get a destination for the tool with this page size set
			context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.SIZE);
			context.put(PagingPropertyReference.SELECTOR_SIZE, sizeOption);
			String destination = this.destination.getDestination(context, focus);
			context.remove(PagingPropertyReference.SELECTOR_SIZE);
			context.remove(PagingPropertyReference.SELECTOR);

			String selected = "";
			if (sizeOption.toString().equals(sizeValue)) selected = " SELECTED";

			response.println("    <option value=\"" + destination + "\"" + selected + ">" + msg + "</option>");
		}
		response.println("</select>");

		// the script
		StringBuffer script = new StringBuffer();

		script.append("var enabled_" + id + "=true;\n");
		script.append("function cancel_" + id + "()\n");
		script.append("{\n");
		script.append("    enabled_" + id + "=false;\n");
		script.append("}\n");
		script.append("function act_" + id + "(destination)\n");
		script.append("{\n");

		// enabled check
		script.append("  if (!enabled_" + id + ")\n");
		script.append("  {\n");
		script.append("    return;\n");
		script.append("  }\n");

		// submitted already check
		script.append("  if (submitted)\n");
		script.append("  {\n");
		script.append("    return;\n");
		script.append("  }\n");

		if (this.submit)
		{
			// if we are doing validate, enable validation
			if (validate)
			{
				script.append("  enableValidate=true;\n");
			}

			// if we validate, submit the form
			script.append("  if (validate())\n");
			script.append("  {\n");

			// set that we submitted already
			script.append("    submitted=true;\n");

			// setup the destination
			script.append("    document." + context.getFormName() + ".destination_.value=destination;\n");

			// submit
			script.append("    document." + context.getFormName() + ".submit();\n");
			script.append("  }\n");
		}

		else
		{
			// set that we submitted already
			script.append("  submitted=true;\n");

			// perform the navigation
			script.append("  document.location=\"" + context.get("sakai.return.url") + "\"+destination;\n");
		}

		script.append("}\n");

		context.addScript(script.toString());
	}
}
