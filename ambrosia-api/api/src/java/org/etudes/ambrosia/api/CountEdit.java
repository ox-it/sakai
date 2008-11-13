/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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
 * CountEdit presents a count input for the user to edit a count.
 */
public interface CountEdit extends SummarizingComponent
{
	/**
	 * Set a decision to enable on-load cursor focus on this field.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	CountEdit setFocus(Decision decision);

	/**
	 * Set a maximum acceptable value for the edit.
	 * 
	 * @param min
	 *        The model reference to compute the maximum acceptable value for the edit.
	 * @return self.
	 */
	CountEdit setMax(PropertyReference max);

	/**
	 * Set a minimum acceptable value for the edit.
	 * 
	 * @param minimum
	 *        The model reference to compute the minimum acceptable value for the edit.
	 * @return self.
	 */
	CountEdit setMin(PropertyReference min);

	/**
	 * Set an alert that will triger once on submit if the field is empty.
	 * 
	 * @param decision
	 *        The decision to include the alert (if null, the alert is unconditionally included).
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references);

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 * @return self.
	 */
	CountEdit setProperty(PropertyReference propertyReference);

	/**
	 * Set the read-only decision.
	 * 
	 * @param decision
	 *        The read-only decision.
	 * @return self.
	 */
	CountEdit setReadOnly(Decision decision);

	/**
	 * Set the size of the text edit box in columns.
	 * 
	 * @param cols
	 *        The number of columns to show.
	 * @return self.
	 */
	CountEdit setSize(int cols);

	/**
	 * Set the field to have a summary if it is used in an iteration.
	 * 
	 * @return self.
	 */
	CountEdit setSummary();

	/**
	 * Set the property reference for the initial value for the summary field.
	 * 
	 * @param propertyReference
	 *        The property reference for the initial value for the summary field.
	 * @return self.
	 */
	CountEdit setSummaryInitialValueProperty(PropertyReference propertyReference);

	/**
	 * Set the summary field text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountEdit setSummaryTitle(String selector, PropertyReference... references);

	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountEdit setTitle(String selector, PropertyReference... references);
}
