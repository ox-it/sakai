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

import java.util.ArrayList;
import java.util.List;

import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.EntityListColumn;
import org.etudes.ambrosia.api.Footnote;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.SummarizingComponent;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * AutoColumn provides automatic numbering for columns in an entity list.
 */
public class UiEntityListColumn implements EntityListColumn
{
	/** The decision to display the column for this entry as an alert. */
	protected Decision alert = null;

	/** The bottomed setting. */
	protected boolean bottomed = false;

	/** The centered setting. */
	protected boolean centered = false;

	/** Components contained in this container. */
	protected List<Component> contained = new ArrayList<Component>();

	/** The entity actions defined related to this column. */
	protected List<Component> entityActions = new ArrayList<Component>();

	/** The inclusion decision for each entity. */
	protected Decision entityIncluded = null;

	/** The navigations to use for the main text of the column. */
	protected List<Navigation> entityNavigations = new ArrayList<Navigation>();

	/** Footnotes for this column. */
	protected List<Footnote> footnotes = new ArrayList<Footnote>();

	/** The include decision. */
	protected Decision included = null;

	/** The navigations defined for display in this column. */
	protected List<Component> navigations = new ArrayList<Component>();

	/** The message to show if an entity is not included in this column. */
	protected Message notIncludedMsg = null;

	/** The no-wrapping indicator for the column. */
	protected boolean noWrap = false;

	/** The right-justified setting. */
	protected boolean right = false;

	/** The destination that leads to this column asc sort. */
	protected Destination sortAsc = null;

	/** The Message describing the sort asc. icon. */
	protected Message sortAscIconMsg = null;

	/** The icon path for the sort asc icon. */
	protected String sortAscIconPath = null;

	/** The destination that leads to this column desc sort. */
	protected Destination sortDesc = null;

	/** The Message describing the sort desc. icon. */
	protected Message sortDescIconMsg = null;

	/** The icon path for the sort desc. icon. */
	protected String sortDescIconPath = null;

	/** The decision that tells if this column is currently the sort column. */
	protected Decision sorting = null;

	/** The decision that tells if this column is doing asc, not desc sort (if false, it is doing desc, not asc) */
	protected Decision sortingAsc = null;

	/** If set, make the sort links submit. */
	protected boolean sortSubmit = false;

	/** The message for the column title. */
	protected Message title = null;

	/** The topped setting. */
	protected boolean topped = false;

	/** The column width (in pixels). */
	protected Integer width = null;

	/** The column width (in em). */
	protected Integer widthEm = null;

	/** The column width (in percent). */
	protected Integer widthPercent = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiEntityListColumn()
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
	protected UiEntityListColumn(UiServiceImpl service, Element xml)
	{
		// alert
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "asAlert");
		if (settingsXml != null)
		{
			Decision decision = service.parseDecisions(settingsXml);
			setAlert(decision);
		}

		// short form for centered
		String centered = StringUtil.trimToNull(xml.getAttribute("centered"));
		if ((centered != null) && ("TRUE".equals(centered))) setCentered();

		// short form for right-justified
		String right = StringUtil.trimToNull(xml.getAttribute("right"));
		if ((right != null) && ("TRUE".equals(right))) setRight();

		// short form for bottomed
		String bottomed = StringUtil.trimToNull(xml.getAttribute("bottomed"));
		if ((bottomed != null) && ("TRUE".equals(bottomed))) setBottomed();

		// short form for toped
		String topped = StringUtil.trimToNull(xml.getAttribute("topped"));
		if ((topped != null) && ("TRUE".equals(topped))) setTopped();

		// entity included
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityIncluded");
		if (settingsXml != null)
		{
			Message notIncluded = null;

			// short for not included message
			String selector = StringUtil.trimToNull(settingsXml.getAttribute("selector"));
			if (selector != null)
			{
				notIncluded = new UiMessage().setMessage(selector);
			}

			Decision decision = service.parseDecisions(settingsXml);

			// not included message
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				notIncluded = new UiMessage(service, innerXml);
			}

