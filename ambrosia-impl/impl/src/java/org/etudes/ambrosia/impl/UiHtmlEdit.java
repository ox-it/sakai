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
import org.etudes.ambrosia.api.HtmlEdit;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.util.HtmlHelper;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiHtmlEdit implements HtmlEdit.
 */
public class UiHtmlEdit extends UiComponent implements HtmlEdit
{
	/** The alt text for the edit icon. */
	protected Message editAlt = new UiMessage().setMessage("edit-alt");

	/** Icon for enabling the editor. */
	// TODO:
	protected String editIcon = "!/ambrosia_library/icons/edit.png";

	protected String editIcon2 = "!/ambrosia_library/icons/edit2.png";

	/** The decision that controls if the field should get on-load focus. */
	protected Decision focusDecision = null;

	/** The decision to control the onEmptyAlert. */
	protected Decision onEmptyAlertDecision = null;

	/** The message for the onEmptyAlert. */
	protected Message onEmptyAlertMsg = null;

	/** If set, start disabled with the text rendered. */
	protected boolean optional = false;

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's text edit, and what value seeds the display.
	 */
	protected PropertyReference propertyReference = null;

	/** The read-only decision. */
	protected Decision readOnly = null;

	/** Size. */
	protected Sizes size = Sizes.full;

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/**
	 * No-arg constructor.
	 */
	public UiHtmlEdit()
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
	protected UiHtmlEdit(UiServiceImpl service, Element xml)
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

		// optional
		String optional = StringUtil.trimToNull(xml.getAttribute("optional"));
		if ((optional != null) && (optional.equals("TRUE")))
		{
			setOptional();
		}

		// size
		String size = StringUtil.trimToNull(xml.getAttribute("size"));
		if (size != null)
		{
			if (size.equals("SMALL"))
			{
				setSize(Sizes.small);
			}
			else if (size.equals("TALL"))
			{
				setSize(Sizes.tall);
			}
			else if (size.equals("FULL"))
			{
				setSize(Sizes.full);
			}
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
		if (id == null) id = this.getClass().getSimpleName() + "_" + idRoot + "_" + System.currentTimeMillis();
		String decodeId = "decode_" + idRoot;

		// read the current value object as a string
		String value = "";
		if (this.propertyReference != null)
		{
			Object valueObj = this.propertyReference.readObject(context, focus);
			if (valueObj != null)
			{
				value = StringUtil.trimToZero(valueObj.toString());

				// clean up bad and good comments
				value = HtmlHelper.stripEncodedFontDefinitionComments(value);
				value = HtmlHelper.stripDamagedComments(value);
				value = HtmlHelper.stripComments(value);
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

		// our status object for enabling the editor
		if (!readOnly && this.optional)
		{
			String docsPath = context.getDocsPath();
			context.addScript("var htmlComponent_" + id + "=new Object();\n");
			context.addScript("htmlComponent_" + id + ".enabled=false;\n");
			if (this.optional)
			{
				context.addScript("htmlComponent_" + id + ".renderedId=\"rendered_" + id + "\";\n");
				context.addScript("htmlComponent_" + id + ".toggleId=\"toggle_" + id + "\";\n");
			}
			else
			{
				context.addScript("htmlComponent_" + id + ".renderedId=null;\n");
				context.addScript("htmlComponent_" + id + ".toggleId=null;\n");
			}
			context.addScript("htmlComponent_" + id + ".textAreaId=\"" + id + "\";\n");
			context.addScript("htmlComponent_" + id + ".mode=\"" + this.size.toString() + "\";\n");
		}

		// the title (if defined), and the edit icon
		if ((this.titleMessage != null) || (!readOnly && this.optional))
		{
			response.println("<div class=\"ambrosiaComponentTitle\">");
			if (this.titleMessage != null)
			{
				response.println(this.titleMessage.getMessage(context, focus));
			}
			if (!readOnly && this.optional)
			{
				response.print("<a style=\"text-decoration:none;\" id=\"toggle_" + id
						+ "\" href=\"#\" onclick=\"ambrosiaEnableHtmlEdit(htmlComponent_" + id + ");return false;\" title=\""
						+ this.editAlt.getMessage(context, focus) + "\">");
				response.print("<img style=\"vertical-align:text-bottom; border-style: none;\" src=\"" + context.getUrl(this.editIcon) + "\" />");
				response.println("</a>");
			}
			response.println("</div>");
		}

		renderActions(context, focus);

		// container div (for optional)
		if (!readOnly /* && this.optional */)
		{
			response.println("<div class=\"ambrosiaHtmlEditContainer ambrosiaHtmlEditSize_" + this.size.toString() + "\">");
		}

		// the edit textarea (if not optional)
		if (!(!readOnly && this.optional))
		{
			response.println("<textarea "
					+ (this.optional ? "style=\"display:none; position:absolute; top:0px; left:0px;\"" : (" class=\"ambrosiaHtmlEdit_"
							+ this.size.toString() + "\"")) + " id=\"" + id + "\" name=\"" + id + "\" " + (readOnly ? " disabled=\"disabled\"" : "")
					+ ">");
			response.print(Validator.escapeHtmlTextarea(value));
			response.println("</textarea>");
		}

		// for optional, a hidden field to hold the value
		else
		{
			response.println("<input type=\"hidden\" id=\"" + id + "\" name=\"" + id + "\"/>");
			// pre-populate
			context.addScript("document.getElementById(\"" + id + "\").value = document.getElementById(\"rendered_" + id + "\").innerHTML;\n");
		}

		// the rendered content - initially visible
		if (!readOnly && this.optional)
		{
			response.println("<div id=\"rendered_" + id + "\" class=\"ambrosiaHtmlEditRendered ambrosiaHtmlEditSize_" + this.size.toString() + "\">");
			if (value != null) response.println(value);
			response.println("</div>");
		}

		if (!readOnly) response.println("</div>");

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
	public HtmlEdit setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setOptional()
	{
		this.optional = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setSize(Sizes size)
	{
		this.size = size;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
