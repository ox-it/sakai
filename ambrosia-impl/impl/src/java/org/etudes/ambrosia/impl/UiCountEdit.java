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
import org.etudes.ambrosia.api.CountEdit;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiCountEdit implements CountEdit.
 */
public class UiCountEdit extends UiComponent implements CountEdit
{
	/** The decision that controls if the field should get on-load focus. */
	protected Decision focusDecision = null;

	/** an icon for the display. */
	protected String icon = null; // "!/ambrosia_library/icons/stopwatch.png";

	/** The alt text for the icon. */
	protected Message iconAlt = null; // new UiMessage().setMessage("duration-alt");

	/** Icon for showing invalid. */
	protected String invalidIcon = "!/ambrosia_library/icons/warning.png";

	/** The message selector for the invalid dismiss message. */
	protected Message invalidOk = new UiMessage().setMessage("ok");

	/** A maximum acceptable value for the edit. */
	protected PropertyReference max = null;

	/** A minimum acceptable value for the edit. Default is 0. */
	protected PropertyReference min = null;

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

	/** Set if a summary of this field when rendered iterated (section or entityList) is requested. */
	protected boolean summary = false;

	protected PropertyReference summaryInitialValue = null;

	protected Message summaryTitle = null;

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/** The message for the validation alert - min only. */
	protected Message validationMsgMin = new UiMessage()
			.setMessage("count-edit-validate-min", new UiPropertyReference().setReference("ambrosia_min"));

	/** The message for the validation alert - min and max. */
	protected Message validationMsgMinMax = new UiMessage().setMessage("count-edit-validate-min-max",
			new UiPropertyReference().setReference("ambrosia_min"), new UiPropertyReference().setReference("ambrosia_max"));

	/**
	 * No-arg constructor.
	 */
	public UiCountEdit()
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
	protected UiCountEdit(UiServiceImpl service, Element xml)
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

