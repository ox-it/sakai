/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/etudes/etudes-util/trunk/etudes-util/util/src/java/org/etudes/util/SqlHelper.java $
 * $Id: SqlHelper.java 66932 2010-03-29 18:22:42Z rashmi@etudes.org $
 ***********************************************************************************
 *
 * Copyright (c) 2010 Etudes, Inc.
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
import java.util.ArrayList;
import java.util.List;
/**
 * Contains helper methods that can be used to convert data and store them in the database
 */
public class SqlHelper
{
	/**
	 * Decode an encoded string array.
	 * 
	 * @param data
	 *        The encoded data.
	 * @return The decoded string array.
	 */
	public static String[] decodeStringArray(String data)
	{
		if (data == null) return new String[0];

		// collect the strings
		List<String> strings = new ArrayList<String>();
		int pos = 0;
		while (pos < data.length())
		{
			// find the next length, bound by a":"
			int bound = data.indexOf(":", pos);
			if (bound == -1) break;

			// take the pos..bound characters (skipping the last one
			String length = data.substring(pos, bound);
			int len = Integer.valueOf(length);

			// take the characters from after bound, len of them
			String s = (len > 0) ? data.substring(bound + 1, bound + 1 + len) : null;
			pos = bound + 1 + len;

			strings.add(s);
		}

		String[] rv = strings.toArray(new String[strings.size()]);
		return rv;
	}

	/**
	 * Encode a string array into a single string
	 * 
	 * @param data
	 *        The data to encode.
	 * @return The encoded data.
	 */
	public static String encodeStringArray(String[] data)
	{
		if ((data == null) || (data.length == 0)) return null;
		StringBuilder rv = new StringBuilder();
		for (String s : data)
		{
			System.out.println("s is "+s);
			rv.append((s == null) ? "0" : Integer.toString(s.length()));
			rv.append(":");
			if (s != null) rv.append(s);
		}

		return rv.toString();
	}

}
