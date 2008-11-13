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
 * UiDecision controls making an Entity selector based decision.
 */
public interface DecisionDelegate
{
	/**
	 * Make the decision, based on a boolean interpretation of the selector of this entity object.
	 * 
	 * @param decision
	 *        The Decision object we are working for.
	 * @param context
	 *        The UiContext.
	 * @param entity
	 *        The entity to get the selector value from.
	 * @return True if the entity has the selector and it evaluates to a boolean TRUE value, false if not.
	 */
	boolean decide(Decision decision, Context context, Object focus);
}
