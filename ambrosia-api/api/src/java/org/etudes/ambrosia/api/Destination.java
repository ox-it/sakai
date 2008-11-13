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
 * UiDestination forms a tool destination, from a template, with possible embedded fill-in-the-blanks, and property references to fill them in.<br />
 * The format is the same as for international messages, i.e. text {0} more text {1} etc
 */
public interface Destination
{
	/**
	 * Fill in the blanks in the template, if any, with the property references, and return the destination string
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The entity object focus.
	 * @return The filled out destination string.
	 */
	String getDestination(Context context, Object focus);

	/**
	 * Set the destination.
	 * 
	 * @param template
	 *        The template string.
	 * @param references
	 *        The optional references.
	 */
	Destination setDestination(String template, PropertyReference... references);
}
