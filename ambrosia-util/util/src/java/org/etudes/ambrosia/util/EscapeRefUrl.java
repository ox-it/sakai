/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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

package org.etudes.ambrosia.util;

/**
 * 
 */
public class EscapeRefUrl
{
	public static String escapeRefUrl(String refStr, String fullUrl)
	{
		int pos = fullUrl.indexOf(refStr);
		String link = null;
		if (pos == -1)
		{
			link = fullUrl;
		}
		else
		{
			String start = fullUrl.substring(0, pos);
			String encoded = escapeUrl(refStr);
			link = start + encoded;
		}

		return link;
	}

	/**
	 * Return a string based on id that is fully escaped using URL rules, using a UTF-8 underlying encoding.
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped using URL rules.
	 */
	public static String escapeUrl(String id)
	{
		if (id == null) return "";
		id = id.trim();
		try
		{
			// convert the string to bytes in UTF-8
			byte[] bytes = id.getBytes("UTF-8");

			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < bytes.length; i++)
			{
				byte b = bytes[i];

				// convert to 'unsigned byte' in an int
				// see: http://www.darksleep.com/player/JavaAndUnsignedTypes.html
				int bi = 0xFF & (int) bytes[i];

				if (("$&+,:;=?@ '\"<>#%{}|\\^~[]`^?;".indexOf((char) b) != -1) || (bi <= 0x1F) || (bi == 0x7F) || (bi >= 0x80))
				{
					buf.append("%");
					buf.append(Integer.toString(bi, 16));
				}
				else
				{
					buf.append((char) b);
				}
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			return id;
		}
	}
}
