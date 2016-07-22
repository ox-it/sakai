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
 * UiMessage is a message from the message bundle that can have property reference parameters.
 */
public interface Message
{
	/**
	 * Add an alternate selector, used if this decision is true.
	 * 
	 * @param selector
	 *        The alternate selector.
	 * @param decision
	 *        The decision.
	 * @return
	 */
	Message addSelector(String selector, Decision decision);

	/**
	 * Format the message from the message selector and the array of property references.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The entity object focus.
	 * @return The formatted message.
	 */
	String getMessage(Context context, Object focus);

	/**
	 * Format the message from the message selector and the array of property references, and the extra args.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The entity object focus.
	 * @param extraArgs
	 *        additional arguments for the formatting.
	 * @return The formatted message.
	 */
	String getMessage(Context context, Object focus, Object[] extraArgs);

	/**
	 * Set the message selector and optional property references.
	 * 
	 * @param selector
	 * @param references
	 * @return self.
	 */
	Message setMessage(String selector, PropertyReference... references);

	/**
	 * Set the message template and optional property references.
	 * 
	 * @param selector
	 * @param references
	 * @return self.
	 */
	Message setTemplate(String template, PropertyReference... references);
}
