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
 * UiFootnote describes a footnote...
 */
public interface Footnote
{
	/**
	 * Check if this footnote should be applied to this object.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The entity object focus.
	 * @return true if this footnote should be applied to this object, false if not.
	 */
	boolean apply(Context context, Object focus);

	/**
	 * Get the footnote text message.
	 * 
	 * @return text The footnote text message.
	 */
	Message getText();

	/**
	 * Check if the footnote is included.
	 * 
	 * @param context
	 *        The UiContext
	 * @return true if the footnote is included, false if not.
	 */
	boolean included(Context context);

	/**
	 * Set the criteria decision for marking a specific selector of an entity with the footnote
	 * 
	 * @param criteria
	 *        The criteria decision for marking a specific selector of an entity with the footnote
	 * @return self.
	 */
	Footnote setCriteria(Decision criteria);

	/**
	 * Set the decision to include the footnote.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	Footnote setIncluded(Decision decision);

	/**
	 * Set the footnote text message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	Footnote setText(String selector, PropertyReference... references);
}
