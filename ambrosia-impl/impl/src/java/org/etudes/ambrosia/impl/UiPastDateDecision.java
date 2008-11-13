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

import java.util.Date;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.PastDateDecision;
import org.w3c.dom.Element;

/**
 * UiPastDateDecision implements PastDateDecision.
 */
public class UiPastDateDecision extends UiDecision implements PastDateDecision
{
	// TODO: add a date against which to check rather than now -ggolden

	/**
	 * No-arg constructor.
	 */
	public UiPastDateDecision()
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
	protected UiPastDateDecision(UiServiceImpl service, Element xml)
	{
		// do the Decision stuff
		super(service, xml);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean decide(Context context, Object focus)
	{
		if (this.propertyReference == null) return false;

		// get the value as an object, not formatted
		Object value = this.propertyReference.readObject(context, focus);
		if (value == null) return false;

		// we want a Date
		if (!(value instanceof Date)) return false;
		Date date = (Date) value;

		// if before now
		return date.before(new Date());
	}
}
