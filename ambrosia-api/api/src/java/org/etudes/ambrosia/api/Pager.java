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
 * Pager presents paging controls.
 */
public interface Pager extends Component
{
	/**
	 * Add a size option to the size dropdown.
	 * 
	 * @param option
	 *        The size option.
	 * @return self.
	 */
	Pager addSizeOption(Integer option);

	/**
	 * Set the tool destination to use when clicked.
	 * 
	 * @param destination
	 *        The tool destination.
	 * @return self.
	 */
	Pager setDestination(Destination destination);

	/**
	 * Set the "first page" icon
	 * 
	 * @param url
	 *        The full URL to the icon
	 * @param selector
	 *        The message selector (for the alt text for the icon).
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Pager setFirstIcon(String url, String selector, PropertyReference... references);

	/**
	 * Set the "last page" icon
	 * 
	 * @param url
	 *        The full URL to the icon
	 * @param selector
	 *        The message selector (for the alt text for the icon).
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Pager setLastIcon(String url, String selector, PropertyReference... references);

	/**
	 * Set the "viewing..." message
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        The message model references.
	 * @return self.
	 */
	Pager setMessage(String selector, PropertyReference... references);

	/**
	 * Set the "next page" icon
	 * 
	 * @param url
	 *        The full URL to the icon
	 * @param selector
	 *        The message selector (for the alt text for the icon).
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Pager setNextIcon(String url, String selector, PropertyReference... references);

	/**
	 * Set the property reference for the page size options.
	 * 
	 * @param propertyReference
	 *        The property reference for the page size options.
	 */
	Pager setPageSizeProperty(PropertyReference propertyReference);

	/**
	 * Set the message used for page size options.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        The message model references.
	 * @return self.
	 */
	Pager setPageSizesMessage(String selector, PropertyReference... references);

	/**
	 * Set the property reference for the paging object.
	 * 
	 * @param propertyReference
	 *        The property reference for the paging object.
	 */
	Pager setPagingProperty(PropertyReference propertyReference);

	/**
	 * Set the "prev page" icon
	 * 
	 * @param url
	 *        The full URL to the icon
	 * @param selector
	 *        The message selector (for the alt text for the icon).
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Pager setPrevIcon(String url, String selector, PropertyReference... references);

	/**
	 * Indicate that the navigation needs to submit the form.
	 * 
	 * @return self.
	 */
	Pager setSubmit();
}
