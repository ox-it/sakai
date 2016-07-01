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
 * UiFillIn presents a set of text inputs for the user to edit embedded in a surrounding string. The string is formatted with "{}" where the fill-ins are expected.<br />
 * The values are taken from / returned to an array property by reference.
 */
public interface FillIn extends Component
{
	/**
	 * Set the property reference for the correct value Booleans marking each existing entry as correct or not.
	 * 
	 * @param propertyReference
	 *        The property reference for the correct value Booleans marking each existing entry as correct or not.
	 * @return self.
	 */
	FillIn setCorrect(PropertyReference correctReference);

	/**
	 * Set the decision for including the correct markers.
	 * 
	 * @param decision
	 *        The decision for including the correct markers.
	 * @return self.
	 */
	FillIn setCorrectDecision(Decision decision);

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
	FillIn setCorrectMarker(PropertyReference propertyReference, String correctIcon, String correctMessage, String incorrectIcon,
			String incorrectMessage, Decision... decision);

	/**
	 * Set a decision to enable on-load cursor focus on this field.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	FillIn setFocus(Decision decision);

	/**
	 * Set for numeric answers.
	 * 
	 * @return self.
	 */
	FillIn setNumeric();

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
	FillIn setOnEmptyAlert(Decision decision, String selector, PropertyReference... references);

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 * @return self.
	 */
	FillIn setProperty(PropertyReference propertyReference);

	/**
	 * Set the decision for read-only.
	 * 
	 * @param decision
	 *        The Decision to provide the read only setting.
	 * @return self.
	 */
	FillIn setReadOnly(Decision decision);

	/**
	 * Set the fill-in text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	FillIn setText(String selector, PropertyReference... references);

	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	FillIn setTitle(String selector, PropertyReference... references);

	/**
	 * Set the field width (characters).
	 * 
	 * @param width
	 *        The field width in characters for each fill-in field.
	 * @return self.
	 */
	FillIn setWidth(int width);
}
