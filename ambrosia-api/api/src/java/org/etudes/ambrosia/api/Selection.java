/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2013, 2014 Etudes, Inc.
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
 * Selection presents a selection of one or more options for the user to select.
 */
public interface Selection extends Component
{
	/** orientation. */
	enum Orientation
	{
		dropdown, horizontal, vertical
	};

	/**
	 * Add a component to the container for the most recently added selection.
	 * 
	 * @param component
	 *        the component to add.
	 * @param separate
	 *        if true, render the component separately, else render it inline
	 */
	Selection addComponentToSelection(Component component, boolean separate);

	/**
	 * Add a selection, one more choice the user can select.
	 * 
	 * @param display
	 *        The message for display.
	 * @param value
	 *        The message for the value to return if selected.
	 * @return self.
	 */
	Selection addSelection(Message display, Message value);

	/**
	 * Get the value of no print
	 * 
	 * @return String value of noprint
	 */
	String getNoprint();

	/**
	 * Set the property reference for the correct value.
	 * 
	 * @param propertyReference
	 *        The property reference for the correct value.
	 * @return self.
	 */
	Selection setCorrect(PropertyReference correctReference);

	/**
	 * Set the decision for including the correct markers.
	 * 
	 * @param decision
	 *        The decision for including the correct markers.
	 * @return self.
	 */
	Selection setCorrectDecision(Decision decision);

	/**
	 * Set the selection to auto-submit on change, and report this destination when it does.
	 * 
	 * @param destination
	 *        The destination for the submit.
	 * @return self
	 */
	Selection setDestination(Destination destination);

	/**
	 * Set the number of lines to display (if dropdown).
	 * 
	 * @param height
	 *        The number of lines to display (if dropdown).
	 * @return self.
	 */
	Selection setHeight(int height);

	/**
	 * Set the value of noprint
	 * 
	 * @param noprint Set to TRUE if we don't want to print the navigation bar
	 */
	void setNoprint(String noprint);

	/**
	 * Set the value of noprintflag
	 * 
	 * @param noprintflag
	 * @return NavigationBar
	 */
	Selection setNoprintflag(boolean noprintflag);

	/**
	 * Set an alert that will trigger once on submit if the field is empty.
	 * 
	 * @param decision
	 *        The decision to include the alert (if null, the alert is unconditionally included).
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 */
	Selection setOnEmptyAlert(Decision decision, String selector, PropertyReference... references);

	/**
	 * Set the orientation of multiple selection choices.
	 * 
	 * @param orientation
	 *        The orientation.
	 * @return self.
	 */
	Selection setOrientation(Orientation orientation);

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 */
	Selection setProperty(PropertyReference propertyReference);

	/**
	 * Set the read-only decision.
	 * 
	 * @param decision
	 *        The read-only decision.
	 * @return self.
	 */
	Selection setReadOnly(Decision decision);

	/**
	 * Set the read-only and show only selected option decision.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	Selection setReadOnlyCollapsed(Decision decision);

	/**
	 * Set the select-all option as or as part of the title setting.
	 * 
	 * @param setting
	 *        the select-all setting
	 * @return self.
	 */
	Selection setSelectAll(boolean setting);

	/**
	 * Set the value that is decoded when the user makes the selection.
	 * 
	 * @param value
	 *        The value decoded when the user make the selection.
	 * @return self.
	 */
	Selection setSelectedValue(String value);

	/**
	 * Set the selection from a model.
	 * 
	 * @param modelRef
	 *        The ref to get the objects from the model, one for each selection choice.
	 * @param iteratorName
	 *        The name to place in the model to represent each selection choice.
	 * @param valueMessage
	 *        The message to get, from the named selection choice object, the value.
	 * @param displayMessage
	 *        The message to get, from the named selection choice object, the display text.
	 * @return self.
	 */
	Selection setSelectionModel(PropertyReference modelRef, String iteratorName, Message valueMessage, Message displayMessage);
	
	/**
	 * Set a decision to use to determine if we are going to be single select (true) or multiple select (false).
	 * 
	 * @param decision
	 *        The decision to use to determine if we are going to be single select (true) or multiple select (false).
	 * @return self.
	 */
	Selection setSingleSelectDecision(Decision decision);

	/**
	 * Set so that we submit on change, and the selected value is the destination.
	 * 
	 * @return self.
	 */
	Selection setSubmitValue();
	
	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Selection setTitle(String selector, PropertyReference... references);
}
