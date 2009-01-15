/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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
 * TextEdit presents a text input for the user to edit.
 */
public interface TextEdit extends Component
{
	/**
	 * Set the selection to auto-submit on change, and report this destination when it does.
	 * 
	 * @param destination
	 *        The destination for the submit.
	 * @return self
	 */
	TextEdit setDestination(Destination destination);

	/**
	 * Set a decision to enable on-load cursor focus on this field.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	TextEdit setFocus(Decision decision);

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
	TextEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references);

	/**
	 * Set the property reference for the options.
	 * 
	 * @param propertyReference
	 *        The property reference for options.
	 */
	TextEdit setOptions(PropertyReference propertyReference);

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 */
	TextEdit setProperty(PropertyReference propertyReference);

	/**
	 * Set the read-only decision.
	 * 
	 * @param decision
	 *        The read-only decision.
	 * @return self.
	 */
	TextEdit setReadOnly(Decision decision);

	/**
	 * Set the size of the text edit box in columns.
	 * 
	 * @param rows
	 *        The number of rows to show.
	 * @param cols
	 *        The number of columns to show.
	 * @return self.
	 */
	TextEdit setSize(int rows, int cols);

	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	TextEdit setTitle(String selector, PropertyReference... references);
}
