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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.DecisionDelegate;
import org.etudes.ambrosia.api.FormatDelegate;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiDecision controls making an Entity selector based decision.
 */
public class UiDecision implements Decision
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(UiDecision.class);

	/** The delegate who, if defined, will make the decision. */
	protected DecisionDelegate delegate = null;

	/** The PropertyReference for this decision. */
	protected PropertyReference propertyReference = null;

	/** If true, the test is reveresed. */
	protected boolean reversed = false;

	/**
	 * No-arg constructor.
	 */
	public UiDecision()
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
	protected UiDecision(UiServiceImpl service, Element xml)
	{
		// reversed
		String reversed = StringUtil.trimToNull(xml.getAttribute("reversed"));
		if ((reversed != null) && ("TRUE".equals(reversed)))
		{
			setReversed();
		}

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
		}

		// the full model reference
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}

		String decisionDelegate = StringUtil.trimToNull(xml.getAttribute("delegate"));
		String tool = StringUtil.trimToNull(xml.getAttribute("tool"));
		if ((decisionDelegate != null) || (tool != null))
		{
			DecisionDelegate d = service.getDecisionDelegate(decisionDelegate, tool);
			if (d != null)
			{
				this.delegate = d;
			}
			else
			{
				M_log.warn("missing delegate: " + decisionDelegate + " tool: " + tool);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean decide(Context context, Object focus)
	{
		boolean decision = false;

		// delegte if setup to do so
		if (this.delegate != null)
		{
			decision = delegateDecision(context, focus);
		}
		else
		{
			// decide
			decision = makeDecision(context, focus);
		}

		// reverse if needed
		if (this.reversed) decision = !decision;

		return decision;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference getProperty()
	{
		return this.propertyReference;
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision setDelegate(DecisionDelegate delegate)
	{
		this.delegate = delegate;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision setReversed()
	{
		this.reversed = true;
		return this;
	}

	/**
	 * Delegate the decision.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The entity object focus.
	 * @return the decision.
	 */
	protected boolean delegateDecision(Context context, Object focus)
	{
		Object value = focus;

		// read the property as an object, use it as focus if defined and found
		if (this.propertyReference != null)
		{
			value = this.propertyReference.readObject(context, focus);
		}

		return delegate.decide(this, context, value);
	}

	/**
	 * Make the decision.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The entity object focus.
	 * @return the decision.
	 */
	protected boolean makeDecision(Context context, Object focus)
	{
		// read the property as a formatted string
		if (this.propertyReference != null)
		{
			String value = this.propertyReference.read(context, focus);
			if (value != null)
			{
				if (Boolean.parseBoolean(value))
				{
					return true;
				}

				// TODO: other interpretations of "true"?
			}
		}

		return false;
	}
}
