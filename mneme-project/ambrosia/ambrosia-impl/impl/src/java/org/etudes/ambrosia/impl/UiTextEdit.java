/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.TextEdit;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiTextEdit presents a text input for the user to edit.
 */
public class UiTextEdit extends UiComponent implements TextEdit
{
	/** The decision that controls if the field should get on-load focus. */
	protected Decision focusDecision = null;

	/** The number of columns per row for the box. */
	protected int numCols = 50;

	/** The number of rows for the edit - if > 1, a textarea is requested. */
	protected int numRows = 1;

	/** The decision to control the onEmptyAlert. */
	protected Decision onEmptyAlertDecision = null;

	/** The message for the onEmptyAlert. */
	protected Message onEmptyAlertMsg = null;

	/** Property reference to get a set of pre-defined answers for the text. */
	protected PropertyReference optionsReference = null;

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's text edit, and what value
	 * seeds the display.
	 */
	protected PropertyReference propertyReference = null;

	/** The read-only decision. */
	protected Decision readOnly = null;

	/** The destination to submit if we are submitting on change. */
	protected Destination submitDestination = null;

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/**
	 * No-arg constructor.
	 */
	public UiTextEdit()
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
	protected UiTextEdit(UiServiceImpl service, Element xml)
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

		// short for options
		String options = StringUtil.trimToNull(xml.getAttribute("options"));
		if (options != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(options);
			if (pRef != null) setOptions(pRef);
		}

		// short form for destination - attribute "destination" as the destination
		String destination = StringUtil.trimToNull(xml.getAttribute("destination"));
		if (destination != null)
		{
			setDestination(service.newDestination().setDestination(destination));
		}

		// size
		int cols = this.numCols;
		int rows = this.numRows;
		try
		{
			rows = Integer.parseInt(xml.getAttribute("rows"));
		}
		catch (Throwable ignore)
		{
		}
		try
		{
			cols = Integer.parseInt(xml.getAttribute("cols"));
		}
		catch (Throwable ignore)
		{
		}
		setSize(rows, cols);

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

		// options
		settingsXml = XmlHelper.getChildElementNamed(xml, "options");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setOptions(pRef);
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

		// read only shortcut
		String readOnly = StringUtil.trimToNull(xml.getAttribute("readOnly"));
		if ((readOnly != null) && ("TRUE".equals(readOnly)))
		{
			this.readOnly = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
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

		// submitDestination
		settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
		if (settingsXml != null)
		{
			// let Destination parse this
			this.submitDestination = new UiDestination(service, settingsXml);
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
		String id = getId();
		if (id == null) id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;

		// read the current value object, unformatted
		String value = "";
		if (this.propertyReference != null)
		{
			Object valueObj = this.propertyReference.readObject(context, focus);
			if (valueObj != null)
			{
				value = StringUtil.trimToZero(valueObj.toString());
			}
		}

		if (onEmptyAlert)
		{
			// this will become visible if a submit happens and the validation fails
			response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
					+ this.onEmptyAlertMsg.getMessage(context, focus) + "</div>");

			// this marks the field as required
			// response.println("<span class=\"reqStarInline\">*</span>");
		}

		// title
		if (this.titleMessage != null)
		{
			response.print("<label class=\"ambrosiaComponentTitle\" for=\"" + id + "\">");
			response.print(this.titleMessage.getMessage(context, focus));
			response.println("</label>");
		}

		// what to do on change
		String onchange = "";
		if (this.submitDestination != null)
		{
			String destination = this.submitDestination.getDestination(context, focus);
			onchange = "onchange=\"ambrosiaSubmit('" + destination + "')\" ";
		}

		// the edit control
		if (this.numRows == 1)
		{
			// prepare the value
			value = FormattedText.decodeNumericCharacterReferences(value);
			value = Validator.escapeHtml(value);

			response.println("<input " + onchange + "type=\"text\" id=\"" + id + "\" name=\"" + id + "\" size=\"" + Integer.toString(this.numCols)
					+ "\" value=\"" + value + "\"" + (readOnly ? " disabled=\"disabled\"" : "") + " />");
		}
		else
		{
			// prepare the value
			value = FormattedText.decodeNumericCharacterReferences(value);
			value = Validator.escapeHtmlTextarea(value);
			
			response.println("<textarea " + onchange + "id=\"" + id + "\" name=\"" + id + "\" cols=\"" + Integer.toString(this.numCols)
					+ "\" rows = \"" + Integer.toString(this.numRows) + "\" wrap=\"SOFT\"" + (readOnly ? " disabled=\"disabled\"" : "") + ">\n"
					+ value + "\n" + "</textarea>");
		}

		context.editComponentRendered(id);

		renderOptions(context, focus, id);

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

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setDestination(Destination destination)
	{
		this.submitDestination = destination;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setOptions(PropertyReference propertyReference)
	{
		this.optionsReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setSize(int rows, int cols)
	{
		this.numRows = rows;
		this.numCols = cols;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * Render the options html, if defined.
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus.
	 * @param textId
	 *        the render id of the main text field.
	 */
	protected void renderOptions(Context context, Object focus, String textId)
	{
		if (this.optionsReference == null) return;
		Object options = this.optionsReference.readObject(context, focus);
		if (options == null) return;

		PrintWriter response = context.getResponseWriter();

		response.println("<select size=\"1\" onchange=\"ambrosiaTextOptions(this,'" + textId + "')\">");
		response.println("<option value=\"\"></option>");
		if (options instanceof Collection)
		{
			for (Object option : (Collection<Object>) options)
			{
				String str = option.toString();
				response.println("<option value=\"" + str + "\">" + str + "</option>");
			}
		}

		response.println("</select>");
	}
}
