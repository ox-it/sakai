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
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.DurationEdit;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiDurationEdit implements DurationEdit.
 */
public class UiDurationEdit extends UiComponent implements DurationEdit
{
	/** The decision that controls if the field should get on-load focus. */
	protected Decision focusDecision = null;

	/** an icon for the display. */
	protected String icon = "!/ambrosia_library/icons/stopwatch.png";

	/** The alt text for the icon. Null until we have an editor! */
	protected Message iconAlt = null; /* new UiMessage().setMessage("duration-alt"); */

	/** Icon for showing invalid. */
	protected String invalidIcon = "!/ambrosia_library/icons/warning.png";

	/** The message selector for the invalid dismiss message. */
	protected Message invalidOk = new UiMessage().setMessage("ok");

	/** The number of columns per row for the box. */
	protected int numCols = 6;

	/** The number of rows for the text box. */
	protected int numRows = 1;

	/** The decision to control the onEmptyAlert. */
	protected Decision onEmptyAlertDecision = null;

	/** The message for the onEmptyAlert. */
	protected Message onEmptyAlertMsg = null;

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's text edit, and what value seeds the display.
	 */
	protected PropertyReference propertyReference = null;

	/** The read-only decision. */
	protected Decision readOnly = null;

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/** The message for the validation alert. */
	protected Message validationMsg = new UiMessage().setMessage("duration-edit-validate");

	/**
	 * No-arg constructor.
	 */
	public UiDurationEdit()
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
	protected UiDurationEdit(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
		}

		// title
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.titleMessage = new UiMessage(service, settingsXml);
		}

		// model
		settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}

		// onEmptyAlert
		settingsXml = XmlHelper.getChildElementNamed(xml, "onEmptyAlert");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.onEmptyAlertMsg = new UiMessage(service, innerXml);
			}

			this.onEmptyAlertDecision = service.parseDecisions(settingsXml);
		}

		// read only
		settingsXml = XmlHelper.getChildElementNamed(xml, "readOnly");
		if (settingsXml != null)
		{
			this.readOnly = service.parseDecisions(settingsXml);
		}

		// focus
		settingsXml = XmlHelper.getChildElementNamed(xml, "focus");
		if (settingsXml != null)
		{
			this.focusDecision = service.parseDecisions(settingsXml);
		}

		// icon
		String icon = StringUtil.trimToNull(xml.getAttribute("icon"));
		if (icon != null) this.icon = icon;

		// icon
		settingsXml = XmlHelper.getChildElementNamed(xml, "icon");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.icon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.iconAlt = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.iconAlt = new UiMessage(service, innerXml);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		// read only?
		boolean readOnly = false;
		if (this.readOnly != null)
		{
			readOnly = this.readOnly.decide(context, focus);
		}

		// alert if empty at submit?
		boolean onEmptyAlert = false;
		if (this.onEmptyAlertMsg != null)
		{
			onEmptyAlert = true;
			if (this.onEmptyAlertDecision != null)
			{
				onEmptyAlert = this.onEmptyAlertDecision.decide(context, focus);
			}
		}

		PrintWriter response = context.getResponseWriter();

		// set some ids
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;

		// read the current value
		String value = "";
		if (this.propertyReference != null)
		{
			value = StringUtil.trimToZero(this.propertyReference.read(context, focus));
		}

		if (onEmptyAlert)
		{
			// this will become visible if a submit happens and the validation fails
			response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
					+ this.onEmptyAlertMsg.getMessage(context, focus) + "</div>");

			// this marks the field as required
			// response.println("<span class=\"reqStarInline\">*</span>");
		}

		// the validation failure message
		String failureMsg = this.validationMsg.getMessage(context, focus);

		// the "failure" panel shown if requirements are not met
		response.println("<div class=\"ambrosiaConfirmPanel\" role=\"alertdialog\" aria-hidden=\"true\" style=\"display:none; left:0px; top:0px; width:340px; height:120px\" id=\"failure_"
				+ id + "\">");
		response.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
		response.println("<td colspan=\"2\" style=\"padding:1em; white-space:normal; line-height:1em; \" align=\"left\">" + failureMsg + "</td>");
		response.println("</tr><tr>");
		response.println("<td style=\"padding:1em\" align=\"left\"><input type=\"button\" value=\"" + this.invalidOk.getMessage(context, focus)
				+ "\" onclick=\"hideConfirm('failure_" + id + "','');return false;\" " + "/></td>");
		response.println("</tr></table></div>");

		// validation function
		StringBuffer script = new StringBuffer();
		script.append("function popupInvalid_" + id + "()\n{\n  showConfirm('failure_" + id + "');\n}\n");
		context.addScript(script.toString());

		// title
		if (this.titleMessage != null)
		{
			response.print("<label class=\"ambrosiaComponentTitle\" for=\"" + id + "\">");
			response.print(this.titleMessage.getMessage(context, focus));
			response.println("</label>");
		}

		// icon text
		String alt = null;
		if (this.iconAlt != null)
		{
			alt = this.iconAlt.getMessage(context, focus);
		}
		if (alt == null) alt = "";

		// TODO: make the icon link to a popup picker!

		response.print("<span style=\"white-space: nowrap;\"><input type=\"text\" id=\""
				+ id
				+ "\" name=\""
				+ id
				+ "\" size=\""
				+ Integer.toString(numCols)
				+ "\" value=\""
				+ value
				+ "\""
				+ (readOnly ? " disabled=\"disabled\"" : "")
				+ " onchange=\"ambrosiaDurationChange(this, 'invalid_"
				+ id
				+ "');\""
				+ " />"
				+ ((this.icon != null) ? " <img style=\"border-style: none;\" src=\"" + context.getUrl(this.icon) + "\" alt=\"" + alt + "\" title=\""
						+ alt + "\" />" : ""));

		// validate failure alert (will display:inline when made visible)
		response.print("<div style=\"display:none\" id=\"invalid_" + id + "\">");
		response.print("<a href=\"#\" onclick=\"popupInvalid_" + id + "();return false;\" title=\"" + failureMsg + "\">");
		response.print("<img style=\"vertical-align:text-bottom;border-style: none;\" src=\"" + context.getUrl(this.invalidIcon) + "\" />");
		response.print("</a></div>");

		response.println("</span>");

		context.editComponentRendered(id);

		// the decode directive
		if ((this.propertyReference != null) && (!readOnly))
		{
			response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_"
					+ decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />" + "<input type=\"hidden\" name=\""
					+ "type_" + decodeId + "\" value=\"" + this.propertyReference.getType() + "\" />");
		}

		// for onEmptyAlert, add some client-side validation
		if ((onEmptyAlert) && (!readOnly))
		{
			context.addValidation("	if (trim(document.getElementById('" + id + "').value) == \"\")\n" + "	{\n"
					+ "		if (document.getElementById('alert_" + id + "').style.display == \"none\")\n" + "		{\n"
					+ "			document.getElementById('alert_" + id + "').style.display = \"\";\n" + "			rv=false;\n" + "		}\n" + "	}\n");
		}

		// for on-load focus
		if ((!readOnly) && (this.focusDecision != null) && (this.focusDecision.decide(context, focus)))
		{
			// add the field name / id to the focus path
			context.addFocusId(id);
		}

		// pre-validate
		context.addScript("ambrosiaValidateDuration(document.getElementById('" + id + "'), 'invalid_" + id + "');");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationEdit setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationEdit setIcon(String icon, String selector, PropertyReference... references)
	{
		this.icon = icon;
		this.iconAlt = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationEdit setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationEdit setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationEdit setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
