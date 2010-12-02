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
import java.util.Collection;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiNavigation presents a navigation control (button or text link) to the user. The result of the press is a navigation to some tool destination.
 */
public class UiNavigation extends UiComponent implements Navigation
{
	/**
	 * Escape with a preceding '\' (for javascript) any single quotes in the source string.
	 * 
	 * @param source
	 *        The source string.
	 * @return The source string with the single quotes escaped to live in a javascript single-quoted string.
	 */
	protected static String escapeSingleQuote(String source)
	{
		if (source == null) return null;

		StringBuilder dest = new StringBuilder();
		for (int i = 0; i < source.length(); i++)
		{
			char c = source.charAt(i);
			if (c == '\'') dest.append('\\');
			dest.append(c);
		}
		return dest.toString();
	}

	/**
	 * Generate the link script for a navigation
	 * 
	 * @param context
	 *        The context.
	 * @param id
	 *        The render id.
	 * @param confirm
	 *        true if we are doing confirm for this nav.
	 * @param validate
	 *        true if we need to validate before linking.
	 * @param submit
	 *        true if we generate a submit, false for a link.
	 * @param destination
	 *        the link destination.
	 * @param root
	 *        The root of the destination URL.
	 * @param requirements
	 *        true if we need to check requirements before allowing the nav to be processed.
	 * @param trigger
	 *        if set, use the destination as javascript to run.
	 */
	protected static void generateLinkScript(Context context, String id, boolean confirm, boolean validate, boolean submit, String destination,
			String root, boolean requirements, boolean trigger, boolean parent)
	{
		// the act method call
		String action = null;
		if (!trigger)
		{
			action = "ambrosiaNavigate(enabled_" + id + ", 'enable_" + id + "()', " + Boolean.toString(confirm) + ", 'confirm_" + id + "', "
					+ Boolean.toString(validate) + ", " + Boolean.toString(submit) + ", '" + escapeSingleQuote(destination) + "','" + root + "', "
					+ (requirements ? "'requirements_" + id + "()','failure_" + id + "'" : "null, null") + ", " + (parent ? "true" : "false") + ");";
		}
		else
		{
			action = destination;
		}

		// the script
		StringBuffer script = new StringBuffer();

		script.append("var enabled_" + id + "=" + (confirm ? "false" : "true") + ";\n");
		script.append("function cancel_" + id + "()\n{\n\tenabled_" + id + "=false;\n}\n");
		script.append("function enable_" + id + "()\n{\n\tenabled_" + id + "=true;\n}\n");
		script.append("function act_" + id + "()\n{\n\t" + action + "\n}\n");
		context.addScript(script.toString());
	}

	/** Message to form the access key. */
	protected Message accessKey = null;

	/** The icon for the "cancel" in a confirm. */
	protected String confirmCancelIcon = null;

	/** The message for the "cancel" in a confirm. */
	protected Message confirmCancelMsg = null;

	/** Decision to make a two step (confirm) button. */
	protected Decision confirmDecision = null;

	/** The confirm message. */
	protected Message confirmMsg = null;

	/** The default decision. */
	protected Decision defaultDecision = null;

	/** If set to true, this is a default decision - overrides the defaultDecision set. */
	protected boolean defaultSet = false;

	/** The message selector for the button description. */
	protected Message description = null;

	/** The tool destination for this navigation. */
	protected Destination destination = null;

	/** The disabled decision. */
	protected Decision disabledDecision = null;

	/** The inclusion decision for each entity. */
	protected Decision entityIncluded = null;

	/** The message selector for the failed requirements message. */
	protected Message failedRequirements = null;

	/** The message selector for the failed requirements dismiss message. */
	protected Message failedRequirementsOk = new UiMessage().setMessage("ok");

	/** Full URL to the icon. */
	protected String icon = null;

	/** Icon placement: left or right. */
	protected IconStyle iconStyle = IconStyle.left;

