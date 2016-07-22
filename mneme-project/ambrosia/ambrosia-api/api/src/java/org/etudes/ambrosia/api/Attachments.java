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
 * Attachments presents a clickable attachment list to the user.
 */
public interface Attachments extends Component
{
	/**
	 * Add a navigation to include in each attachment display line
	 * 
	 * @param navigation
	 *        The navigation to add.
	 * @return self.
	 */
	Attachments addNavigation(Navigation navigation);

	/**
	 * Set the reference to the Collection of attachments to list.
	 * 
	 * @param attachments
	 *        The reference to the Collection of entities to list.
	 * @param name
	 *        The context name for the currently iterated attachment.
	 * @return self.
	 */
	Attachments setAttachments(PropertyReference attachments, String name);

	/**
	 * Set the icon for the navigation
	 * 
	 * @param url
	 *        The full URL to the icon.
	 * @return self.
	 */
	Attachments setIcon(String icon);

	/**
	 * Set the display to include only the raw reference strings as defined, with no checking to see if they are valid.
	 * 
	 * @param setting
	 *        If true, set raw, else don't.
	 * @return self.
	 */
	Attachments setRaw(boolean setting);

	/**
	 * Set the display to the size
	 * 
	 * @param setting
	 *        If true, include the size, else don't.
	 * @return self.
	 */
	Attachments setSize(boolean setting);

	/**
	 * Set the display to the timestamp.
	 * 
	 * @param setting
	 *        If true, include the timestamp, else don't.
	 * @return self.
	 */
	Attachments setTimestamp(boolean setting);

	/**
	 * Set the title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Attachments setTitle(String selector, PropertyReference... references);
}
