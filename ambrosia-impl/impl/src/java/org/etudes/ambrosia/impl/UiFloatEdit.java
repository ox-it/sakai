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
import org.etudes.ambrosia.api.FloatEdit;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiCountEdit implements CountEdit.
 */
public class UiFloatEdit extends UiComponent implements FloatEdit
{
	/** optional model reference for value to use in the UI if the field becomes blank. */
	protected PropertyReference defaultReference = null;

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
	protected int numCols = 16;

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

	/** Component ID that we sumarize any change of value to. */
	protected String sumToId = null;

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/** The message for the validation alert. */
	protected Message validationMsg = new UiMessage().setMessage("float-edit-validate");

	/** The message for the validation alert - max only. */
	protected Message validationMsgMax = new UiMessage()
			.setMessage("float-edit-validate-max", new UiPropertyReference().setReference("ambrosia_max"));

	/** The message for the validation alert - min only. */
	protected Message validationMsgMin = new UiMessage()
			.setMessage("float-edit-validate-min", new UiPropertyReference().setReference("ambrosia_min"));

	/** The message for the validation alert - min and max. */
	protected Message validationMsgMinMax = new UiMessage().setMessage("float-edit-validate-min-max",
			new UiPropertyReference().setReference("ambrosia_min"), new UiPropertyReference().setReference("ambrosia_max"));

	/**
	 * No-arg constructor.
	 */
	public UiFloatEdit()
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
	protected UiFloatEdit(UiServiceImpl service, Element xml)
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

		// short for defaultModel
		String defaultModel = StringUtil.trimToNull(xml.getAttribute("defaultValue"));
		if (defaultModel != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(defaultModel);
			setDefaultProperty(pRef);
		}

		// short for sumTo
		String sumTo = StringUtil.trimToNull(xml.getAttribute("sumTo"));
		if (sumTo != null)
		{
			setSumToId(sumTo);
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

		// default
		settingsXml = XmlHelper.getChildElementNamed(xml, "defaultValue");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setDefaultProperty(pRef);
			}
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

		// size
		try
		{
			int cols = Integer.parseInt(xml.getAttribute("cols"));
			setSize(cols);
		}
		catch (Throwable ignore)
		{
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
		String id = (this.id != null) ? this.id : this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;
		String shadowId = "shadow_" + idRoot;

		// read the current value
		String value = "";
		if (this.propertyReference != null)
		{
			value = StringUtil.trimToZero(this.propertyReference.read(context, focus));
		}

		// read the default value
		String defaultValue = null;
		if (this.defaultReference != null)
		{
			defaultValue = this.defaultReference.read(context, focus);
		}
		if (defaultValue == null) defaultValue = "";

		if (onEmptyAlert)
		{
			// this will become visible if a submit happens and the validation fails
			response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
					+ this.onEmptyAlertMsg.getMessage(context, focus) + "</div>");

			// this marks the field as required
			// response.println("<span class=\"reqStarInline\">*</span>");
		}

		// min
		String minValue = null;
		if (this.min != null)
		{
			try
			{
				minValue = this.min.read(context, focus);
				Float.parseFloat(minValue);
			}
			catch (NumberFormatException e)
			{
				minValue = null;
			}
		}

		// max
		String maxValue = null;
		if (this.max != null)
		{
			try
			{
				maxValue = this.max.read(context, focus);
				Float.parseFloat(maxValue);
			}
			catch (NumberFormatException e)
			{
				maxValue = null;
			}
		}

		// the validation failure message
		String failureMsg = null;
		if ((minValue == null) && (maxValue == null))
		{
			failureMsg = this.validationMsg.getMessage(context, focus);
		}

		else if (maxValue == null)
		{
			context.put("ambrosia_min", minValue);
			failureMsg = this.validationMsgMin.getMessage(context, focus);
			context.remove("ambrosia_min");
		}

		else if (minValue == null)
		{
			context.put("ambrosia_max", maxValue);
			failureMsg = this.validationMsgMax.getMessage(context, focus);
			context.remove("ambrosia_max");
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
				+ " onchange=\"ambrosiaFloatChange(this, "
				+ valueOrNull(shadowId)
				+ ", "
				+ valueOrNull(this.sumToId)
				+ ", "
				+ valueOrNull(defaultValue)
				+ ", "
				+ valueOrNull(minValue)
				+ ", "
				+ valueOrNull(maxValue)
				+ ", 'invalid_"
				+ id
				+ "');\""
				+ " onkeyup=\"ambrosiaFloatChange(this, "
				+ valueOrNull(shadowId)
				+ ", "
				+ valueOrNull(this.sumToId)
				+ ", "
				+ valueOrNull(defaultValue)
				+ ", "
				+ valueOrNull(minValue)
				+ ", "
				+ valueOrNull(maxValue)
				+ ", 'invalid_"
				+ id
				+ "');\""
				+ " />"
				+ ((this.icon != null) ? " <img src=\"" + context.getUrl(this.icon) + "\" style=\"border-style: none;\" alt=\"" + alt + "\" title=\""
						+ alt + "\" />" : ""));

		// validate failure alert (will display:inline when made visible)
		response.print("<div style=\"display:none\" id=\"invalid_" + id + "\">");
		response.print("<a href=\"#\" onclick=\"popupInvalid_" + id + "();return false;\" title=\"" + failureMsg + "\">");
		response.print("<img style=\"vertical-align:text-bottom; border-style: none;\" src=\"" + context.getUrl(this.invalidIcon) + "\" />");
		response.print("</a></div>");

		response.println("</span>");

		context.editComponentRendered(id);

		// the shadow value field (holding the last known value)
		if (this.sumToId != null)
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
			context.addValidation("  if (trim(document.getElementById('" + id + "').value) == \"\")\n" + "  {\n"
					+ "    if (document.getElementById('alert_" + id + "').style.display == \"none\")\n" + "    {\n"
					+ "      document.getElementById('alert_" + id + "').style.display = \"\";\n" + "      rv=false;\n" + "    }\n" + "  }\n");
		}

		// for on-load focus
		if ((!readOnly) && (this.focusDecision != null) && (this.focusDecision.decide(context, focus)))
		{
			// add the field name / id to the focus path
			context.addFocusId(id);
		}

		// pre-validate
		context.addScript("ambrosiaValidateFloat(document.getElementById('" + id + "'), " + valueOrNull(minValue) + ", " + valueOrNull(maxValue)
				+ ", 'invalid_" + id + "');\n");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setDefaultProperty(PropertyReference propertyReference)
	{
		this.defaultReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setMax(PropertyReference max)
	{
		this.max = max;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setMin(PropertyReference min)
	{
		this.min = min;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	// public FloatEdit setIcon(String icon, String selector, PropertyReference... references)
	// {
	// this.icon = icon;
	// this.iconAlt = new UiMessage().setMessage(selector, references);
	//
	// return this;
	// }
	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setSize(int cols)
	{
		this.numCols = cols;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setSumToId(String id)
	{
		this.sumToId = id;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit setTitle(String selector, PropertyReference... references)
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
