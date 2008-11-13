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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.DurationPropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiDurationPropertyReference implements DurationPropertyReference.
 */
public class UiDurationPropertyReference extends UiPropertyReference implements DurationPropertyReference
{
	/**
	 * No-arg constructor.
	 */
	public UiDurationPropertyReference()
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
	protected UiDurationPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "duration";
	}

	/**
	 * Format the long to at least two digits.
	 * 
	 * @param value
	 *        The long value.
	 * @return The long value formatted as a string of at least two digits.
	 */
	protected String fmtTwoDigit(Long value)
	{
		if (value.longValue() < 10) return "0" + value.toString();
		return value.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		// support h:mm

		long time = 0;
		if (value instanceof Integer)
		{
			time = ((Integer) value).longValue();
		}
		else if (value instanceof Long)
		{
			time = ((Long) value).longValue();
		}
		else if (value instanceof String)
		{
			time = Long.parseLong((String) value);
		}

		// convert to seconds
		time = time / 1000;

		// format the hours and minutes
		long hours = time / (60 * 60);
		long minutes = (time - (hours * (60 * 60))) / 60;
		long seconds = (time - (hours * (60 * 60)) - (minutes * 60));

		return hours + ":" + fmtTwoDigit(minutes);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String unFormat(String value)
	{
		value = StringUtil.trimToNull(value);
		if (value == null) return null;

		String[] parts = StringUtil.split(value, ":");
		if (parts.length == 2)
		{
			try
			{
				long duration = 0;
				// hours
				duration = Integer.parseInt(parts[0]) * 60l * 60l * 1000l;
				// minutes
				duration += Integer.parseInt(parts[1]) * 60l * 1000l;

				return Long.toString(duration);
			}
			catch (NumberFormatException e)
			{
			}
		}

		// if not as expected, complain
		throw new IllegalArgumentException();
	}
}
