/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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
import org.etudes.ambrosia.api.ImagePropertyReference;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.w3c.dom.Element;

/**
 * UiImagePropertyReference implements ImagePropertyReference
 */
public class UiImagePropertyReference extends UiPropertyReference implements ImagePropertyReference
{
	/** Caption for the image. */
	protected Message caption = null;

	/**
	 * No-arg constructor.
	 */
	public UiImagePropertyReference()
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
	protected UiImagePropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// caption
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "caption");
		if (settingsXml != null)
		{
			// let Message parse this
			this.caption = new UiMessage(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "image";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		// get the full URL from the ref
		String url = super.read(context, focus);

		StringBuilder rv = new StringBuilder();
		rv.append("<img src=\"" + url + "\" style=\"border-style: none;\" />");
		String caption = null;
		if (this.caption != null)
		{
			caption = this.caption.getMessage(context, focus);
		}
		if (caption != null)
		{
			rv.append("<div>" + caption + "</div>");
		}

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public ImagePropertyReference setCaption(String selector, PropertyReference... references)
	{
		this.caption = new UiMessage().setMessage(selector, references);
		return this;
	}
}
