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

import java.util.Collection;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.HasValueDecision;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiHasValueDecision is a decision that is true if the property has a value - not null, and if string or Collection, not empty.
 */
public class UiHasValueDecision extends UiDecision implements HasValueDecision
{
	/**
	 * No-arg constructor.
	 */
	public UiHasValueDecision()
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
	protected UiHasValueDecision(UiServiceImpl service, Element xml)
	{
		// do the Decision stuff
		super(service, xml);
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean makeDecision(Context context, Object focus)
	{
		// read the property as a formatted string
		if (this.propertyReference != null)
		{
			Object value = this.propertyReference.readObject(context, focus);
			if (value != null)
			{
				if (value instanceof String)
				{
					String s = (String) value;
					if (s.length() == 0)
					{
						return false;
					}
				}

				else if (value instanceof Collection)
				{
					Collection c = (Collection) value;
					if (c.isEmpty())
					{
						return false;
					}
				}

				return true;
			}
		}

		return false;
	}
}
