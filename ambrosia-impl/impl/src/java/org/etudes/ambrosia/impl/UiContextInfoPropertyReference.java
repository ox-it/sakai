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

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.ContextInfoPropertyReference;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiContextInfoPropertyReference handles context id values by providing some context information for the display.
 */
public class UiContextInfoPropertyReference extends UiPropertyReference implements ContextInfoPropertyReference
{
	/** The context info we want. */
	protected Selector selector = Selector.title;

	/**
	 * No-arg constructor.
	 */
	public UiContextInfoPropertyReference()
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
	protected UiContextInfoPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// selector
		String selector = StringUtil.trimToNull(xml.getAttribute("selector"));
		if ("TITLE".equals(selector)) setSelector(Selector.title);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "contextInfo";
	}

	/**
	 * {@inheritDoc}
	 */
	public ContextInfoPropertyReference setSelector(Selector property)
	{
		this.selector = property;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (value == null) return super.format(context, value);
		if (!(value instanceof String)) return super.format(context, value);

		// TODO: assuming title for now...

		// context for now is site, so get the site title
		try
		{
			Site site = SiteService.getSite((String) value);
			return Validator.escapeHtml(site.getTitle());
		}
		catch (IdUnusedException e)
		{
			return Validator.escapeHtml((String) value);
		}
	}
}
