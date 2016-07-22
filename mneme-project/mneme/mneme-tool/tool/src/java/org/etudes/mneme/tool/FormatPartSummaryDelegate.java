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

package org.etudes.mneme.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.QuestionPick;

/**
 * The "FormatPartSummary" format delegate for the mneme tool.
 */
public class FormatPartSummaryDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatPartSummaryDelegate.class);

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public String format(Context context, Object value)
	{
		// value is a part
		if (value == null) return null;
		if (!(value instanceof Part)) return value.toString();
		Part part = (Part) value;

		int draws = 0;
		int picks = 0;
		for (PartDetail detail : part.getDetails())
		{
			if (detail instanceof PoolDraw)
			{
				draws++;
			}
			else if (detail instanceof QuestionPick)
			{
				picks++;
			}
		}

		// deal with single / plural!
		String template = "fmt-part-summary";
		template += (part.getNumQuestions().intValue() == 1) ? "s" : "p";
		template += (draws == 1) ? "s" : "p";
		template += (picks == 1) ? "s" : "p";

		Object[] args = new Object[3];
		args[0] = part.getNumQuestions().toString();
		args[1] = Integer.valueOf(draws);
		args[2] = Integer.valueOf(picks);
		return context.getMessages().getFormattedMessage(template, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object formatObject(Context context, Object value)
	{
		return value;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}
}
