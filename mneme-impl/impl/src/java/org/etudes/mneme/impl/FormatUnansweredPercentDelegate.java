/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.Submission;

/**
 * The "FormatPercentDelegate" format delegate for the mneme tool.
 */
public class FormatUnansweredPercentDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatUnansweredPercentDelegate.class);

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
		// ignore value

		// "submissions" is the List<Submission> of submissions
		Object o = context.get("submissions");
		if (o == null) return null;
		if (!(o instanceof List)) return null;
		List<Submission> submissions = (List<Submission>) o;

		// "question" is the Question
		o = context.get("question");
		if (o == null) return null;
		if (!(o instanceof Question)) return null;
		Question question = (Question) o;

		int count = 0;
		int total = 0;
		for (Submission s : submissions)
		{
			if (s.getIsPhantom()) continue;
			if (!s.getIsComplete()) continue;

			Answer a = s.getAnswer(question);
			if (a != null)
			{
				total++;

				if (!a.getIsAnswered())
				{
					count++;
				}
			}
		}

		if (total > 0)
		{
			if (count == 0)
			{
				return context.getMessages().getString("format-unanswered-percent-none");
			}

			// percent
			int pct = (count * 100) / total;

			Object[] args = new Object[2];
			args[0] = Integer.valueOf(pct);
			args[1] = Integer.valueOf(count);

			String template = "format-unanswered-percent";
			return context.getMessages().getFormattedMessage(template, args);
		}

		return context.getMessages().getString("format-percent-none");
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
