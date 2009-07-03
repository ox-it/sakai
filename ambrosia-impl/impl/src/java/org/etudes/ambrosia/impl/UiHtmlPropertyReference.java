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
import org.etudes.ambrosia.api.HtmlPropertyReference;
import org.etudes.util.XrefHelper;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiHtmlPropertyReference implements HtmlPropertyReference
 */
public class UiHtmlPropertyReference extends UiPropertyReference implements HtmlPropertyReference
{
	/** This blank line appears from tiny editor in IE 7. */
	final static String htmlEditorBlankDoc = "<html />";

	final static String htmlEditorBlankLine = "<p>&nbsp;</p>";

	/** Set if we are going to strip surrounding paragraph marks from the value. */
	protected boolean stripP = false;

	/**
	 * No-arg constructor.
	 */
	public UiHtmlPropertyReference()
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
	protected UiHtmlPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// stripP
		String stripP = StringUtil.trimToNull(xml.getAttribute("stripP"));
		if ((stripP != null) && ("TRUE".equals(stripP))) setStripP();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "html";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		String value = super.read(context, focus);
		if (value == null) return null;

		// if missing, don't do special treatment
		if (value.equals(missingValue(context))) return value;

		// strip surrounding <p>
		if (this.stripP)
		{
			int start = 0;
			if (value.startsWith("<p>")) start += 3;

			int end = value.length();
			if (value.endsWith("</p>")) end -= 4;

			value = value.substring(start, end);
		}

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlPropertyReference setStripP()
	{
		this.stripP = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String unFormat(String value)
	{
		if (value != null)
		{
			// if there is just the htmlEditorBlankLine, remove it (TinyMCE puts this in for a totally blank edit)
			if (value.equals(htmlEditorBlankLine) || value.equals(htmlEditorBlankDoc))
			{
				value = "";
			}
			
			// shorten any full URL embedded references (such as what Tiny puts in for "emotions")
			else
			{
				value = XrefHelper.shortenFullUrls(value);
			}
			
		}

		return value;
	}
}
