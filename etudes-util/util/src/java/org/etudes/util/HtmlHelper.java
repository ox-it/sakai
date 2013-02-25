/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013 Etudes, Inc.
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
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.HtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import org.htmlcleaner.XPatherException;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

/**
 * Stripper has helper methods for stripping out junk from user entered HTML
 */
public class HtmlHelper
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(HtmlHelper.class);

	/**
	 * Make the elements in alpha order, no internal spacing, all ; terminated
	 * 
	 * @param value
	 * @return
	 */
	public static String canonicalStyleAttribute(String value)
	{
		// break on ";"
		String[] parts = StringUtil.split(value, ";");

		// collect style and value elements for alpha rendering
		TreeMap<String, String> elements = new TreeMap<String, String>();
		for (String part : parts)
		{
			// break on first ":"
			String[] lr = StringUtil.split(part, ":");

			if (lr.length == 2)
			{
				elements.put(lr[0].trim(), lr[1].trim());
			}
		}

		// reform the value
		StringBuilder newValue = new StringBuilder();
		for (String key : elements.keySet())
		{
			String setting = elements.get(key);
			newValue.append(key);
			newValue.append(":");
			newValue.append(setting);
			newValue.append(";");
		}

		return newValue.toString();
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
	 * Clean some user entered HTML. Assures well formed HTML. Assures all anchor tags have target=_blank. Assures all tag attributes are in determined (alpha) order.
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
		rv = preClean(rv, fragment);

		// run through jtidy, for the best in making it valid (fragment is ignored)
		rv = cleanWithJtidy(rv, fragment);

		// run through htmlcleaner, where we can more easily manipulate the tree before rendering
		rv = cleanWithHtmlCleaner(rv, fragment);

		return rv;
	}

	/**
	 * Compare two HTML fragment strings and see if they are essentially the same or not.
	 * 
	 * @param oneHtml
	 *        One HTML string.
	 * @param otherHtml
	 *        The other HTML string.
	 * @param fragment
	 *        true if the strings are html fragments, false if full html documents.
	 * @return true if these are essentially the same, false if they are essentially different.
	 */
	public static boolean compareHtml(String oneHtml, String otherHtml, boolean fragment)
	{
		// compare the clean versions of both
		String cleanOne = clean(oneHtml, fragment);
		String cleanTwo = clean(otherHtml, fragment);

		// TODO: - script tag body text formatting
		return !Different.different(cleanOne, cleanTwo);
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
	 * Remove image tags that have for src "file://" "webkit-fake-url://" or "x-apple-ql-id://" prefixes (transports)
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripBadImageTags(String data)
	{
		if (data == null) return data;

		// pattern to find link/meta tags
		// TODO: the .*? needs to stop on a >, else if there's a good image and later a bad one, it combines the two into one and removes it all!
		Pattern p = Pattern.compile("<img\\s+.*?src=\"(file:|webkit-fake-url:|x-apple-ql-id:).*?/>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
				| Pattern.DOTALL);

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
	 * Remove link and meta tags
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	public static String stripLinks(String data)
	{
		if (data == null) return data;

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
	 * Clean some user entered HTML. Assures well formed HTML. Assures all anchor tags have target=_blank. Assures all tag attributes are in determined (alpha) order.
	 * 
	 * @param source
	 *        The source HTML
	 * @param fragment
	 *        if true, return a fragment of html, else return a complete html document.
	 * @return The cleaned up HTML.
	 */
	protected static String cleanWithHtmlCleaner(String source, boolean fragment)
	{
		if (source == null) return null;

		// http://htmlcleaner.sourceforge.net
		// http://mvnrepository.com/artifact/net.sourceforge.htmlcleaner/htmlcleaner/2.2
		HtmlCleaner cleaner = new HtmlCleaner();
		cleaner.getProperties().setOmitXmlDeclaration(true);
		cleaner.getProperties().setTranslateSpecialEntities(false);
		cleaner.getProperties().setOmitComments(true);

		TagNode node = cleaner.clean(source);

		node.traverse(new TagNodeVisitor()
		{
			public boolean visit(TagNode tagNode, HtmlNode htmlNode)
			{
				if (htmlNode instanceof TagNode)
				{
					TagNode tag = (TagNode) htmlNode;
					String tagName = tag.getName();
					if ("a".equals(tagName))
					{
						// if there is a href attribute, and it does not begin with # (a link to an anchor), add a target attribute
						String href = tag.getAttributeByName("href");
						if ((href != null) && (!href.startsWith("#")))
						{
							tag.setAttribute("target", "_blank");
						}
					}

					// order the tag attributes
					Map<String, String> attributes = tag.getAttributes();
					TreeMap<String, String> attr = new TreeMap<String, String>();
					attr.putAll(attributes);

					for (String key : attr.keySet())
					{
						tag.removeAttribute(key);
					}

					for (String key : attr.keySet())
					{
						String value = attr.get(key);

						// fix up style attributes
						if ("style".equalsIgnoreCase(key))
						{
							value = canonicalStyleAttribute(value);
						}

						tag.setAttribute(key, value);
					}
				}

				return true;
			}
		});

		String rv = "";
		final HtmlSerializer serializer = new CompactHtmlSerializer(cleaner.getProperties());

		try
		{
			TagNode nodeToGet = node;
			boolean omitEnvelope = false;

			if (fragment)
			{
				Object[] nodes = node.evaluateXPath("//body");
				if (nodes.length == 1)
				{
					nodeToGet = (TagNode) (nodes[0]);
					omitEnvelope = true;
				}
			}

			rv = serializer.getAsString(nodeToGet, "UTF-8", omitEnvelope);
		}
		catch (XPatherException e)
		{
		}
		catch (IOException e)
		{
		}

		return rv;
	}

	/**
	 * Clean some user entered HTML. Assures well formed HTML. Assures all anchor tags have target=_blank. Assures all tag attributes are in determined (alpha) order.
	 * 
	 * @param source
	 *        The source HTML
	 * @param fragment
	 *        if true, return a fragment of html, else return a complete html document.
	 * @return The cleaned up HTML.
	 */
	protected static String cleanWithJtidy(String source, boolean fragment)
	{
		if (source == null) return null;

		try
		{
			Tidy tidy = new Tidy();
			ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes("UTF-8"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(baos);
			tidy.setErrout(pw);

			tidy.setDocType("loose");
			tidy.setDropEmptyParas(false);
			tidy.setEscapeCdata(false);
			tidy.setForceOutput(true);
			tidy.setInputEncoding("UTF-8");
			tidy.setMakeClean(false);		// this is what changes <font> and <center> to style tags, but not without trouble for old content, so we disable it.
			tidy.setQuiet(true);
			tidy.setSmartIndent(false);
			tidy.setSpaces(0);
			tidy.setTabsize(0);
			tidy.setTidyMark(false);
			tidy.setWraplen(Integer.MAX_VALUE);
			tidy.setXHTML(true);
			tidy.setXmlOut(false);

			// StringWriter writer = new StringWriter();
			// tidy.getConfiguration().printConfigOptions(writer, true);
			// System.out.println(writer.toString());

			Document doc = tidy.parseDOM(bais, null);

			// get the whole thing in a string
			baos = new ByteArrayOutputStream();
			tidy.pprint(doc, baos);
			String rv = baos.toString("UTF-8");

			// System.out.println("JTidy output:\n" + rv);

			return rv;
		}
		catch (Throwable e)
		{
			M_log.warn("cleanWithJtidy: " + e.toString() + "\n source:\n" + source);
			return source;
		}
	}

	/**
	 * Convert any actual tab characters to the html entity .
	 * 
	 * @param data
	 *        the html data.
	 * @return The cleaned up data.
	 */
	protected static String encodeTabs(String data)
	{
		if (data == null) return data;

		// quick check for any hint of the pattern
		if (data.indexOf("\t") == -1) return data;

		String rv = data.replaceAll("\\t", "&#9;");

		return rv;
	}

	/**
	 * Clean some user entered HTML - all the steps before the actual parsing.
	 * 
	 * @param source
	 *        The source HTML
	 * @param fragment
	 *        if true, return a fragment of html, else return a complete html document.
	 * @return The cleaned up HTML.
	 */
	protected static String preClean(String source, boolean fragment)
	{
		if (source == null) return null;

		// strip before cleaning for better tag structure (if something is removed by HtmlCleaner, it can split a content node into two)

		// deal with some specifics conditions
		source = stripEncodedFontDefinitionComments(source);
		source = stripEncodedStyleDefinitionComments(source);
		source = stripDamagedComments(source);
		source = stripBadEncodingCharacters(source);
		source = encodeTabs(source);
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

		return source;
	}
}
