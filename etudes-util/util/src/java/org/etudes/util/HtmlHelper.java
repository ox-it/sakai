/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011 Etudes, Inc.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.tidy.Tidy;

/**
 * Stripper has helper methods for stripping out junk from user entered HTML
 */
public class HtmlHelper
{
	/** Our log. */
	// private static Log M_log = LogFactory.getLog(HtmlHelper.class);

	/**
	 * Compare two HTML fragment strings and see if they are essentially the same or not.
	 * 
	 * @param oneHtml
	 *        One HTML fragment string.
	 * @param otherHtml
	 *        The other HTML fragment string.
	 * @return true if these are essentially the same, false if they are essentially different.
	 */
	public static boolean compareHtml(String oneHtml, String otherHtml)
	{
		//inner-tag formatting and attribute order
		oneHtml = normalizeTagsofHtml(oneHtml);
		otherHtml = normalizeTagsofHtml(otherHtml);
		
		//TODO: - script tag body text formatting
		return !Different.different(oneHtml, otherHtml);
	}

	/**
	 * Normalizes the text. Finds all of the html tag in the text and for each tag parses its attributes and writes back in sorted order.
	 * 
	 * @param content1
	 *        HTML Content
	 * @return normalize HTML Content
	 */
	public static String normalizeTagsofHtml(String content1)
	{
		if (content1 == null || content1.length() == 0) return content1;
		// to get all html tags
		Pattern p1 = Pattern.compile("<.*?>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
		// to get tag name
		Pattern p_tagName = Pattern.compile("(<.+?[\\s]+)(.*?[^#]*)(/*>)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
		// to get the list of attributes
		Pattern p2 = Pattern.compile("[\\s]*(.*?)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		// to parse css style
		Pattern p_css = Pattern.compile("(.*?[^#]*)(:)(.*?[^#]*)(;)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

		Matcher m = p1.matcher(content1);
		StringBuffer sb = new StringBuffer();

		while (m.find())
		{
			Matcher m_tag = p_tagName.matcher(m.group(0));

			while (m_tag.find())
			{
				// write the tag name
				String tagsContent = new String();
				tagsContent = tagsContent.concat(m_tag.group(1).trim() + " ");

				// find attributes of tag
				Matcher m2 = p2.matcher(m_tag.group(2));
				HashMap<String, String> attribs = new HashMap<String, String>(0);
				while (m2.find())
				{
					attribs.put(m2.group(1), m2.group(2));
				}

				// write the attributes in sorted format
				TreeMap<String, String> sort_attribs = new TreeMap<String, String>(attribs);
				Set<String> sort_keys = sort_attribs.keySet();
				for (String k : sort_keys)
				{
					// TODO:parse value to fix space formatting on css styles but not disturbing src,href values
					// ex: mce_style="font-size: 10pt; font-family: Verdana;"
					String value = attribs.get(k);
					tagsContent = tagsContent.concat(" " + k.trim() + "=\"" + value + "\" ");
				}
				tagsContent = tagsContent.concat(m_tag.group(3).trim());
				m.appendReplacement(sb, tagsContent);
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
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

	/**
	 * Remove any text that match the "comments damaged from IE and Tiny" from the data.
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripDamagedComments(String data)
	{
		if (data == null) return data;

		// quick check for any hint of the pattern
		if (data.indexOf("<! [endif] >") == -1) return data;

		// Notes: DOTALL so the "." matches line terminators too, "*?" Reluctant quantifier so text between two different comments is not lost
		Pattern p = Pattern.compile("<!--\\[if.*?<! \\[endif\\] >", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

		Matcher m = p.matcher(data);
		StringBuffer sb = new StringBuffer();

		while (m.find())
		{
			m.appendReplacement(sb, "");
		}

		m.appendTail(sb);

		// now remove the bad comment end
		String rv = sb.toString().replace("<-->", "");
		return rv;
	}

	/**
	 * Remove any text that match the "comments from Word font definitions encoded into html by Tiny" from the data.
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripEncodedFontDefinitionComments(String data)
	{
		if (data == null) return data;

		// quick check for any hint of the pattern
		if (data.indexOf("&lt;!--  /* Font Definitions */") == -1) return data;

		// Notes: DOTALL so the "." matches line terminators too, "*?" Reluctant quantifier so text between two different comments is not lost
		Pattern p = Pattern.compile("&lt;!--  /\\* Font Definitions \\*/.*?--&gt;", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

		Matcher m = p.matcher(data);
		StringBuffer sb = new StringBuffer();

		while (m.find())
		{
			m.appendReplacement(sb, "");
		}

		m.appendTail(sb);

		return sb.toString();
	}

	/**
	 * Remove any Link tags from the data.
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripLinks(String data)
	{
		if (data == null) return data;

		// quick check for any link or meta tags
		if (data.indexOf("<link ") == -1 && data.indexOf("<meta ") == -1) return data;

		// pattern to find link/meta tags
		Pattern p = Pattern.compile("<(link|meta)\\s+.*?(/*>)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

		Matcher m = p.matcher(data);
		StringBuffer sb = new StringBuffer();

		while (m.find())
		{
			m.appendReplacement(sb, "");
		}

		m.appendTail(sb);

		return sb.toString();
	}
	
	/**
	 * Clean some user entered HTML. Assures well formed XML. Assures all anchor tags have target=_blank.
	 * 
	 * @param source
	 *        The source HTML
	 * @return The cleaned up HTML.
	 */
	public static String clean(String source)
	{
		if (source == null) return null;

		try
		{
			// parse possibly dirty html
			Tidy tidy = new Tidy();
			ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes("UTF-8"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(baos);
			tidy.setErrout(pw);
			tidy.setQuiet(true);
			tidy.setXHTML(true);
			Document doc = tidy.parseDOM(bais, null);

			// assure target=_blank in all anchors
			XPath x = new DOMXPath("//a");
			List l = x.selectNodes(doc);
			for (Object o : l)
			{
				Element e = (Element) o;
				e.setAttribute("target", "_blank");
			}

			// get the whole thing in a string
			baos = new ByteArrayOutputStream();
			tidy.pprint(doc, baos);
			String all = baos.toString("UTF-8");
			String rv = null;

			// find the substring between <body> and </body>
			int start = all.indexOf("<body>");
			if (start != -1)
			{
				start += "<body>".length();
				int end = all.lastIndexOf("</body>");
				rv = all.substring(start, end);
			}

			return rv;
		}
		catch (IOException e)
		{
			//M_log.warn(e);
		}
		catch (JaxenException e)
		{
			//M_log.warn(e);
		}

		return null;
	}
}