	/** The context name for the current iteration object. */
	protected String iteratorName = null;

	/** The reference to an entity to iterate over. */
	protected PropertyReference iteratorReference = null;

	/** Set if the link is a portal (i.e. full screen) link. */
	protected boolean portal = false;

	/** The requirements decision. */
	protected Decision requirementsDecision = null;

	/** The icon for the requirements failed display. */
	protected String requirementsOkIcon = "!/ambrosia_library/icons/ok.png";

	/** The requirement of the related select to make this valid. */
	protected SelectRequirement selectRequirement = SelectRequirement.none;

	/** The component id the select requirement is against. */
	protected String selectRequirementId = null;

	/** Set to get the navigation text smaller. */
	protected boolean small = false;

	/** The display style. */
	protected Style style = Style.link;

	/** If true, we need to submit the form on the press. */
	protected boolean submit = false;

	/** The message selector for the button title. */
	protected Message title = null;

	/** If true, we need to trigger the destination as javascript on the press. */
	protected boolean trigger = false;

	/** Decision to force form validation when pressed. */
	protected Decision validationDecision = null;

	/** let the display wrap or not. */
	protected boolean wrap = false;

	/**
	 * Public no-arg constructor.
	 */
	public UiNavigation()
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
	protected UiNavigation(UiServiceImpl service, Element xml)
	{
		// control stuff
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
			setStyle("BUTTON".equals(style) ? Style.button : Style.link);
		}

		// small?
		String small = StringUtil.trimToNull(xml.getAttribute("small"));
		if (small != null)
		{
			setSmall();
		}

		// wrap?
		String wrap = StringUtil.trimToNull(xml.getAttribute("wrap"));
		if (wrap != null)
		{
			setWrap();
		}

		// short form for destination - attribute "destination" as the destination
		String destination = StringUtil.trimToNull(xml.getAttribute("destination"));
		if (destination != null)
		{
			setDestination(service.newDestination().setDestination(destination));
		}

		// short form for submit
		String submit = StringUtil.trimToNull(xml.getAttribute("submit"));
		if (submit != null)
		{
			if ("TRUE".equals(submit))
			{
				setSubmit();
			}
			else if ("TRIGGER".equals(submit))
			{
				setTrigger();
			}
		}

		// short form for default
		String dflt = StringUtil.trimToNull(xml.getAttribute("default"));
		if ((dflt != null) && ("TRUE".equals(dflt)))
		{
			setDefault();
		}

		// short form for parent
		String portal = StringUtil.trimToNull(xml.getAttribute("portal"));
		if ((portal != null) && ("TRUE".equals(portal)))
		{
			setPortal();
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

		// selectRequirement
		String selectRequirement = StringUtil.trimToNull(xml.getAttribute("selectRequirement"));
		if (selectRequirement != null)
		{
			setSelectRequirement(SelectRequirement.valueOf(selectRequirement.toLowerCase()));
		}

		// selectRequirementId
		String selectRequirementId = StringUtil.trimToNull(xml.getAttribute("selectRequirementId"));
		if (selectRequirementId != null)
		{
			setSelectRequirementId(selectRequirementId);
		}

		// short form for select requirement message
		String selectRequirementMessage = StringUtil.trimToNull(xml.getAttribute("selectRequirementMessage"));
		if (selectRequirementMessage != null)
		{
			this.setFailedRequirmentsMessage(selectRequirementMessage);
		}

		// short form for select requirement message
		selectRequirementMessage = StringUtil.trimToNull(xml.getAttribute("failedRequirementsMessage"));
		if (selectRequirementMessage != null)
		{
			this.setFailedRequirmentsMessage(selectRequirementMessage);
		}

		// confirm
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "confirm");
		if (settingsXml != null)
		{
			// decision
			Decision decision = null;
			String decisionShort = StringUtil.trimToNull(settingsXml.getAttribute("decision"));
			if ((decisionShort != null) && ("TRUE".equals(decisionShort)))
			{
				decision = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("TRUE"));
			}
			else
			{
				decision = service.parseDecisions(settingsXml);
			}

