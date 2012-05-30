/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2012 Etudes, Inc.
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

package org.etudes.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.cover.PreferencesService;

/**
 * Utility class to help with formatting and parsing dates considering a user's preferred time zone.
 */
public class DateHelper
{
	/**
	 * Format a date for display to this user, in the user's preferred time zone.
	 * 
	 * @param date
	 *        The date.
	 * @param userId
	 *        The user id - leave as null to use the current session user.
	 * @return The formatted date.
	 */
	public static String formatDate(Date date, String userId)
	{
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, getPreferredLocale(userId));
		format.setTimeZone(getPreferredTimeZone(userId));
		String rv = format.format(date);
		return rv;
	}

	/**
	 * Access this user's preferred time zone. If not set to a recognized value, or there is no user passed in or currently set, use GMT.
	 * 
	 * @param userId
	 *        The user id - leave as null to use the current session user.
	 * @return The TimeZone for this user's preferences, or GMT if we can't get one.
	 */
	public static Locale getPreferredLocale(String userId)
	{
		Locale rv = null;

		if (userId == null) userId = SessionManager.getCurrentSessionUserId();
		if (userId != null)
		{

			Preferences prefs = PreferencesService.getPreferences(userId);
			ResourceProperties localeProps = prefs.getProperties("sakai:resourceloader");
			String localeString = localeProps.getProperty("locale");
			if (localeString != null)
			{
				String[] locValues = localeString.split("_");
				if (locValues.length > 1)
				{
					// language, country
					rv = new Locale(locValues[0], locValues[1]);
				}
				else if (locValues.length == 1)
				{
					// just language
					rv = new Locale(locValues[0]); // just language
				}
			}
		}
		if (rv == null)
		{
			rv = Locale.getDefault();
		}
		return rv;

		// TODO: do we need any of this?
		// if (!Locale.getDefault().getLanguage().equals("en") && loc.getLanguage().equals("en"))
		// {
		// // Tweak for English: en is default locale. It has no suffix in filename.
		// loc = new Locale("");
		// }

		// TODO: this is another way to refer to the US locale
		// Locale.US
	}

	/**
	 * Access this user's preferred time zone. If not set to a recognized value, or there is no user passed in or currently set, use GMT.
	 * 
	 * @param userId
	 *        The user id - leave as null to use the current session user.
	 * @return The TimeZone for this user's preferences, or GMT if we can't get one.
	 */
	public static TimeZone getPreferredTimeZone(String userId)
	{
		TimeZone rv = null;

		if (userId == null) userId = SessionManager.getCurrentSessionUserId();
		if (userId != null)
		{
			Preferences prefs = PreferencesService.getPreferences(userId);
			ResourceProperties tzProps = prefs.getProperties(TimeService.APPLICATION_ID);
			String timeZoneId = tzProps.getProperty(TimeService.TIMEZONE_KEY);

			// defaults to GMT is the zone id is not recognized
			rv = TimeZone.getTimeZone(timeZoneId);
		}

		// if no user, default to GMT
		if (rv == null)
		{
			TimeZone.getTimeZone("GMT");
		}

		return rv;
	}

	/**
	 * Parse a string in standard input format, in the user's preferred time zone, into a Date.
	 * 
	 * @param dateString
	 *        The input string
	 * @param userId
	 *        The user id - leave as null to use the current session user.
	 * @return The Date.
	 * @throws ParseException
	 *         if the date is not in the proper format.
	 */
	public static Date parseDate(String dateString, String userId) throws ParseException
	{
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, getPreferredLocale(userId));
		format.setTimeZone(getPreferredTimeZone(userId));
		Date rv = format.parse(dateString);
		return rv;
	}
}
