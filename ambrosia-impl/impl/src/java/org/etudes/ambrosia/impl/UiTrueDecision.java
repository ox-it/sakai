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

package org.etudes.ambrosia.impl;

import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.TrueDecision;
import org.w3c.dom.Element;

/**
 * UiTrueDecision implements TrueDecision.
 */
public class UiTrueDecision extends UiDecision implements TrueDecision
{
	/**
	 * No-arg constructor.
	 */
	public UiTrueDecision()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiTrueDecision(UiServiceImpl service, Element xml)
	{
		// do the Decision stuff
		super(service, xml);
	}

	/**
	 * Make the decision.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The entity object focus.
	 * @return the decision.
	 */
	protected boolean makeDecision(Context context, Object focus)
	{
		return true;
	}
}
