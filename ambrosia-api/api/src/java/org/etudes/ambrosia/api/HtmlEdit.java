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
 * HtmlEdit presents a html text input for the user to edit.
 */
public interface HtmlEdit extends Component
{
	/** Three sizes. */
	enum Sizes
	{
		full, small, tall
	}

	/**
	 * Set a decision to enable on-load cursor focus on this field.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	HtmlEdit setFocus(Decision decision);

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
	HtmlEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references);

	/**
	 * Set the editor as initially disabled, showing the rendered text, with the user option to enable by clicking on an edit icon.
	 * 
	 * @return self.
	 */
	HtmlEdit setOptional();

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 */
	HtmlEdit setProperty(PropertyReference propertyReference);

	/**
	 * Set the read-only decision.
	 * 
	 * @param decision
	 *        The read-only decision.
	 * @return self.
	 */
	HtmlEdit setReadOnly(Decision decision);

	/**
	 * Set the editor size.
	 * 
	 * @param size
	 *        The size.
	 * @return self.
	 */
	HtmlEdit setSize(Sizes size);

	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	HtmlEdit setTitle(String selector, PropertyReference... references);
}
