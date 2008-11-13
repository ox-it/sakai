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
import org.etudes.ambrosia.api.Courier;
import org.etudes.ambrosia.api.Destination;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiCourier implements Courier.
 */
public class UiCourier extends UiComponent implements Courier
{
	/** The destination. */
	protected Destination destination = null;

	/** The frequency (seconds). */
	protected int frequency = 0;

	/**
	 * Public no-arg constructor.
	 */
	public UiCourier()
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
	protected UiCourier(UiServiceImpl service, Element xml)
	{
		super(service, xml);

		// frequency
		String frequency = StringUtil.trimToNull(xml.getAttribute("frequency"));
		if (frequency != null)
		{
			try
			{
				this.frequency = Integer.parseInt(frequency);
			}
			catch (NumberFormatException e)
			{
			}
		}

		// short form for destination - attribute "destination" as the destination
		String destination = StringUtil.trimToNull(xml.getAttribute("destination"));
		if (destination != null)
		{
			this.destination = service.newDestination().setDestination(destination);
		}

		Element settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
		if (settingsXml != null)
		{
			this.destination = new UiDestination(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// if not fully defined
		if ((this.destination == null) || (this.frequency < 1)) return false;

		// included?
		if (!isIncluded(context, focus)) return false;

		// here's the URL for the courier's GET
		String url = context.get("sakai.return.url") + this.destination.getDestination(context, focus);

		// TODO: works ONLY if we have ONLY one in the view... also depends on Sakai's headscript.js (might move to our own and let there be many)

		context.addScript("updateTime = " + this.frequency + "000;\n");
		context.addScript("updateUrl = \"" + url + "\"\n");
		context.addScript("scheduleUpdate();\n");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Courier setDestination(Destination destination)
	{
		this.destination = destination;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Courier setFrequency(int seconds)
	{
		this.frequency = seconds;
		return this;
	}
}
