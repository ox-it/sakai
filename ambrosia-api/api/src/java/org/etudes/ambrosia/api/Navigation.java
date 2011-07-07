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

package org.etudes.ambrosia.api;

/**
 * Navigation presents a navigation control (button or text link) to the user. The result of the press is a navigation to some tool destination. A form submit is optional.
 */
public interface Navigation extends Component
{
	/** Icon styles. */
	enum IconStyle
	{
		left, none, right
	};

	/** selection requirements. */
	enum SelectRequirement
	{
		multiple, none, single, some
	};

	/** rendering styles. */
	enum Style
	{
		button, link
	};

	/**
	 * Add an alternate icon, used if this decision is true.
	 * 
	 * @param icon
	 *        The alternate icon.
	 * @param decision
	 *        The decision.
	 * @return
	 */
	Navigation addIcon(String icon, Decision decision);

	/**
	 * Access the tool destination for the navigation.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The focus.
	 * @return The tool destination for the selection link for this item.
	 */
	String getDestination(Context context, Object focus);

	/**
	 * Access the submit setting.
	 * 
	 * @return The submit setting.
	 */
	boolean getSubmit();

	/**
	 * Set the access key for the navigation to the character produced by this message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        One or more PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Navigation setAccessKey(String selector, PropertyReference... references);

	/**
	 * Set the decision and other details about makeing this a two step / confirmed process
	 * 
	 * @param decision
	 *        The decicion.
	 * @param cancelSelcetor
	 *        A message selector for the "cancel" wording.
	 * @param cancelIcon
	 *        An icon path for the "cancel" choice.
	 * @param msgSelector
	 *        A message selector for the confirm message.
	 * @param references
	 *        One or more PropertyReferences to form the additional values in the confirm message.
	 * @return self.
	 */
	Navigation setConfirm(Decision decision, String cancelSelector, String cancelIcon, String msgSelector, PropertyReference... references);

	/**
	 * Set this as a default choice.
	 * 
	 * @return self.
	 */
	Navigation setDefault();

	/**
	 * Set the decision to make this a default choice or not.
	 * 
	 * @param defaultDecision
	 *        The decision, or set of decisions, all of which must pass to make this the default choice.
	 * @return self.
	 */
	Navigation setDefault(Decision... defaultDecision);

	/**
	 * Set the descriptive text for the navigation.
	 * 
	 * @param message
	 *        The message.
	 * @return self.
	 */
	Navigation setDescription(Message message);

	/**
	 * Set the descriptive text for the navigation.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        One or more PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Navigation setDescription(String selector, PropertyReference... references);

	/**
	 * Set the tool destination to use when clicked.
	 * 
	 * @param destination
	 *        The tool destination.
	 * @return self.
	 */
	Navigation setDestination(Destination destination);

	/**
	 * Set the decision to be disabled (inactive, but visible).
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to be disabled.
	 * @return self.
	 */
	Navigation setDisabled(Decision... decision);

	/**
	 * Set the decision to include each entity (if the EntityReference is set to a Collection)
	 * 
	 * @param inclusionDecision
	 *        The decision for inclusion of each entity.
	 * @return self.
	 */
	Navigation setEntityIncluded(Decision inclusionDecision);

	/**
	 * Set the message to show when the requirements are not met.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Navigation setFailedRequirmentsMessage(String selector, PropertyReference... references);

	/**
	 * Set the icon for the navigation
	 * 
	 * @param url
	 *        The full URL to the icon.
	 * @param style
	 *        The icon style (left or right placement).
	 * @return self.
	 */
	Navigation setIcon(String url, IconStyle style);

	/**
	 * Set the decision to be included.
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to be included.
	 * @return self.
	 */
	Navigation setIncluded(Decision... decision);

	/**
	 * Set a reference to an array of Collection of entities to iterate over.<br />
	 * The section will be repeated for each entity. Each repeat will set additional entries in the context.
	 * 
	 * @param reference
	 *        The reference to an array or collection to iterate over.
	 * @param name
	 *        The context name for the current iteration item.
	 * @return self.
	 */
	Navigation setIterator(PropertyReference reference, String name);

	/**
	 * Set this link as a portal (i.e. full screen) link.
	 * 
	 * @return self.
	 */
	Navigation setPortal();

	/**
	 * Set the decision for requirements; if not met, an alert will show instead of having the navigation activated when clicked.
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass as requirements.
	 * @return self.
	 */
	Navigation setRequirements(Decision... decision);

	/**
	 * Set the select requirement, used if the navigation is linked to a select column to declare what is valid.
	 * 
	 * @param requirement
	 *        The select requirement that makes this select valid.
	 * @return self.
	 */
	Navigation setSelectRequirement(SelectRequirement requirement);

	/**
	 * Set the component id that the select requirement is against.
	 * 
	 * @param id
	 *        The component is that the select requirement is against.
	 * @return self.
	 */
	Navigation setSelectRequirementId(String id);

	/**
	 * Set the text to be small.
	 * 
	 * @return self.
	 */
	Navigation setSmall();

	/**
	 * Set the format style.
	 * 
	 * @param style
	 *        The format style.
	 * @return self.
	 */
	Navigation setStyle(Style style);

	/**
	 * Indicate that the navigation needs to submit the form.
	 * 
	 * @return self.
	 */
	Navigation setSubmit();

	/**
	 * Set the navigation title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Navigation setTitle(String selector, PropertyReference... references);

	/**
	 * Indicate that instead of a submit, the action triggers the destination as javascript.
	 * 
	 * @return self.
	 */
	Navigation setTrigger();

	/**
	 * Set the decision about forcing form validation when this navigation (submit only) is pressed.
	 * 
	 * @param decision
	 *        The decicion.
	 * @return self.
	 */
	Navigation setValidation(Decision decision);

	/**
	 * Set the text to be wrapped.
	 * 
	 * @return self.
	 */
	Navigation setWrap();
}