		// min
		settingsXml = XmlHelper.getChildElementNamed(xml, "minValue");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setMin(pRef);
			}
		}

		// max
		settingsXml = XmlHelper.getChildElementNamed(xml, "maxValue");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setMax(pRef);
			}
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

		// summary
		settingsXml = XmlHelper.getChildElementNamed(xml, "summary");
		if (settingsXml != null)
		{
			this.summary = true;

			String initialValue = StringUtil.trimToNull(settingsXml.getAttribute("initialValue"));
			if (initialValue != null)
			{
				this.summaryInitialValue = service.newPropertyReference().setReference(initialValue);
			}

			title = StringUtil.trimToNull(settingsXml.getAttribute("title"));
			if (title != null)
			{
				this.summaryTitle = new UiMessage().setMessage(title);
			}

			// initial value (a model ref)
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "initialValue");
			if (innerXml != null)
			{
				Element wayInThere = XmlHelper.getChildElementNamed(settingsXml, "model");
				if (wayInThere != null)
				{
					this.summaryInitialValue = service.parsePropertyReference(wayInThere);
				}
			}

			// title
			innerXml = XmlHelper.getChildElementNamed(settingsXml, "title");
			if (innerXml != null)
			{
				this.summaryTitle = new UiMessage(service, settingsXml);
			}

			// we need an id for summary...
			if (this.id == null)
			{
				this.id = this.getClass().getSimpleName() + "_" + this.hashCode();
			}
		}

		// size
		try
		{
			int cols = Integer.parseInt(xml.getAttribute("cols"));
			setSize(cols);
		}
		catch (Throwable ignore)
		{
		}

		// icon
		// String icon = StringUtil.trimToNull(xml.getAttribute("icon"));
		// if (icon != null) this.icon = icon;

		// icon
		// settingsXml = XmlHelper.getChildElementNamed(xml, "icon");
		// if (settingsXml != null)
		// {
		// icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
		// if (icon != null) this.icon = icon;
		//
		// String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
		// if (selector != null)
		// {
		// this.iconAlt = new UiMessage().setMessage(selector);
		// }
		//
		// Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
		// if (innerXml != null)
		// {
		// this.iconAlt = new UiMessage(service, innerXml);
		// }
		// }
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSummaryRequired()
	{
		return this.summary;
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

		// if summarizing, make sure we have a single render id for all of the instances of this field, and for the summary
		String summaryId = null;
		if (this.summary)
		{
			// already registered?
			summaryId = context.getRegistration(getId());
			if (summaryId == null)
			{
				// no, do it now
				summaryId = this.getClass().getSimpleName() + "_" + context.getUniqueId();
				context.register(getId(), summaryId);
			}
		}

		// min
		String minValue = null;
		if (this.min != null)
		{
			minValue = this.min.read(context, focus);
		}
		else
		{
			minValue = "0";
		}
		try
		{
			Integer.parseInt(minValue);
		}
		catch (NumberFormatException e)
		{
			minValue = "0";
		}

		// max
		String maxValue = null;
		if (this.max != null)
		{
			maxValue = this.max.read(context, focus);
		}
		try
		{
			Integer.parseInt(maxValue);
		}
		catch (NumberFormatException e)
		{
			maxValue = null;
		}

		// set some uniqe ids for this field (among our brethren iterations)
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;
		String shadowId = "shadow_" + idRoot;

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
		String failureMsg = null;
		if (maxValue == null)
		{
			context.put("ambrosia_min", minValue);
			failureMsg = this.validationMsgMin.getMessage(context, focus);
			context.remove("ambrosia_min");
		}
		else
		{
			context.put("ambrosia_min", minValue);
			context.put("ambrosia_max", maxValue);
			failureMsg = this.validationMsgMinMax.getMessage(context, focus);
			context.remove("ambrosia_max");
			context.remove("ambrosia_min");
		}

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
				+ " onchange=\"ambrosiaCountChange(this, "
				+ valueOrNull(shadowId)
				+ ", "
				+ valueOrNull(summaryId)
				+ ", "
				+ valueOrNull(minValue)
				+ ", "
				+ valueOrNull(maxValue)
				+ ", 'invalid_"
				+ id
				+ "');\""
				+ " onkeyup=\"ambrosiaCountChange(this, "
				+ valueOrNull(shadowId)
				+ ", "
				+ valueOrNull(summaryId)
				+ ", "
				+ valueOrNull(minValue)
				+ ", "
				+ valueOrNull(maxValue)
				+ ", 'invalid_"
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

		// the shadow value field (holding the last known value)
		if (this.summary)
		{
			response.println("<input type=\"hidden\" name=\"" + shadowId + "\" id=\"" + shadowId + "\"value =\"" + value + "\" />");
		}

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
		context.addScript("ambrosiaValidateInt(document.getElementById('" + id + "'), " + valueOrNull(minValue) + ", " + valueOrNull(maxValue)
				+ ", 'invalid_" + id + "');");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void renderSummary(Context context, Object focus)
	{
		// summarizing?
		if (!this.summary) return;

		PrintWriter response = context.getResponseWriter();

		// get the summaryId
		String summaryId = context.getRegistration(getId());
		if (summaryId == null) summaryId = this.getClass().getSimpleName() + "_" + context.getUniqueId();

		// read the initial value
		String value = "";
		if (this.summaryInitialValue != null)
		{
			value = StringUtil.trimToZero(this.summaryInitialValue.read(context, focus));
		}

		// title
		if (this.summaryTitle != null)
		{
			Object extraArgs[] = new Object[1];
			extraArgs[0] = "<span id=\"" + summaryId + "\">" + value + "</span>";
			response.print(this.summaryTitle.getMessage(context, focus, extraArgs));
		}
		else
		{
			response.println("<span id=\"" + summaryId + "\">" + value + "</span>");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setMax(PropertyReference max)
	{
		this.max = max;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setMin(PropertyReference min)
	{
		this.min = min;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	// public CountEdit setIcon(String icon, String selector, PropertyReference... references)
	// {
	// this.icon = icon;
	// this.iconAlt = new UiMessage().setMessage(selector, references);
	//
	// return this;
	// }
	/**
	 * {@inheritDoc}
	 */
	public CountEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setSize(int cols)
	{
		this.numCols = cols;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setSummary()
	{
		this.summary = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setSummaryInitialValueProperty(PropertyReference propertyReference)
	{
		this.summaryInitialValue = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setSummaryTitle(String selector, PropertyReference... references)
	{
		this.summaryTitle = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * Prepare the value for a javascript parameter, either sending 'null', or the value quoted.
	 * 
	 * @param value
	 *        The value.
	 * @return The value prepared for a javascript parameter, either sending 'null', or the value quoted.
	 */
	protected String valueOrNull(String value)
	{
		return value == null ? "null" : "'" + value + "'";
	}
}
