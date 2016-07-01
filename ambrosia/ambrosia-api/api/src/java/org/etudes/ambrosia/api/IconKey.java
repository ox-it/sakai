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
 * IconKey presents a key to icon use for some other part of the interface using icons. Each icon is shown with a description.
 */
public interface IconKey extends Component
{
	/**
	 * Add one more icon to the key
	 * 
	 * @param url
	 *        The full URL to the icon
	 * @param description
	 *        The icon description Message
	 * @return self.
	 */
	IconKey addIcon(String url, Message description);

	/**
	 * Set the reference to the description display in the key object.
	 * 
	 * @param descriptionReference
	 *        The reference to the description display in the key object.
	 */
	IconKey setDescriptionReference(PropertyReference descriptionReference);

	/**
	 * Set the reference to the icon display in the key object.
	 * 
	 * @param iconReference
	 *        The reference to the icon display in the key object.
	 */
	IconKey setIconReference(PropertyReference iconReference);

	/**
	 * Set the reference to the Collection of keys to list.
	 * 
	 * @param keysReference
	 *        The reference to the Collection of keys to list.
	 */
	IconKey setKeysReference(PropertyReference keysReference);

	/**
	 * Set the key title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param properties
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	IconKey setTitle(String selector, PropertyReference... properties);
}
