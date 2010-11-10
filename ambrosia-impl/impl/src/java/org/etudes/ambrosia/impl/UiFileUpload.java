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
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.FileUpload;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Navigation.IconStyle;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiFileUpload...
 */
public class UiFileUpload extends UiComponent implements FileUpload
{
	/** The decision to control the onEmptyAlert. */
	protected Decision onEmptyAlertDecision = null;

	/** The message for the onEmptyAlert. */
	protected Message onEmptyAlertMsg = null;

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's uploaded file.
	 */
	protected PropertyReference propertyReference = null;

	/** The read only decision. */
	protected Decision readOnly = null;

	/** The message that will provide title to display. */
	protected Message title = null;

	/** The include decision for the title. */
	protected Decision titleIncluded = null;

	/** The tool destination for the upload button. */
	protected Destination uploadDestination = null;

	/** The message for an upload button - if null, no button. */
	protected Message uploadSubmit = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiFileUpload()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        The UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiFileUpload(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// onEmptyAlert
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "onEmptyAlert");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.onEmptyAlertMsg = new UiMessage(service, innerXml);
			}

			this.onEmptyAlertDecision = service.parseDecisions(settingsXml);
		}

		// model
		settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			this.propertyReference = service.parsePropertyReference(settingsXml);
		}

		// read only
		settingsXml = XmlHelper.getChildElementNamed(xml, "readOnly");
		if (settingsXml != null)
		{
			this.readOnly = service.parseDecisions(settingsXml);
		}

		// title
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

			// title included
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "included");
			if (innerXml != null)
			{
				this.titleIncluded = service.parseDecisions(innerXml);
			}
		}

		// upload message
		settingsXml = XmlHelper.getChildElementNamed(xml, "upload");
		if (settingsXml != null)
		{
			this.uploadSubmit = new UiMessage(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
		if (settingsXml != null)
		{
			// let Destination parse this
			this.uploadDestination = new UiDestination(service, settingsXml);
		}
	}

	/** Full URL to the icon. */
	protected String icon = null;

	/** Icon placement: left or right. */
	protected IconStyle iconStyle = IconStyle.left;

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

		// get some ids
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "file_" + idRoot;

		PrintWriter response = context.getResponseWriter();

		if (onEmptyAlert)
		{
			// this will become visible if a submit happens and the validation fails
			response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
					+ this.onEmptyAlertMsg.getMessage(context, focus) + "</div>");
		}

		// title
		if ((this.title != null) && (isTitleIncluded(context, focus)))
		{
			response.print("<label class=\"ambrosiaComponentTitle\" for=\"" + id + "\">");
			response.print(this.title.getMessage(context, focus));
			response.println("</label>");
		}

		// the file chooser
		if (!readOnly)
		{
			response.println("<input type=\"file\" name=\"" + id + "\" id=\"" + id + "\" />");

			// the decode directive
			if ((this.propertyReference != null) && (!readOnly))
			{
				response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\""
						+ "prop_" + decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");
			}

			if (this.uploadSubmit != null)
			{
				// title
				String title = "";
				if (this.uploadSubmit != null)
				{
					title = this.uploadSubmit.getMessage(context, focus);
				}

				// access key
				String accessKey = null;

				// description
				String description = "";

				// make it a two step / confirm?
				boolean confirm = false;

				// are there requirements?
				boolean requirements = false;

				boolean validate = false;

				// our action javascript
				UiNavigation.generateLinkScript(context, id, confirm, validate, true, (this.uploadDestination != null ? this.uploadDestination
						.getDestination(context, focus) : ""), (String) context.get("sakai.return.url"), requirements, false, false);

				response.print("<span class=\"ambrosiaNavNormal\">");

				// do a button
				response
						.println("<input type=\"button\" "
								+ " name=\""
								+ id
								+ "\" id=\""
								+ id
								+ "\" value=\""
								+ title
								+ "\""
								+ " onclick=\"act_"
								+ id
								+ "();return false;\" "
								+ ((accessKey == null) ? "" : "accesskey=\"" + accessKey.charAt(0) + "\" ")
								+ "title=\""
								+ description
								+ "\" "
								+ (((this.icon != null) && (this.iconStyle == IconStyle.left)) ? "style=\"padding-left:2em; background: #eee url('"
										+ context.getUrl(this.icon) + "') .2em no-repeat;\"" : "")
								+ (((this.icon != null) && (this.iconStyle == IconStyle.right)) ? "style=\"padding-left:.4em; padding-right:2em; background: #eee url('"
										+ context.getUrl(this.icon) + "') right no-repeat;\""
										: "") + "/>");

				// link code (instead of button) if we ever want it
				// // no title special case
				// if (title.length() == 0)
				// {
				// response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
				//
				// if (this.icon != null)
				// {
				// response.print("<img style=\"vertical-align:text-bottom; border-style: none;\" src=\"" + context.getUrl(this.icon) + "\" " + "title=\""
				// + description + "\" " + "alt=\"" + description + "\" />");
				// }
				//
				// response.print("</a>");
				// }
				//
				// else
				// {
				// if ((this.icon != null) && (this.iconStyle == IconStyle.left))
				// {
				// response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
				// response.print("<img style=\"vertical-align:text-bottom; padding-right:0.3em; border-style: none;\" src=\"" + context.getUrl(this.icon)
				// + "\" " + "title=\"" + description + "\" " + "alt=\"" + description + "\" />");
				// response.print("</a>");
				// }
				//
				// response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
				//
				// response.print(title);
				//
				// response.print("</a>");
				//
				// if ((this.icon != null) && (this.iconStyle == IconStyle.right))
				// {
				// response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
				// response.print("<img style=\"vertical-align:text-bottom; padding-left:0.3em; border-style: none;\" src=\"" + context.getUrl(this.icon)
				// + "\" " + "title=\"" + description + "\" " + "alt=\"" + description + "\" />");
				// response.print("</a>");
				// }
				// }
				//
				// response.println();
			}

			// for onEmptyAlert, add some client-side validation
			if ((onEmptyAlert) && (!readOnly))
			{
				context.addValidation("	if (trim(document.getElementById('" + id + "').value) == \"\")\n" + "	{\n"
						+ "		if (document.getElementById('alert_" + id + "').style.display == \"none\")\n" + "		{\n"
						+ "			document.getElementById('alert_" + id + "').style.display = \"\";\n" + "			rv=false;\n" + "		}\n" + "	}\n");
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload setDestination(Destination destination)
	{
		this.uploadDestination = destination;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload setTitleIncluded(Decision... decision)
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
	public FileUpload setUpload(String selector, PropertyReference... references)
	{
		this.uploadSubmit = new UiMessage().setMessage(selector, references);
		return this;
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
}
