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

import org.etudes.ambrosia.api.BooleanPropertyReference;
import org.etudes.ambrosia.api.Context;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiBooleanPropertyReference implements BooleanPropertyReference.
 */
public class UiBooleanPropertyReference extends UiPropertyReference implements BooleanPropertyReference
{
	/** Message selector for false values. */
	protected String falseText = null;

	/** Message selector for true values. */
	protected String trueText = null;

	/**
	 * No-arg constructor.
	 */
	public UiBooleanPropertyReference()
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
	protected UiBooleanPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		String trueText = StringUtil.trimToNull(xml.getAttribute("true"));
		String falseText = StringUtil.trimToNull(xml.getAttribute("false"));
		setText(trueText, falseText);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "boolean";
	}

	/**
	 * {@inheritDoc}
	 */
	public BooleanPropertyReference setText(String trueText, String falseText)
	{
		this.trueText = trueText;
		this.falseText = falseText;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (value instanceof Boolean)
		{
			if (((Boolean) value).booleanValue())
			{
				// use the true text if set
				if (this.trueText != null)
				{
					return context.getMessages().getString(this.trueText);
				}
			}
			else
			{
				// use the false text if set
				if (this.falseText != null)
				{
					return context.getMessages().getString(this.falseText);
				}
			}

			// the text was not set, use the standard Boolean formatting
			return value.toString();
		}

		return super.format(context, value);
	}
}
