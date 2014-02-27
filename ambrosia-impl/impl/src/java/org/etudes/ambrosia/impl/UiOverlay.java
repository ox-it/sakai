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

import java.io.PrintWriter;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Overlay;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiSection implements Section.
 */
public class UiOverlay extends UiContainer implements Overlay
{
	/** Height. */
	protected String height = "250px";

	/** Width. */
	protected String width = "620px";

	/**
	 * Public no-arg constructor.
	 */
	public UiOverlay()
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
	protected UiOverlay(UiServiceImpl service, Element xml)
	{
		// do the container thing
		super(service, xml);

		// width and height
		String width = StringUtil.trimToNull(xml.getAttribute("width"));
		if (width != null) setWidth(width);

		String height = StringUtil.trimToNull(xml.getAttribute("height"));
		if (height != null) setHeight(height);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		PrintWriter response = context.getResponseWriter();

		// setup the container
		response.println("<div id=\"" + getId(context) + "\" class=\"ambrosiaOverlay\" style=\"display:none\">");

		// render the contents
		super.render(context, focus);

		// end the container
		response.println("</div>");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Overlay setHeight(String height)
	{
		this.height = height;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Overlay setWidth(String width)
	{
		this.width = width;
		return this;
	}
}
