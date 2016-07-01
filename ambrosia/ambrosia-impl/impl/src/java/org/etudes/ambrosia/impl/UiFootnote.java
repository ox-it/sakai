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
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Footnote;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.w3c.dom.Element;

/**
 * UiFootnote describes a footnote...
 */
public class UiFootnote implements Footnote
{
	/** The criteria decision for marking a specific selector of an entity with the footnote. */
	protected Decision criteria = null;

	/** The include decision. */
	protected Decision included = null;

	/** The message for the footnote text. */
	protected Message text = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiFootnote()
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
	protected UiFootnote(UiServiceImpl service, Element xml)
	{
		// TODO: !
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean apply(Context context, Object focus)
	{
		if (!included(context)) return false;

		if ((this.criteria != null) && (!this.criteria.decide(context, focus))) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getText()
	{
		return this.text;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean included(Context context)
	{
		// included?
		if ((this.included != null) && (!this.included.decide(context, null))) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Footnote setCriteria(Decision criteria)
	{
		this.criteria = criteria;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Footnote setIncluded(Decision decision)
	{
		this.included = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Footnote setText(String selector, PropertyReference... references)
	{
		this.text = new UiMessage().setMessage(selector, references);
		return this;
	}
}
