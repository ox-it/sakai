/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 20010 Etudes, Inc.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stripper has helper methods for stripping out junk from user entered HTML
 */
public class HtmlHelper
{
	/** Our log. */
	// private static Log M_log = LogFactory.getLog(HtmlHelper.class);

	/**
	 * Remove any characters from the data that will cause mysql to reject the record because of encoding errors<br />
	 * (java.sql.SQLException: Incorrect string value) if they are present.
	 * 
	 * @param data
	 *        The html data.
	 * @return The data with the bad characters replaced with spaces.
	 */
	public static String stripBadEncodingCharacters(String data)
	{
		// Note: these characters become two characters in the String - the first is 56256 0xDBC0 or 55304 0xD808 and the second varies, but is 56xxx

		if (data == null) return data;

		// quick check for any strange characters
		if ((data.indexOf(56256) == -1) && (data.indexOf(55304) == -1)) return data;

		StringBuilder buf = new StringBuilder(data);
		int len = buf.length() - 1;
		for (int i = 0; i < len; i++)
		{
			char c = buf.charAt(i);
			if ((c == 56256) || (c == 55304))
			{
				buf.setCharAt(i, ' ');
				i++;
				buf.setCharAt(i, ' ');
			}
		}

		return buf.toString();
	}

	/**
	 * Remove any HTML comments from the data.
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripComments(String data)
	{
		if (data == null) return data;

		// quick check for any comments
		if (data.indexOf("<!--") == -1) return data;

		// pattern to find html comments
		// Notes: DOTALL so the "." matches line terminators too, "*?" Reluctant quantifier so text between two different comments is not lost
		Pattern p = Pattern.compile("<!--.*?-->", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

		Matcher m = p.matcher(data);
		StringBuffer sb = new StringBuffer();

		while (m.find())
		{
			m.appendReplacement(sb, "");
		}

		m.appendTail(sb);

		return sb.toString();
	}
}
