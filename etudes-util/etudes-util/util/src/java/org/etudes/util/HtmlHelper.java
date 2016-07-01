/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Stripper has helper methods for stripping out junk from user entered HTML
 */
public class HtmlHelper
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(HtmlHelper.class);

	/**
	 * Assure that any anchor tag to external content that does not have a target attribute gets one as target="_blank".
	 * 
	 * @param data
	 *        The html data.
	 * @return The modified data.
	 */
	public static String assureAnchorTargetBlank(String data)
	{
		if (data == null)
		{
			return null;
		}

		StringBuffer sb = new StringBuffer();

		// find the <a> tags, and isolate the contents of a tag
		Pattern p = Pattern.compile("<a\\s+([^>]+)>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

		// find the href attribute and isolate the value
		Pattern hrefPattern = Pattern.compile("href\\s*=\\s*[\"\'](.*?)[\"\']", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

		// find the target attribute
		Pattern targetPattern = Pattern.compile("target\\s*=\\s*[\"\'][^\"\']*[\"\']\\s*");

		// find the onclick attribute
		Pattern onclickPattern = Pattern.compile("onclick\\s*=\\s*[\"\'][^\"\']*[\"\']\\s*");

		Matcher m = p.matcher(data);
		while (m.find())
		{
			if (m.groupCount() == 1)
			{
				String tagContents = m.group(1);

				// only if we do NOT have a target attribute at all
				Matcher targetMatcher = targetPattern.matcher(tagContents);
				if (!targetMatcher.find())
				{
					// only if we do not have an onclick (CKEditor puts this in for the "popup" target option in the link dialog
					Matcher onclickMatcher = onclickPattern.matcher(tagContents);
					if (!onclickMatcher.find())
					{
						// only if we do NOT have an internal href
						Matcher hrefMatcher = hrefPattern.matcher(tagContents);
						if (hrefMatcher.find() && hrefMatcher.groupCount() == 1)
						{
							String href = hrefMatcher.group(1);
							if (!href.startsWith("#"))
							{
								// we have an href that's not to a local anchor, so do the target stuff
								// Note: no need to remove existing target attributes, as we have checked that there is none
								// tagContents = "target=\"_blank\" " + tagContents.replaceAll("(target\\s*=\\s*[\"\'][^\"\']*[\"\']\\s*)?", "");
								tagContents = "target=\"_blank\" " + tagContents;
							}
						}
					}
				}

				tagContents = "<a " + tagContents + ">";
				m.appendReplacement(sb, Matcher.quoteReplacement(tagContents));
			}
		}

		m.appendTail(sb);

		return sb.toString();
	}

	/**
	 * Clean some user entered HTML fragment. Assures well formed HTML. Assures all anchor tags have target=_blank.
	 * 
	 * @param source
	 *        The source HTML
	 * @return The cleaned up HTML fragment.
	 */
	public static String clean(String source)
	{
		return clean(source, true);
	}

	/**
	 * Clean some user entered HTML.
	 * 
	 * @param source
	 *        The source HTML
	 * @param fragment
	 *        if true, return a fragment of html, else return a complete html document.
	 * @return The cleaned up HTML.
	 */
	public static String clean(String source, boolean fragment)
	{
		String rv = source;

		// get rid of the junk we have seen cause trouble
		// Note: we will assure the anchor targets, too, just like in cleanAndAssureAnchorTarget()
		rv = preClean(rv, fragment, true);

		// that's it folks!

		return rv;
	}

	/**
	 * Clean some user entered HTML - also assure a target=_blank for all anchors to external content.
	 * 
	 * @param source
	 *        The source HTML
	 * @param fragment
	 *        if true, return a fragment of html, else return a complete html document.
	 * @return The cleaned up HTML.
	 */
	public static String cleanAndAssureAnchorTarget(String source, boolean fragment)
	{
		String rv = source;

		// get rid of the junk we have seen cause trouble
		rv = preClean(rv, fragment, true);

		// that's it folks!

		return rv;
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

		// log that we are doing this
		M_log.warn("HtmlClean: stripBadEncodingCharacters");

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

		data = sb.toString();

		// if any open tags are left, likely because of missing a matching close tag, we will remove them.
		// if we leave them in, a missing close comment tag will be inserted by HtmlCleaner at the very END of the document, making the rest a big comment.
		// this fix exposes some comment text into the content, but preserves actual content.
		data = data.replaceAll("<!--", "");

		return data;
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

		// log that we are doing this
		M_log.warn("HtmlClean: stripDamagedComments");

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

		// log that we are doing this
		M_log.warn("HtmlClean: stripEncodedFontDefinitionComments");

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
	 * Remove any text that match the "comments from Word style definitions encoded into html by Tiny" from the data.
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripEncodedStyleDefinitionComments(String data)
	{
		if (data == null) return data;

		// quick check for any hint of the pattern
		if (data.indexOf("&lt;!-- /* Style Definitions */") == -1) return data;

		// log that we are doing this
		M_log.warn("HtmlClean: stripEncodedStyleDefinitionComments");

		// Notes: DOTALL so the "." matches line terminators too, "*?" Reluctant quantifier so text between two different comments is not lost
		Pattern p = Pattern.compile("&lt;!-- /\\* Style Definitions \\*/.*?--&gt;", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

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
	 * Remove form tags in content by changing them to div tags.  Also disable any input tags.
	 * 
	 * @param source
	 *        The source content.
	 * @return The converted content.
	 */
	public static String stripForms(String source)
	{
		source = source.replaceAll("<form", "<div");
		source = source.replaceAll("</form", "</div");
		source = source.replaceAll("<input","<input disabled");
		return source;
	}

	/**
	 * Remove any tags only valid in headers (title base meta link style)
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripHeaderTags(String data)
	{
		if (data == null) return data;

		// pattern to find link/meta tags
		Pattern p = Pattern.compile("<(link|meta|title|base|style)\\s+.*?(/*>)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

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
	 * Clean some user entered HTML - all the steps before the actual parsing.
	 * 
	 * @param source
	 *        The source HTML
	 * @param fragment
	 *        if true, return a fragment of html, else return a complete html document.
	 * @param assureAnchorTarget
	 *        if true, make sure any anchor to an external content has a target=_blank attribute.
	 * @return The cleaned up HTML.
	 */
	protected static String preClean(String source, boolean fragment, boolean assureAnchorTarget)
	{
		if (source == null) return null;

		// strip before cleaning for better tag structure (if something is removed by HtmlCleaner, it can split a content node into two)

		// deal with some specifics conditions
		source = stripEncodedFontDefinitionComments(source);
		source = stripEncodedStyleDefinitionComments(source);
		source = stripDamagedComments(source);
		source = stripBadEncodingCharacters(source);
		// source = stripBadImageTags(source); // TODO: not yet working

		// we don't need to keep comments
		source = stripComments(source);

		if (fragment)
		{
			// meta, link, title, style are not valid in body
			source = stripHeaderTags(source);
		}

		// shorten any full URL embedded references (such as what editors puts in for "smilies")
		source = XrefHelper.shortenFullUrls(source);

		if (assureAnchorTarget)
		{
			source = assureAnchorTargetBlank(source);
		}

		return source;
	}
}
