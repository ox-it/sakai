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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Toggle;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiToggle implements Toggle
 */
public class UiToggle extends UiComponent implements Toggle
{
	/** Message to form the access key. */
	protected Message accessKey = null;

	/** The message selector for the button description. */
	protected Message description = null;

	/** The disabled decision. */
	protected Decision disabledDecision = null;

	/** Full URL to the icon. */
	protected String icon = null;

	/** Icon placement: left or right. */
	protected Navigation.IconStyle iconStyle = Navigation.IconStyle.left;

	/** The display style. */
	protected Navigation.Style style = Navigation.Style.link;

	/** The component id of the target to toggle. */
	protected String target = null;

	/** The message selector for the button title. */
	protected Message title = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiToggle()
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
	protected UiToggle(UiServiceImpl service, Element xml)
	{
		// controll stuff
		super(service, xml);

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// short form for style - attribute "style" as BUTTON or LINK
		String style = StringUtil.trimToNull(xml.getAttribute("style"));
		if (style != null)
		{
			setStyle("BUTTON".equals(style) ? Navigation.Style.button : Navigation.Style.link);
		}

		// target
		String target = StringUtil.trimToNull(xml.getAttribute("target"));
		if (target != null)
		{
			setTarget(target);
		}

		// short form for disabled
		String disabled = StringUtil.trimToNull(xml.getAttribute("disabled"));
		if ((disabled != null) && ("TRUE".equals(disabled)))
		{
			this.disabledDecision = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("TRUE"));
		}

		// short for access key
		String accessKey = StringUtil.trimToNull(xml.getAttribute("accessKey"));
		if (accessKey != null) setAccessKey(accessKey);

		// short for description
		String description = StringUtil.trimToNull(xml.getAttribute("description"));
		if (description != null) setDescription(description);

		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.title = new UiMessage(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "disabled");
		if (settingsXml != null)
		{
			this.disabledDecision = service.parseDecisions(settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "icon");
		if (settingsXml != null)
		{
			String icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			String iStyle = StringUtil.trimToNull(settingsXml.getAttribute("style"));
			Navigation.IconStyle is = "LEFT".equals(iStyle) ? Navigation.IconStyle.left : Navigation.IconStyle.right;
			setIcon(icon, is);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		// disabled?
		boolean disabled = isDisabled(context, focus);

		// generate id
		String id = this.getClass().getSimpleName() + "_" + context.getUniqueId();

		// title
		String title = "";
		if (this.title != null)
		{
			title = this.title.getMessage(context, focus);
		}

		// access key
		String accessKey = null;
		if (this.accessKey != null)
		{
			accessKey = StringUtil.trimToNull(this.accessKey.getMessage(context, focus));
		}

		// description
		String description = null;
		if (this.description != null)
		{
			description = this.description.getMessage(context, focus);
		}
		if (description == null) description = "";

		PrintWriter response = context.getResponseWriter();

		String targetId = this.target;
		String iteratation = (String) context.get("ambrosia_iteration_index");
		if (iteratation != null)
		{
			targetId += "_" + iteratation;
		}

		// the script
		context.addScript("function act_" + id + "()\n{\n\tambrosiaToggleVisibility(\"" + targetId + "\")\n}\n");

		switch (this.style)
		{
			case link:
			{
				// no title special case
				if (title.length() == 0)
				{
					if (!disabled) response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");

					if (this.icon != null)
					{
						response.print("<img style=\"vertical-align:text-bottom; border-style: none;\" src=\"" + context.getUrl(this.icon) + "\" "
								+ "title=\"" + description + "\" " + "alt=\"" + description + "\" />");
					}

					if (!disabled) response.print("</a>");
				}

				else
				{
					if ((this.icon != null) && (this.iconStyle == Navigation.IconStyle.left))
					{
						if (!disabled) response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
						response.print("<img style=\"vertical-align:text-bottom; padding-right:0.3em; border-style: none;\" src=\""
								+ context.getUrl(this.icon) + "\" " + "title=\"" + description + "\" " + "alt=\"" + description + "\" />");
						if (!disabled) response.print("</a>");
					}

					if (!disabled) response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");

					response.print(title);

					if (!disabled) response.print("</a>");

					if ((this.icon != null) && (this.iconStyle == Navigation.IconStyle.right))
					{
						if (!disabled) response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
						response.print("<img style=\"vertical-align:text-bottom; padding-left:0.3em; border-style: none;\" src=\""
								+ context.getUrl(this.icon) + "\" " + "title=\"" + description + "\" " + "alt=\"" + description + "\" />");
						if (!disabled) response.print("</a>");
					}
				}

				response.println();

				break;
			}

			case button:
			{
				response
						.println("<input type=\"button\" "
								+ " name=\""
								+ id
								+ "\" id=\""
								+ id
								+ "\" value=\""
								+ title
								+ "\""
								+ (disabled ? " disabled=\"disabled\"" : "")
								+ " onclick=\"act_"
								+ id
								+ "();return false;\" "
								+ ((accessKey == null) ? "" : "accesskey=\"" + accessKey.charAt(0) + "\" ")
								+ ((description == null) ? "" : "title=\"" + description + "\" ")
								+ (((this.icon != null) && (this.iconStyle == Navigation.IconStyle.left)) ? "style=\"padding-left:2em; background: #eee url('"
										+ context.getUrl(this.icon) + "') .2em no-repeat;\""
										: "")
								+ (((this.icon != null) && (this.iconStyle == Navigation.IconStyle.right)) ? "style=\"padding-left:.4em; padding-right:2em; background: #eee url('"
										+ context.getUrl(this.icon) + "') right no-repeat;\""
										: "") + "/>");

				break;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setAccessKey(String selector, PropertyReference... references)
	{
		this.accessKey = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setDescription(Message message)
	{
		this.description = message;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setDescription(String selector, PropertyReference... references)
	{
		this.description = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setDisabled(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.disabledDecision = decision[0];
			}
			else
			{
				this.disabledDecision = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setIcon(String icon, Navigation.IconStyle style)
	{
		this.icon = icon;
		this.iconStyle = style;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setStyle(Navigation.Style style)
	{
		this.style = style;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setTarget(String target)
	{
		this.target = target;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * Check if this is a disabled.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if this is a disabled false if not.
	 */
	protected boolean isDisabled(Context context, Object focus)
	{
		// if no target, we are disabled
		if (this.target == null) return true;

		if (this.disabledDecision == null) return false;
		return this.disabledDecision.decide(context, focus);
	}
}
