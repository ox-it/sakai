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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;

/**
 * Test HtmlHelper.
 */
public class HtmlHelperTest extends TestCase
{
	/** Logger. */
	private static final Log log = LogFactory.getLog(HtmlHelperTest.class);

	/**
	 * @param arg0
	 */
	public HtmlHelperTest(String arg0)
	{
		super(arg0);
	}

	public void testA() throws Exception
	{
		String source = "<p>test &lt;b&gt;</p>";
		String cleaned = HtmlHelper.clean(source);
		display(source, cleaned);
	}

	public void testClean() throws Exception
	{
		String source = "<p>some html</p>";
		String cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>some html";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "some html";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = null;
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>some html</p>      ";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "         <p>some html</p>      ";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<P>some html</P>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<P >some html</P >";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<P >some html<  /P >";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>some html</p></p></p></p>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>some html</p><p>more html</p>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<P>some html</P>and then some";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "leading text <P>some html</P> and then some";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "leading text     <P>some html</P>     and then some";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<P>some html</P>and then some<div>some in a div";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<P>some html</P>and then some<div>some in a div</p>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<P>some html</P>and then some<div>some in a div</div>and more";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>test &lt;b&gt;</p>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);
	}

	public void testCleanTiming() throws Exception
	{
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++)
		{
			String source = "<p>some html</p>";
			String cleaned = HtmlHelper.clean(source);
		}
		long elapsed = System.currentTimeMillis() - start;
		System.out.println("100 cleanings in " + elapsed + " (ms)");
	}

	public void testTarget() throws Exception
	{
		String source = "<p>some text <a href=\"some.url\" target=\"_blank\">the link</a></p>";
		String cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>some text <a href=\"some.url\" target=\"help\">the link</a></p>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>some text <a href=\"some.url\">the link</a></p>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<p>some text <a href=\"some.url\">the link</a><a href=\"some.url\">the link</a></p>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);

		source = "<a href=\"some.url\">the link</a>";
		cleaned = HtmlHelper.clean(source);
		display(source, cleaned);
	}

	protected boolean display(String source, String cleaned)
	{
		System.out.println("source:\n" + source + "\n\ncleaned:\n" + cleaned + "\n\n\n\n");
		return true;
	}

	/**
	 * @param arg0
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @param arg0
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
}
