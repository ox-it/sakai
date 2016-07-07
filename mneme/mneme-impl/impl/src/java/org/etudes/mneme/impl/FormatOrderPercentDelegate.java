/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/FormatOrderPercentDelegate.java $
 * $Id: FormatOrderPercentDelegate.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2015 Etudes, Inc.
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
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.etudes.mneme.impl.OrderQuestionImpl.OrderQuestionChoice;

/**
 * The "FormatOrderPercent" format delegate for the mneme tool.
 */
public class FormatOrderPercentDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatOrderPercentDelegate.class);

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
		
		o = context.get("choice");
		if (o == null) return null;
		if (!(o instanceof OrderQuestionChoice)) return null;
		
		String choiceId = ((OrderQuestionChoice)o).getId();
		String correctPos = ((OrderQuestionChoice)o).getCorrectPos();
		
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
					// does this answer's entry match by choice and position
					String[] answers = a.getTypeSpecificAnswer().getData();
					if (answers != null)
					{
						for (int i = 0; i < answers.length; i++)
						{
							String answerPos = answers[i++];
							String answerChoiceId = answers[i];
							if ((answerPos != null) && (answerPos.equals(correctPos)))
							{
								if ((answerChoiceId != null) && (answerChoiceId.equals(choiceId)))
								{
									count++;
								}
							}
						}
					}

				}
			}
		}

		if (total > 0)
		{
			// percent
			int pct = (count * 100) / total;

			Object[] args = new Object[1];
			args[0] = Integer.valueOf(pct);

			String template = "format-percent";
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
