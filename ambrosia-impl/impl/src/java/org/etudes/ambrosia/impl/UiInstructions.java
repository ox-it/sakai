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

import java.io.PrintWriter;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Instructions;
import org.w3c.dom.Element;

/**
 * UiInstructions implements Instructions.
 */
public class UiInstructions extends UiText implements Instructions
{
	/**
	 * Public no-arg constructor.
	 */
	public UiInstructions()
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
	protected UiInstructions(UiServiceImpl service, Element xml)
	{
		super(service, xml);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		String msg = null;
		String titleMsg = null;
		if (this.message != null)
		{
			msg = this.message.getMessage(context, focus);
		}
		if (this.titleMessage != null)
		{
			titleMsg = this.titleMessage.getMessage(context, focus);
		}

		if ((msg == null) && (titleMsg == null)) return false;

		PrintWriter response = context.getResponseWriter();

		// title
		if (titleMsg != null)
		{
			response.print("<div class=\"ambrosiaComponentTitle\">");
			response.print(titleMsg);
			response.println("</div>");
		}

		if (msg != null)
		{
			// we allow rich text / html
			response.println("<span class =\"ambrosiaInstructions\" >" + this.message.getMessage(context, focus) + "</span>");
		}

		return true;
	}
}
