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
 * UiInterface is the top most container for each user interface.<br />
 * The interface title is rendered as the main (think window or frame) title.<br />
 * The interface has a header that is rendered in the display at the top.
 */
public interface Interface extends Container
{
	/**
	 * Add a component to the footer
	 * 
	 * @param c
	 *        The component to add to the footer.
	 * @return self.
	 */
	Interface addFooter(Component c);

	/**
	 * Add a component to the header
	 * 
	 * @param c
	 *        The component to add to the header.
	 * @return self.
	 */
	Interface addHeader(Component c);

	/**
	 * Add a component to the sub-header
	 * 
	 * @param c
	 *        The component to add to the sub-header.
	 * @return self.
	 */
	Interface addSubHeader(Component c);

	/**
	 * Set the Destination for the attachment picker UI for this view.
	 * 
	 * @param destination
	 *        The Destination for the attachment picker UI for this view.
	 * @return self
	 */
	Interface setAttachmentPickerDestination(Destination destination);

	/**
	 * Set the user interface footer message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Interface setFooter(String selector, PropertyReference... references);

	/**
	 * Set the user interface header message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Interface setHeader(String selector, PropertyReference... references);

	/**
	 * Set the view's mode bar
	 * 
	 * @param bar
	 *        The mode bar.
	 * @return self.
	 */
	Interface setModeBar(Component bar);

	/**
	 * Set to disable browser auto-complete for the interface.
	 * 
	 * @return self.
	 */
	Interface setNoAutoComplete();

	/**
	 * Set as a popup interface.
	 * 
	 * @return self.
	 */
	Interface setPopup();

	/**
	 * Set the user interface sub-header message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Interface setSubHeader(String selector, PropertyReference... references);

	/**
	 * Set the user interface title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Interface setTitle(String selector, PropertyReference... references);
}
