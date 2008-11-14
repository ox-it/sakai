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
 * The "FormatCount" format delegate for the mneme tool.
 */
public class FormatCountDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatCountDelegate.class);

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
		// value is the answer text
		if (value == null) return null;
		if (!(value instanceof String)) return value.toString();
		String target = (String) value;

		// "submissions" is the List<Submission> of submissions
		Object o = context.get("submissions");
		if (o == null) return value.toString();
		if (!(o instanceof List)) return value.toString();
		List<Submission> submissions = (List<Submission>) o;

		// "question" is the Question
		o = context.get("question");
		if (o == null) return value.toString();
		if (!(o instanceof Question)) return value.toString();
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

				if (a.getIsAnswered())
				{
					// does the answer's value match our target answer?
					// Note: assume that the answer is one of the getData() strings
					String[] answers = a.getTypeSpecificAnswer().getData();
					if ((answers != null) && (answers.length > 0))
					{
						for (int i = 0; i < answers.length; i++)
						{
							if (answers[i].equals(target))
							{
								count++;
							}
						}
					}
				}
			}
		}

		if (total > 0)
		{
			Object[] args = new Object[1];
			args[0] = Integer.valueOf(count);

			String template = "format-count";
			return context.getMessages().getFormattedMessage(template, args);
		}

		String template = "format-percent-none";
		return context.getMessages().getString(template);
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
