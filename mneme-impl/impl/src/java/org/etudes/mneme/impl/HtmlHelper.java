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

package org.etudes.mneme.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.tidy.Tidy;

/**
 * HtmlHelper has some utility methods for working with user entered HTML.
 */
public class HtmlHelper
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(HtmlHelper.class);

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
			M_log.warn(e);
		}
		catch (JaxenException e)
		{
			M_log.warn(e);
		}

		return null;
	}
}