			String cancelMsg = StringUtil.trimToNull(settingsXml.getAttribute("cancelSelector"));
			String cancelIcon = StringUtil.trimToNull(settingsXml.getAttribute("cancelIcon"));
			String msg = StringUtil.trimToNull(settingsXml.getAttribute("selector"));
			String ref = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			PropertyReference pRef = null;
			if (ref != null) pRef = service.newPropertyReference().setReference(ref);

			if (pRef == null)
			{
				setConfirm(decision, cancelMsg, cancelIcon, msg);
			}
			else
			{
				setConfirm(decision, cancelMsg, cancelIcon, msg, pRef);
			}

			Element innerSettingsXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerSettingsXml != null)
			{
				this.confirmMsg = new UiMessage(service, innerSettingsXml);
			}

			innerSettingsXml = XmlHelper.getChildElementNamed(settingsXml, "cancel");
			if (innerSettingsXml != null)
			{
				cancelMsg = StringUtil.trimToNull(innerSettingsXml.getAttribute("selector"));
				if (cancelMsg != null) this.confirmCancelMsg = new UiMessage().setMessage(cancelMsg);
				cancelIcon = StringUtil.trimToNull(innerSettingsXml.getAttribute("icon"));
				if (cancelIcon != null) this.confirmCancelIcon = cancelIcon;
			}
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.title = new UiMessage(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "failedRequirementsMessage");
		if (settingsXml != null)
		{
			// let Message parse this
			this.failedRequirements = new UiMessage(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "description");
		if (settingsXml != null)
		{
			// let Message parse this
			setDescription(new UiMessage(service, settingsXml));
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
		if (settingsXml != null)
		{
			// let Destination parse this
			this.destination = new UiDestination(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "disabled");
		if (settingsXml != null)
		{
			this.disabledDecision = service.parseDecisions(settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "requirements");
		if (settingsXml != null)
		{
			this.requirementsDecision = service.parseDecisions(settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "default");
		if (settingsXml != null)
		{
			this.defaultDecision = service.parseDecisions(settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "validate");
		if (settingsXml != null)
		{
			this.validationDecision = service.parseDecisions(settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "icon");
		if (settingsXml != null)
		{
			String icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			String iStyle = StringUtil.trimToNull(settingsXml.getAttribute("style"));
			IconStyle is = "LEFT".equals(iStyle) ? IconStyle.left : (("RIGHT".equals(iStyle)) ? IconStyle.right : IconStyle.none);
			setIcon(icon, is);
		}

		// entity included
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityIncluded");
		if (settingsXml != null)
		{
			Decision decision = service.parseDecisions(settingsXml);
			this.entityIncluded = decision;
		}

		// iterator
		settingsXml = XmlHelper.getChildElementNamed(xml, "iterator");
		if (settingsXml != null)
		{
			String name = StringUtil.trimToNull(settingsXml.getAttribute("name"));
			if (name != null) this.iteratorName = name;

			// short for model
			String model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			if (model != null)
			{
				this.iteratorReference = service.newPropertyReference().setReference(model);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.iteratorReference = service.parsePropertyReference(innerXml);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDestination(Context context, Object focus)
	{
		if (this.destination == null) return null;

		// included?
		if (!isIncluded(context, focus)) return null;

		// disabled?
		if (isDisabled(context, focus)) return null;

		return this.destination.getDestination(context, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getSubmit()
	{
		return this.submit;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		// the iterator
		Object iterator = null;
		if (this.iteratorReference != null)
		{
			iterator = this.iteratorReference.readObject(context, focus);
		}

		// if iterating over a Collection, we will repeat our contents once for each one
		if ((iterator != null) && (iterator instanceof Collection))
		{
			Collection c = (Collection) iterator;
			int index = -1;
			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

				// check if this entity is to be included
				if ((this.entityIncluded == null) || (this.entityIncluded.decide(context, o)))
				{
					renderContents(context, o);
				}

				// remove the context item
				if (this.iteratorName != null)
				{
					context.remove(this.iteratorName);
				}
			}

			return true;
		}

		// if iterating over an array, we will repeat our contents once for each one
		if ((iterator != null) && (iterator.getClass().isArray()))
		{
			Object[] c = (Object[]) iterator;
			int index = -1;
			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

				// check if this entity is to be included
				if ((this.entityIncluded == null) || (this.entityIncluded.decide(context, o)))
				{
					renderContents(context, o);
				}

				// remove the context item
				if (this.iteratorName != null)
				{
					context.remove(this.iteratorName);
				}
			}

			return true;
		}

		// if no repeating entity, just render once
		renderContents(context, focus);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setAccessKey(String selector, PropertyReference... references)
	{
		this.accessKey = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setConfirm(Decision decision, String cancelSelector, String cancelIcon, String msgSelector, PropertyReference... references)
	{
		this.confirmDecision = decision;
		this.confirmCancelMsg = new UiMessage().setMessage(cancelSelector);
		this.confirmCancelIcon = cancelIcon;
		this.confirmMsg = new UiMessage().setMessage(msgSelector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setDefault()
	{
		this.defaultSet = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setDefault(Decision... defaultDecision)
	{
		if (defaultDecision != null)
		{
			if (defaultDecision.length == 1)
			{
				this.defaultDecision = defaultDecision[0];
			}

			else
			{
				this.defaultDecision = new UiAndDecision().setRequirements(defaultDecision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setDescription(Message message)
	{
		this.description = message;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setDescription(String selector, PropertyReference... references)
	{
		this.description = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setDestination(Destination destination)
	{
		this.destination = destination;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setDisabled(Decision... decision)
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
	public Navigation setEntityIncluded(Decision inclusionDecision)
	{
		this.entityIncluded = inclusionDecision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setFailedRequirmentsMessage(String selector, PropertyReference... references)
	{
		this.failedRequirements = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setIcon(String icon, IconStyle style)
	{
		this.icon = icon;
		this.iconStyle = style;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setIncluded(Decision... decision)
	{
		super.setIncluded(decision);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setIterator(PropertyReference reference, String name)
	{
		this.iteratorReference = reference;
		this.iteratorName = name;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setPortal()
	{
		this.portal = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setRequirements(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.requirementsDecision = decision[0];
			}
			else
			{
				this.requirementsDecision = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setSelectRequirement(SelectRequirement requirement)
	{
		this.selectRequirement = requirement;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setSelectRequirementId(String id)
	{
		this.selectRequirementId = id;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setSmall()
	{
		this.small = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setStyle(Navigation.Style style)
	{
		this.style = style;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setSubmit()
	{
		this.submit = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setTrigger()
	{
		this.trigger = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setValidation(Decision decision)
	{
		this.validationDecision = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation setWrap()
	{
		this.wrap = true;
		return this;
	}

	/**
	 * Check if this is a default choice.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if this is a default choice, false if not.
	 */
	protected boolean isDefault(Context context, Object focus)
	{
		if (this.defaultSet) return true;
		if (this.defaultDecision == null) return false;
		return this.defaultDecision.decide(context, focus);
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
		// if no destination, we are disabled (unless submitting)
		if ((this.destination == null) && (!this.submit)) return true;

		if (this.disabledDecision == null) return false;
		return this.disabledDecision.decide(context, focus);
	}

	/**
	 * Check if this passes requirements.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if this passes requirements, false if not.
	 */
	protected boolean passesRequirements(Context context, Object focus)
	{
		// if no requirements, all is well
		if (this.requirementsDecision == null) return true;

		return this.requirementsDecision.decide(context, focus);
	}

	/**
	 * Render one iteration.
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus.
	 */
	protected void renderContents(Context context, Object focus)
	{
		// included?
		// if (!isIncluded(context, focus)) return;

		// disabled?
		boolean disabled = isDisabled(context, focus);

		// generate id
		// if this component has a name-id, and it has already been registered in the context, we are an alias:
		// use the registered value as id and skip our script / confirm generation.
		String id = null;
		boolean isAliasRendering = false;
		if (getId() != null)
		{
			id = context.getRegistration(getId());
			if (id != null) isAliasRendering = true;
		}

		if (id == null)
		{
			id = this.getClass().getSimpleName() + "_" + context.getUniqueId();

			// register if we have a name so any alias can use this same id
			if (getId() != null)
			{
				context.register(getId(), id);
			}
		}

		// is this a default choice?
		boolean dflt = isDefault(context, focus);

		// validate? default to true
		boolean validate = true;
		if (this.validationDecision != null)
		{
			validate = this.validationDecision.decide(context, focus);
		}

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
			description = StringUtil.trimToNull(this.description.getMessage(context, focus));
		}
		if (description == null) description = "";

		// make it a two step / confirm?
		boolean confirm = false;
		if (this.confirmDecision != null)
		{
			confirm = this.confirmDecision.decide(context, focus);
		}

		// are there select requirements?
		boolean selectRequirements = (this.selectRequirement != SelectRequirement.none) && (this.failedRequirements != null);
		// TODO: also consider decision requirements
		String relatedId = null;
		if (selectRequirements)
		{
			// use our configured target
			relatedId = this.selectRequirementId;

			// if none, try the context
			if (relatedId == null)
			{
				relatedId = (String) context.get("ambrosia.navigation.related.id");
			}

			// if still none, we have no requirements
			if (relatedId == null) selectRequirements = false;
		}

		// are there other requirements?
		boolean failedRequirements = false;
		if (this.requirementsDecision != null)
		{
			// do they fail?
			if (!passesRequirements(context, focus))
			{
				failedRequirements = true;
			}
		}

		PrintWriter response = context.getResponseWriter();

		// generate the script and confirm stuff only if not an alias
		if (!isAliasRendering)
		{
			// our action javascript
			if (!disabled)
			{
				// usually use the return url as the root, but for portal, form the direct tool url for root
				String root = (String) context.get("sakai.return.url");
				if (this.portal)
				{
					// the navigation destination starts with /toolid/, continues with the tool parameters
					root = ((String) context.get("sakai.server.url")) + "/portal/directtool";
				}
				generateLinkScript(context, id, confirm, validate, this.submit,
						(this.destination != null ? this.destination.getDestination(context, focus) : ""), root,
						(selectRequirements | failedRequirements), this.trigger, this.portal);
			}

			if (confirm)
			{
				response.println("<div class=\"ambrosiaConfirmPanel\" style=\"display:none; left:0px; top:0px; width:390px; height:130px\" id=\"confirm_"
						+ id + "\">");
				response.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
				response.println("<td colspan=\"2\" style=\"padding:1em; white-space:normal; line-height:1em; \" align=\"left\">"
						+ this.confirmMsg.getMessage(context, focus) + "</td>");
				response.println("</tr><tr>");
				response.println("<td style=\"padding:1em\" align=\"left\"><input type=\"button\" value=\""
						+ this.confirmCancelMsg.getMessage(context, focus)
						+ "\" onclick=\"hideConfirm('confirm_"
						+ id
						+ "','cancel_"
						+ id
						+ "()');return false;\" "
						+ ((this.confirmCancelIcon != null) ? "style=\"padding-left:2em; background: #eee url('"
								+ context.getUrl(this.confirmCancelIcon) + "') .2em no-repeat;\"" : "") + "/></td>");
				response.print("<td style=\"padding:1em\" align=\"right\"><input type=\"button\" value=\"" + title
						+ "\" onclick=\"hideConfirm('confirm_" + id + "','act_" + id + "();');return false;\"");
				if (this.icon != null)
				{
					response.print(" style=\"padding-left:2em; background: #eee url('" + context.getUrl(this.icon) + "') .2em no-repeat;\"");
				}
				response.println("/></td>");
				response.println("</tr></table></div>");
			}

			if (selectRequirements || failedRequirements)
			{
				// the "failure" panel shown if requirements are not met
				response.println("<div class=\"ambrosiaConfirmPanel\" style=\"display:none; left:0px; top:0px; width:390px; height:130px\" id=\"failure_"
						+ id + "\">");
				response.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
				response.println("<td colspan=\"2\" style=\"padding:1em; white-space:normal; line-height:1em; \" align=\"left\">"
						+ this.failedRequirements.getMessage(context, focus) + "</td>");
				response.println("</tr><tr>");
				response.println("<td style=\"padding:1em\" align=\"left\"><input type=\"button\" value=\""
						+ this.failedRequirementsOk.getMessage(context, focus)
						+ "\" onclick=\"hideConfirm('failure_"
						+ id
						+ "','');return false;\" "
						// TODO: do we need confirm cancel? -ggolden
						+ ((this.requirementsOkIcon != null) ? "style=\"padding-left:2em; background: #eee url('"
								+ context.getUrl(this.requirementsOkIcon) + "') .2em no-repeat;\"" : "") + "/></td>");
				response.println("</tr></table></div>");

				// validation function
				StringBuffer script = new StringBuffer();
				script.append("function requirements_" + id + "()\n{\n" + "");

				if (failedRequirements)
				{
					script.append("\treturn false;\n");
				}

				// the select requirements
				else
				{
					script.append("\tcount = ambrosiaCountChecked('" + relatedId + "');\n");
					switch (this.selectRequirement)
					{
						case multiple:
						{
							script.append("\treturn count > 1;\n");
							break;
						}
						case single:
						{
							script.append("\treturn count == 1;\n");
							break;
						}
						case some:
						{
							script.append("\treturn count > 0;\n");
							break;
						}
					}
				}

				script.append("}\n");
				context.addScript(script.toString());
			}
		}

		if (this.small)
		{
			response.print("<div class=\"ambrosiaNavSmall\">");
		}
		else if (this.wrap)
		{
			response.print("<span class=\"ambrosiaNavWrap\">");
		}
		else
		{
			response.print("<span class=\"ambrosiaNavNormal\">");
		}

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
					if ((this.icon != null) && (this.iconStyle == IconStyle.left))
					{
						if (!disabled) response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");
						response.print("<img style=\"vertical-align:text-bottom; padding-right:0.3em; border-style: none;\" src=\""
								+ context.getUrl(this.icon) + "\" " + "title=\"" + description + "\" " + "alt=\"" + description + "\" />");
						if (!disabled) response.print("</a>");
					}

					if (!disabled) response.print("<a href=\"#\" onclick=\"act_" + id + "();return false;\">");

					response.print(title);

					if (!disabled) response.print("</a>");

					if ((this.icon != null) && (this.iconStyle == IconStyle.right))
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
				response.println("<input type=\"button\" "
						+ (dflt ? "class=\"active\"" : "")
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
						+ "title=\""
						+ description
						+ "\" "
						+ (((this.icon != null) && (this.iconStyle == IconStyle.left)) ? "style=\"padding-left:2em; background: #eee url('"
								+ context.getUrl(this.icon) + "') .2em no-repeat;\"" : "")
						+ (((this.icon != null) && (this.iconStyle == IconStyle.right)) ? "style=\"padding-left:.4em; padding-right:2em; background: #eee url('"
								+ context.getUrl(this.icon) + "') right no-repeat;\""
								: "") + "/>");

				break;
			}
		}

		if (this.small)
		{
			response.print("</div>");
		}
		else
		{
			response.print("</span>");
		}
	}
}