			setEntityIncluded(decision, notIncluded);
		}

		// included
		settingsXml = XmlHelper.getChildElementNamed(xml, "included");
		if (settingsXml != null)
		{
			this.included = service.parseDecisions(settingsXml);
		}

		// no wrap
		String noWrap = StringUtil.trimToNull(xml.getAttribute("wrap"));
		if ((noWrap != null) && ("FALSE".equals(noWrap))) setNoWrap();

		// sort
		settingsXml = XmlHelper.getChildElementNamed(xml, "sort");
		if (settingsXml != null)
		{
			String submit = StringUtil.trimToNull(settingsXml.getAttribute("submit"));
			if ((submit != null) && ("TRUE".equals(submit))) setSortSubmit();

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "active");
			if (innerXml != null)
			{
				this.sorting = service.parseDecisions(innerXml);
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "direction");
			if (innerXml != null)
			{
				this.sortingAsc = service.parseDecisions(innerXml);
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "asc");
			if (innerXml != null)
			{
				this.sortAscIconPath = StringUtil.trimToNull(innerXml.getAttribute("icon"));

				Element wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "message");
				if (wayInnerXml != null)
				{
					this.sortAscIconMsg = new UiMessage(service, wayInnerXml);
				}

				wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "destination");
				if (wayInnerXml != null)
				{
					this.sortAsc = new UiDestination(service, wayInnerXml);
				}
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "desc");
			if (innerXml != null)
			{
				this.sortDescIconPath = StringUtil.trimToNull(innerXml.getAttribute("icon"));

				Element wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "message");
				if (wayInnerXml != null)
				{
					this.sortDescIconMsg = new UiMessage(service, wayInnerXml);
				}

				wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "destination");
				if (wayInnerXml != null)
				{
					this.sortDesc = new UiDestination(service, wayInnerXml);
				}
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

		// width - pixels
		String width = StringUtil.trimToNull(xml.getAttribute("pixels"));
		if (width != null)
		{
			try
			{
				setWidth(Integer.parseInt(width));
			}
			catch (NumberFormatException e)
			{
			}
		}

		// width - percent
		String percent = StringUtil.trimToNull(xml.getAttribute("percent"));
		if (percent != null)
		{
			try
			{
				setWidthPercent(Integer.parseInt(percent));
			}
			catch (NumberFormatException e)
			{
			}
		}

		// width - em
		width = StringUtil.trimToNull(xml.getAttribute("em"));
		if (width != null)
		{
			try
			{
				setWidthEm(Integer.parseInt(width));
			}
			catch (NumberFormatException e)
			{
			}
		}

		// components
		settingsXml = XmlHelper.getChildElementNamed(xml, "container");
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
						this.contained.add(c);
					}
				}
			}
		}

		// entity navigations
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityNavigations");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element innerXml = (Element) node;
					if ("navigation".equals(innerXml.getTagName()))
					{
						Navigation n = new UiNavigation(service, innerXml);
						this.entityNavigations.add(n);
					}
				}
			}
		}

		// footnotes
		settingsXml = XmlHelper.getChildElementNamed(xml, "footnotes");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element innerXml = (Element) node;
					if ("footnote".equals(innerXml.getTagName()))
					{
						Footnote f = new UiFootnote(service, innerXml);
						this.footnotes.add(f);
					}
				}
			}
		}

		// navigations
		settingsXml = XmlHelper.getChildElementNamed(xml, "navigations");
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
						this.navigations.add(c);
					}
				}
			}
		}

		// entityActions
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityActions");
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
						this.entityActions.add(c);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn add(Component component)
	{
		this.contained.add(component);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn addEntityAction(Component action)
	{
		this.entityActions.add(action);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn addEntityNavigation(Navigation navigation)
	{
		this.entityNavigations.add(navigation);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn addFootnote(Footnote footnote)
	{
		this.footnotes.add(footnote);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn addNavigation(Component navigation)
	{
		this.navigations.add(navigation);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean alert(Context context, Object focus)
	{
		if ((this.alert != null) && (this.alert.decide(context, focus))) return true;

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBottomed()
	{
		return this.bottomed;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getCentered()
	{
		return this.centered;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayText(Context context, Object entity, int row, String id, int size)
	{
		// set the context to capture instead of adding to the output
		context.setCollecting();

		// render the contained
		for (Component c : this.contained)
		{
			c.render(context, entity);
		}

		// get the captured text, resetting to output mode
		String rv = context.getCollected();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Component> getEntityActions()
	{
		return entityActions;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityNavigationDestination(Context context, Object focus)
	{
		if (this.entityNavigations == null) return null;

		for (Navigation n : this.entityNavigations)
		{
			String destination = n.getDestination(context, focus);
			if (destination != null) return destination;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getEntityNavigationSubmit()
	{
		if (this.entityNavigations == null) return false;
		return this.entityNavigations.get(0).getSubmit();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Footnote> getFootnotes()
	{
		return this.footnotes;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getIsEntityIncluded(Context context, Object focus)
	{
		if ((this.entityIncluded != null) && (!this.entityIncluded.decide(context, focus))) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getIsNoWrap()
	{
		return this.noWrap;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Component> getNavigations()
	{
		return navigations;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getNotIncludedMsg()
	{
		return this.notIncludedMsg;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOneTimeText(Context context, Object focus, String id, int numRows)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPrefixText(Context context, Object focus, String id)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getRight()
	{
		return this.right;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSortAscIcon()
	{
		return this.sortAscIconPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getSortAscMsg()
	{
		return this.sortAscIconMsg;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSortDescIcon()
	{
		return this.sortDescIconPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getSortDescMsg()
	{
		return this.sortDescIconMsg;
	}

	/**
	 * {@inheritDoc}
	 */
	public Destination getSortDestinationAsc()
	{
		return this.sortAsc;
	}

	/**
	 * {@inheritDoc}
	 */
	public Destination getSortDestinationDesc()
	{
		return this.sortDesc;
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision getSortingAscDecision()
	{
		return this.sortingAsc;
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision getSortingDecision()
	{
		return this.sorting;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getSortSubmit()
	{
		return this.sortSubmit;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTitle(Context context, Object focus, String effectiveId)
	{
		if (this.title != null)
		{
			return this.title.getMessage(context, focus);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getTopped()
	{
		return this.topped;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getWidth()
	{
		return this.width;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getWidthEm()
	{
		return this.widthEm;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getWidthPercent()
	{
		return this.widthPercent;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasTitle(Context context, Object focus)
	{
		return this.title != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean included(Context context)
	{
		if ((this.included != null) && (!this.included.decide(context, null))) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSummaryRequired()
	{
		for (Component c : this.contained)
		{
			if (c instanceof SummarizingComponent)
			{
				if (((SummarizingComponent) c).isSummaryRequired())
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void renderSummary(Context context, Object entity)
	{
		for (Component c : this.contained)
		{
			if (c instanceof SummarizingComponent)
			{
				if (((SummarizingComponent) c).isSummaryRequired())
				{
					((SummarizingComponent) c).renderSummary(context, entity);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setAlert(Decision alertDecision)
	{
		this.alert = alertDecision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setBottomed()
	{
		this.bottomed = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setCentered()
	{
		this.centered = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setEntityIncluded(Decision inclusionDecision, Message notIncludedMsg)
	{
		this.entityIncluded = inclusionDecision;
		this.notIncludedMsg = notIncludedMsg;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setIncluded(Decision decision)
	{
		this.included = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setNoWrap()
	{
		this.noWrap = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setRight()
	{
		this.right = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setSortDestination(Destination asc, Destination desc)
	{
		this.sortAsc = asc;
		this.sortDesc = desc;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setSortIcons(String ascUrl, Message ascDescription, String descUrl, Message descDescription)
	{
		this.sortAscIconPath = ascUrl;
		this.sortAscIconMsg = ascDescription;
		this.sortDescIconPath = descUrl;
		this.sortDescIconMsg = descDescription;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setSorting(Decision sorting, Decision ascNotDesc)
	{
		this.sorting = sorting;
		this.sortingAsc = ascNotDesc;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setSortSubmit()
	{
		this.sortSubmit = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setTopped()
	{
		this.topped = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setWidth(int width)
	{
		this.width = new Integer(width);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setWidthEm(int width)
	{
		this.widthEm = new Integer(width);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setWidthPercent(int percent)
	{
		this.widthPercent = new Integer(percent);
		return this;
	}
}
