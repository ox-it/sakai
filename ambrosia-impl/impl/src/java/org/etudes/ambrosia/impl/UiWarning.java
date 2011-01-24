/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
import org.etudes.ambrosia.api.Warning;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiWarning implements Warning.
 */
public class UiWarning extends UiComponent implements Warning
{
	/** Message describing the warning condition. */
	protected Message message = new UiMessage().setMessage("warning");

	/** The message for the warning popup dismiss message. */
	protected Message okMsg = new UiMessage().setMessage("ok");

	/** Icon for showing the warning. */
	protected String warningIcon = "!/ambrosia_library/icons/warning.png";

	/**
	 * No-arg constructor.
	 */
	public UiWarning()
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
	protected UiWarning(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// short for for message - attribute "selector" for the selector, and attribute "model" for a single reference.
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

		// message
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "message");
		if (settingsXml != null)
		{
			this.message = new UiMessage(service, settingsXml);
		}

		// short for description
		String icon = StringUtil.trimToNull(xml.getAttribute("icon"));
		if (icon != null) setIcon(icon);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		PrintWriter response = context.getResponseWriter();

		// set some uniqe ids for this field (among our brethren iterations)
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;

		// the validation failure message
		String msg = null;
		if (this.message != null)
		{
			msg = this.message.getMessage(context, focus);
		}

		// the "failure" panel shown if requirements are not met
		response.println("<div class=\"ambrosiaConfirmPanel\" role=\"alertdialog\" aria-hidden=\"true\" style=\"display:none; left:0px; top:0px; width:340px; height:120px\" id=\"warning_"
				+ id + "\">");
		response.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
		response.println("<td colspan=\"2\" style=\"padding:1em; white-space:normal; line-height:1em; \" align=\"left\">" + msg + "</td>");
		response.println("</tr><tr>");
		response.println("<td style=\"padding:1em\" align=\"left\"><input type=\"button\" value=\"" + this.okMsg.getMessage(context, focus)
				+ "\" onclick=\"hideConfirm('warning_" + id + "','');return false;\" " + "/></td>");
		response.println("</tr></table></div>");

		// popup function
		StringBuffer script = new StringBuffer();
		script.append("function popupWarning_" + id + "()\n{\n  showConfirm('warning_" + id + "');\n}\n");
		context.addScript(script.toString());

		// warning indicator
		response.print("<a href=\"#\" onclick=\"popupWarning_" + id + "();return false;\" title=\"" + msg + "\">");
		response.print("<img style=\"vertical-align:text-bottom; border-style: none;\" src=\"" + context.getUrl(this.warningIcon) + "\" />");
		response.println("</a>");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Warning setIcon(String icon)
	{
		this.warningIcon = icon;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Warning setText(String selector, PropertyReference... references)
	{
		this.message = new UiMessage().setMessage(selector, references);
		return this;
	}
}
