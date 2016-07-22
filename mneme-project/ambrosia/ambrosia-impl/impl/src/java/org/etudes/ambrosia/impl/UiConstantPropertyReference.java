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

import org.etudes.ambrosia.api.ConstantPropertyReference;
import org.etudes.ambrosia.api.Context;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiConstantPropertyReference implements ConstantPropertyReference
 */
public class UiConstantPropertyReference extends UiPropertyReference implements ConstantPropertyReference
{
	protected String value = null;

	/**
	 * No-arg constructor.
	 */
	public UiConstantPropertyReference()
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
	protected UiConstantPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// value
		String value = StringUtil.trimToNull(xml.getAttribute("value"));
		setValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "constant";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readObject(Context context, Object focus)
	{
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConstantPropertyReference setValue(String value)
	{
		this.value = value;
		return this;
	}
}
