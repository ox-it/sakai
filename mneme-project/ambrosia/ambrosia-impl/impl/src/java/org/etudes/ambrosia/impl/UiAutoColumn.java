/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2015 Etudes, Inc.
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

import org.etudes.ambrosia.api.AutoColumn;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * AutoColumn provides automatic numbering for columns in an entity list.
 */
public class UiAutoColumn extends UiEntityListColumn implements AutoColumn
{
	/** The auto values. */
	protected String[] autoValues = {"A.", "B.", "C.", "D.", "E.", "F.", "G.", "H.", "I.", "J.", "K.", "L.", "M.", "N.", "O.", "P.", "Q.", "R.",
			"S.", "T.", "U.", "V.", "W.", "X.", "Y.", "Z."};

	/** Optional reference to the model - if set, attempt to get an index value for the display from here. */
	protected PropertyReference propertyReference = null;

	/** Value of numeric */
	protected Boolean numeric = Boolean.FALSE;

	/**
	 * Public no-arg constructor.
	 */
	public UiAutoColumn()
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
	protected UiAutoColumn(UiServiceImpl service, Element xml)
	{
		// EntityListColumn stuff
		super(service, xml);

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
		}

		// model
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayText(Context context, Object entity, int row, String id, int size)
	{
		// TODO: watch out for overflow...

		// see if we have a model reference, use row if not
		int index = row;
		if (this.propertyReference != null)
		{
			String value = this.propertyReference.read(context, entity);

			if (value != null)
			{
				try
				{
					index = Integer.parseInt(value);
				}
				catch (NumberFormatException e)
				{

				}
			}
		}

		// guard against over and under flow
		if (index < 0) index = 0;
		if (index >= this.autoValues.length) index = this.autoValues.length - 1;

		if (this.numeric) return String.valueOf(index+1)+".";
		return this.autoValues[index];
	}

	/**
	 * {@inheritDoc}
	 */
	public AutoColumn setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public AutoColumn setNumeric()
	{
		this.numeric = Boolean.TRUE;
		return this;
}

}
