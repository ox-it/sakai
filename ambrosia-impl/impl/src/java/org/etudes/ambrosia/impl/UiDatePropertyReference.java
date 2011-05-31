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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.DatePropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiDatePropertyReference implements DatePropertyReference.
 */
public class UiDatePropertyReference extends UiPropertyReference implements DatePropertyReference
{
	/** If set, split the date to date on top, time below. */
	protected boolean multiLine = false;

	/** If set, use the SHORT instad of MEDIUM format. */
	protected boolean shortFormat = false;

	/**
	 * No-arg constructor.
	 */
	public UiDatePropertyReference()
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
	protected UiDatePropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// two line
		String twoLine = StringUtil.trimToNull(xml.getAttribute("twoLine"));
		if ((twoLine != null) && (twoLine.equals("TRUE"))) setTwoLine();

		String fmt = StringUtil.trimToNull(xml.getAttribute("format"));
		if ((fmt != null) && (fmt.equals("SHORT"))) setShort();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "date";
	}

	/**
	 * {@inheritDoc}
	 */
	public DatePropertyReference setShort()
	{
		this.shortFormat = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DatePropertyReference setTwoLine()
	{
		this.multiLine = true;
		return this;
	}

	/**
	 * Add in the seconds part expected by the MEDUIM date format (":00").
	 * 
	 * @param display
	 *        The MEDIUM formatted date w/o seconds.
	 * @return The MEDIUM formatted date with the (0) seconds added.
	 */
	protected String addSeconds(String display)
	{
		int i = display.lastIndexOf(" ");
		if (i == -1) return display;

		String rv = display.substring(0, i) + ":00" + display.substring(i);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (value instanceof Date)
		{
			Date date = (Date) value;
			// TODO: use the end-user's locale and time zone prefs

			// pick format
			int fmt = DateFormat.MEDIUM;
			if (shortFormat) fmt = DateFormat.SHORT;

			if (multiLine)
			{
				DateFormat dateFormat = DateFormat.getDateInstance(fmt, Locale.US);
				DateFormat timeFormat = DateFormat.getTimeInstance(fmt, Locale.US);

				return "<span style=\"white-space: nowrap;\">" + dateFormat.format(date) + "</span><br /><span style=\"white-space: nowrap;\">"
						+ removeSeconds(timeFormat.format(date)) + "</span>";
			}
			else
			{
				DateFormat format = DateFormat.getDateTimeInstance(fmt, fmt, Locale.US);

				return removeSeconds(format.format(date));
			}
		}

		return super.format(context, value);
	}

	/**
	 * Remove the ":xx" seconds part of a MEDIUM date format display.
	 * 
	 * @param display
	 *        The MEDIUM formatted date.
	 * @return The MEDIUM formatted date with the seconds removed.
	 */
	protected String removeSeconds(String display)
	{
		int i = display.lastIndexOf(":");
		if ((i == -1) || ((i + 3) >= display.length())) return display;

		String rv = display.substring(0, i) + display.substring(i + 3);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String unFormat(String value)
	{
		value = StringUtil.trimToNull(value);
		if (value == null) return null;

		// assume single line, both date and time

		// pick format
		int fmt = DateFormat.MEDIUM;
		if (shortFormat) fmt = DateFormat.SHORT;

		// TODO: use the end-user's locale and time zone prefs
		DateFormat format = DateFormat.getDateTimeInstance(fmt, fmt, Locale.US);
		try
		{
			Date date = format.parse(addSeconds(value));

			// sanity check year - must be between 1000 and 9999 (matched mysql DATE and DATETIME types validation)
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int year = cal.get(Calendar.YEAR);
			if ((year < 1000) || (year > 9999)) throw new IllegalArgumentException();

			return Long.toString(date.getTime());
		}
		catch (ParseException e)
		{
			// if not as expected, complain
			throw new IllegalArgumentException();
		}
	}
}
