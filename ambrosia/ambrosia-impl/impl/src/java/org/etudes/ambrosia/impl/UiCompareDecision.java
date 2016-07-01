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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.etudes.ambrosia.api.CompareDecision;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiCompareDecision is a UiDecision that compares the value to some other value.
 */
public class UiCompareDecision extends UiDecision implements CompareDecision
{
	/** The PropertyReference for the comparison. */
	protected PropertyReference compareReference = null;

	/** If we don't have a compare property, use this value. */
	protected String[] compareReferenceValue = null;

	/**
	 * No-arg constructor.
	 */
	public UiCompareDecision()
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
	protected UiCompareDecision(UiServiceImpl service, Element xml)
	{
		// do the Decision stuff
		super(service, xml);

		List<String> values = new ArrayList<String>();

		// shortcut for a single constant
		String value = StringUtil.trimToNull(xml.getAttribute("constant"));
		if (value != null)
		{
			values.add(value);
		}

		// constants
		NodeList settings = xml.getChildNodes();
		for (int i = 0; i < settings.getLength(); i++)
		{
			Node node = settings.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element innerXml = (Element) node;
				if ("constant".equals(innerXml.getTagName()))
				{
					value = StringUtil.trimToNull(innerXml.getAttribute("value"));
					if (value != null)
					{
						values.add(value);
					}
				}
			}
		}

		// convert the refs into an array
		if (!values.isEmpty())
		{
			this.compareReferenceValue = values.toArray(new String[values.size()]);
		}

		// short for compare model
		String model = StringUtil.trimToNull(xml.getAttribute("compare"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setEqualsProperty(pRef);
		}

		// the full compare model reference
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "compare");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(xml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setEqualsProperty(pRef);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompareDecision setEqualsConstant(String... value)
	{
		this.compareReferenceValue = value;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompareDecision setEqualsProperty(PropertyReference propertyReference)
	{
		this.compareReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean makeDecision(Context context, Object focus)
	{
		// read the property as a formatted string
		if (this.propertyReference != null)
		{
			String value = this.propertyReference.read(context, focus);
			if (value != null)
			{
				// check against the reference, if set
				if (this.compareReference != null)
				{
					Object compareObject = this.compareReference.readObject(context, focus);
					if (compareObject != null)
					{
						// if value is a member of the collection
						if (compareObject instanceof Collection)
						{
							Collection compare = (Collection) compareObject;
							return compare.contains(value);
						}

						// if value is in an array
						else if (compareObject.getClass().isArray())
						{
							Object[] compare = (Object[]) compareObject;
							for (Object o : compare)
							{
								if (value.equalsIgnoreCase(o.toString()))
								{
									return true;
								}
							}
						}

						// compare to the string value of whatever we found
						else
						{
							String compare = compareObject.toString();
							return value.equalsIgnoreCase(compare);
						}
					}
				}

				// or against the constant, if set
				else if (this.compareReferenceValue != null)
				{
					return StringUtil.contains(this.compareReferenceValue, value);
				}
			}
		}

		return false;
	}
}
