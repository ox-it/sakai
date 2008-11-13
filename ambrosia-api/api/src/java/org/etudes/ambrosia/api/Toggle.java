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
 * Toggle presents a navigation-like control to toggle visibility of another id'ed component.
 */
public interface Toggle extends Component
{
	/**
	 * Set the access key for the navigation to the character produced by this message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        One or more PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Toggle setAccessKey(String selector, PropertyReference... references);

	/**
	 * Set the descriptive text for the navigation.
	 * 
	 * @param message
	 *        The message.
	 * @return self.
	 */
	Toggle setDescription(Message message);

	/**
	 * Set the descriptive text for the navigation.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        One or more PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Toggle setDescription(String selector, PropertyReference... references);

	/**
	 * Set the decision to be disabled (inactive, but visible).
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to be disabled.
	 * @return self.
	 */
	Toggle setDisabled(Decision... decision);

	/**
	 * Set the icon for the navigation
	 * 
	 * @param url
	 *        The full URL to the icon.
	 * @param style
	 *        The icon style (left or right placement).
	 * @return self.
	 */
	Toggle setIcon(String url, Navigation.IconStyle style);

	/**
	 * Set the format style.
	 * 
	 * @param style
	 *        The format style.
	 * @return self.
	 */
	Toggle setStyle(Navigation.Style style);

	/**
	 * Set the id of the target component.
	 * 
	 * @param target
	 *        The id of the target component.
	 * @return self.
	 */
	Toggle setTarget(String target);

	/**
	 * Set the navigation title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Toggle setTitle(String selector, PropertyReference... references);
}
