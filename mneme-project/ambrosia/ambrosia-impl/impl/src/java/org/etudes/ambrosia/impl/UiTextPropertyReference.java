/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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
import org.etudes.ambrosia.api.TextPropertyReference;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiTextPropertyReference implements TextPropertyReference
 */
public class UiTextPropertyReference extends UiPropertyReference implements TextPropertyReference
{
	protected int maxChars = -1;

	protected boolean stripHtml = false;

	/**
	 * No-arg constructor.
	 */
	public UiTextPropertyReference()
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
	protected UiTextPropertyReference(UiServiceImpl service, Element xml)
	{
		// property reference stuff
		super(service, xml);

		// max length
		String max = StringUtil.trimToNull(xml.getAttribute("max"));
		if (max != null)
		{
			try
			{
				setMaxLength(Integer.parseInt(max));
			}
			catch (NumberFormatException e)
			{
			}
		}

		// strip html
		String strip = StringUtil.trimToNull(xml.getAttribute("stripHtml"));
		if ((strip != null) && ("TRUE".equals(strip))) setStripHtml();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "text";
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

		// strip the html if desired
		if (this.stripHtml)
		{
			value = FormattedText.convertFormattedTextToPlaintext(value);

			// also remove any remaining new lines
			value = value.replace("\n", " ");
		}

		// truncate if desired and needed
		if (this.maxChars > -1)
		{
			if (value.length() > this.maxChars)
			{
				value = value.substring(0, this.maxChars) + "...";
			}
		}

		value = FormattedText.decodeNumericCharacterReferences(value);
		return Validator.escapeHtml(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public TextPropertyReference setMaxLength(int maxChars)
	{
		this.maxChars = maxChars;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public TextPropertyReference setStripHtml()
	{
		this.stripHtml = true;
		return this;
	}
}
