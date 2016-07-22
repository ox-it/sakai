/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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

import java.util.ArrayList;
import java.util.List;


/**
 * UiFillInline presents a set of dropdowns for the user to edit embedded in a surrounding string. The string is formatted with "{}" where the dropdowns are expected.<br />
 * The values are taken from / returned to an array property by reference.
 */
public interface FillInline extends Component
{
	
	/**
	 * Set the property reference for the correct value Booleans marking each existing entry as correct or not.
	 * 
	 * @param propertyReference
	 *        The property reference for the correct value Booleans marking each existing entry as correct or not.
	 * @return self.
	 */
	FillInline setCorrect(PropertyReference correctReference);

	/**
	 * Set the decision for including the correct markers.
	 * 
	 * @param decision
	 *        The decision for including the correct markers.
	 * @return self.
	 */
	FillInline setCorrectDecision(Decision decision);

	/**
	 * Set the relative icon path to the icon to use to mark already correct entries.
	 * 
	 * @param propertyReference
	 *        The property reference for the set of Booleans marking each existing entry as correct or not.
	 * @param correctIcon
	 *        The relative icon path to the icon to use to mark already correct entries.
	 * @param correctMessage
	 *        The message selector string to use for the alternate text for the correct entry marking.
	 * @param incorrectIcon
	 *        The relative icon path to the icon to use to mark already incorrect entries.
	 * @param incorrectMessage
	 *        The message selector string to use for the alternate text for the incorrect entry marking.
	 * @param decision
	 *        The decision(s) to include the correct marking (if null, it's just included).
	 * @return self.
	 */
	FillInline setCorrectMarker(PropertyReference propertyReference, String correctIcon, String correctMessage, String incorrectIcon,
			String incorrectMessage, Decision... decision);

	/**
	 * Set a decision to enable on-load cursor focus on this field.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	FillInline setFocus(Decision decision);

	/**
	 * Set an alert that will triger once on submit if the field is empty.
	 * 
	 * @param decision
	 *        The decision to include the alert (if null, the alert is unconditionally included).
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 */
	FillInline setOnEmptyAlert(Decision decision, String selector, PropertyReference... references);

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 * @return self.
	 */
	FillInline setProperty(PropertyReference propertyReference);

	/**
	 * Set the decision for read-only.
	 * 
	 * @param decision
	 *        The Decision to provide the read only setting.
	 * @return self.
	 */
	FillInline setReadOnly(Decision decision);
	
	/**
	 * Set the list of lists for selection values.
	 * 
	 * @param selectionLists
	 *        The list of string lists of selection values.
	 * @return self.
	 */
	FillInline setSelectionLists(List<ArrayList<String>> selectionLists);
	
	/**
	 * Show user entered response with a grey background instead of the text field
	 * 
	 * @param decision
	 *        The Decision to provide the show response setting.
	 * @return self.
	 */
	FillInline setShowResponse(Decision decision);

	/**
	 * Set the fill-in text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	FillInline setText(String selector, PropertyReference... references);

	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	FillInline setTitle(String selector, PropertyReference... references);

}
