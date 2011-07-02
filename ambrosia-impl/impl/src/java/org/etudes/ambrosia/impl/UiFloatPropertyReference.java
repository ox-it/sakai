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

package org.etudes.ambrosia.impl;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.FloatPropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiFloatPropertyReference implements FloatPropertyReference.
 */
public class UiFloatPropertyReference extends UiPropertyReference implements FloatPropertyReference
{
	// protected boolean concise = true;

	protected boolean decimal2 = false;

	/**
	 * No-arg constructor.
	 */
	public UiFloatPropertyReference()
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
	protected UiFloatPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// 2 decimal formatting
		String d2 = StringUtil.trimToNull(xml.getAttribute("decimal2"));
		if ((d2 != null) && ("TRUE".equals(d2))) set2decimal();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "float";
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatPropertyReference set2decimal()
	{
		this.decimal2 = true;
		return this;
	}

	/**
	 * Format the value found into a display string.
	 * 
	 * @param context
	 *        The Context.
	 * @param value
	 *        The value.
	 * @return The value formatted into a display string.
	 */
	protected String format(Context context, Object value)
	{
		if ((this.decimal2) && (value != null) && (value instanceof Float))
		{
			Float v = (Float) value;

			// round to two places
			String rv = Float.toString(Math.round(v.floatValue() * 100.0f) / 100.0f);

			// get rid of ".00"
			if (rv.endsWith(".00"))
			{
				rv = rv.substring(0, rv.length() - 3);
			}

			// get rid of ".0"
			if (rv.endsWith(".0"))
			{
				rv = rv.substring(0, rv.length() - 2);
			}

			return rv;
		}

		else
		{
			return super.format(context, value);
		}
	}
}
