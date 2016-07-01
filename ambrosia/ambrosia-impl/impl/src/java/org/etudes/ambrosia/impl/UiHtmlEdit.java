/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2012, 2013, 2014 Etudes, Inc.
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
import org.etudes.basicltiContact.SakaiBLTIUtil;
import org.etudes.util.HtmlHelper;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
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

		// store htmlEdit title
		String editRendererTitle = null;
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

				// clean
				value = HtmlHelper.clean(value, true);
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

		// the title (if defined), and the edit icon
		if ((this.titleMessage != null) || (!readOnly && this.optional))
		{
			response.println("<div class=\"ambrosiaComponentTitle\">");
			if (this.titleMessage != null)
			{
				// later compare html edit box title to enable VT for Question presentation text only
				editRendererTitle = this.titleMessage.getMessage(context, focus);
				response.println(editRendererTitle);				
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
			// make sure the context.getDocsPath() exists
			assureDocsPath(context);

			// enable VT in editor if available for the site
			String placementId = SessionManager.getCurrentToolSession().getPlacementId();
			String siteId = SiteService.findTool(placementId).getContext();
			boolean enableVT = (SakaiBLTIUtil.showProviderInEditor(siteId, "VoiceThread Editor")) ? true : false;
			String callerId = (String) context.get("question_id");
			String callingQuestionPage = (callerId != null) ? "Question_" + callerId + ".htm" : "Question.htm";
			
			response.println("<textarea " + (this.optional ? "style=\"display:none; position:absolute; top:0px; left:0px;\"" : "") + " id=\"" + id
					+ "\" name=\"" + id + "\" " + (readOnly ? " disabled=\"disabled\"" : "") + ">");
			response.print(Validator.escapeHtmlTextarea(value));
			response.println("</textarea>");
			response.println("<script type=\"text/javascript\" defer=\"1\">sakai.editor.collectionId =\"" + context.getDocsPath() + "\";");
			response.println("function config(){}");
			
			// enable VT for the Question Presentation box only and not other boxes
			if (enableVT && "Question".equals(editRendererTitle))
			{
				response.println("config.prototype.enableVT=true;");
				response.println("config.prototype.serverUrl='" + ServerConfigurationService.getString("serverUrl") + "';");
				response.println("config.prototype.siteId='" + siteId + "';");
				response.println("config.prototype.resourceId='" + callingQuestionPage + "';");
			}
			else 
				response.println("config.prototype.enableVT=false;");
			
			response.println("if (enableBrowse == false)");
			response.println("{");
			response.println("config.prototype.disableBrowseServer=true;");
			response.println("}");
			response.println("sakai.editor.launch('" + id + "',new config(),getWidth('.ambrosiaHtmlEditSize_" + this.size.toString()
					+ "'),getHeight('.ambrosiaHtmlEditSize_" + this.size.toString() + "'));");
			response.println("</script>");
			
			// on submit, record the editor's changed flag
			context.addOnSubmit("document.getElementById('" + "changed_" + decodeId + "').value = CKEDITOR.instances['" + id + "'].checkDirty();");
			response.println("<input type=\"hidden\" id=\"" + "changed_" + decodeId + "\" name=\"" + "changed_" + decodeId + "\" value =\"\" />");
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
		response.println("<div class=\"ckeditorGap_" + this.size.toString() + "\"></div>");

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

	/**
	 * Make sure the context.getDocsPath() collection exists, and has the proper PROP_ALTERNATE_REFERENCE.
	 * 
	 * @param context
	 *        The context.
	 */
	protected void assureDocsPath(Context context)
	{
		// make sure we can read!
		pushAdvisor();

		try
		{
			String docsPath = context.getDocsPath();
			if (docsPath == null) return;

			String[] pathComponents = StringUtil.split(docsPath, "/");
			if (pathComponents.length < 3) return;

			String refRoot = "/" + pathComponents[2];
			try
			{
				ContentCollection container = contentHostingService().getCollection(docsPath);

				// make sure it has the property
				if (null == container.getProperties().getProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE))
				{
					// add it
					try
					{
						ContentCollectionEdit edit = contentHostingService().editCollection(docsPath);
						edit.getPropertiesEdit().addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, refRoot);
						contentHostingService().commitCollection(edit);
					}
					catch (IdUnusedException e)
					{
					}
					catch (TypeException e)
					{
					}
					catch (PermissionException e)
					{
					}
					catch (InUseException e)
					{
					}
				}
			}
			catch (IdUnusedException e)
			{
				// create the collection (and any missing containing ones), and add the property
				try
				{
					ContentCollectionEdit edit = contentHostingService().addCollection(docsPath);
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, pathComponents[pathComponents.length - 1]);
					edit.getPropertiesEdit().addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, refRoot);
					contentHostingService().commitCollection(edit);
				}
				catch (IdUsedException e2)
				{
				}
				catch (IdInvalidException e2)
				{
				}
				catch (PermissionException e2)
				{
				}
				catch (InconsistentException e2)
				{
				}
			}
			catch (TypeException e)
			{
			}
			catch (PermissionException e)
			{
			}
		}
		finally
		{
			// clear the security advisor
			popAdvisor();
		}
	}

	/**
	 * @return The ContentHostingService, via the component manager.
	 */
	protected ContentHostingService contentHostingService()
	{
		return (ContentHostingService) ComponentManager.get(ContentHostingService.class);
	}

	/**
	 * Remove our security advisor.
	 */
	protected void popAdvisor()
	{
		securityService().popAdvisor();
	}

	/**
	 * Setup a security advisor.
	 */
	protected void pushAdvisor()
	{
		// setup a security advisor
		securityService().pushAdvisor(new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * @return The ContentHostingService, via the component manager.
	 */
	protected SecurityService securityService()
	{
		return (SecurityService) ComponentManager.get(SecurityService.class);
	}

}
