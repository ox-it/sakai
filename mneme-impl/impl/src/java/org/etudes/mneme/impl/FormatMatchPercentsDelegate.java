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
import org.etudes.mneme.api.TypeSpecificQuestion;

/**
 * The "FormatMatchPercents" format delegate for the mneme tool.
 */
public class FormatMatchPercentsDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatMatchPercentsDelegate.class);

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

		// "question" is the Question
		Object o = context.get("question");
		if (o == null) return null;
		if (!(o instanceof Question)) return null;
		Question question = (Question) o;

		TypeSpecificQuestion tsq = question.getTypeSpecificQuestion();
		if (!(tsq instanceof MatchQuestionImpl)) return null;
		MatchQuestionImpl plugin = (MatchQuestionImpl) tsq;

		// "submissions" is the Submissions list
		o = context.get("submissions");
		if (o == null) return null;
		if (!(o instanceof List)) return null;
		List<Submission> submissions = (List<Submission>) o;

		// "match" is the match id
		o = context.get("match");
		if (o == null) return null;
		if (!(o instanceof String)) return null;
		String matchId = (String) o;

		// "choice" is the choice id
		o = context.get("choice");
		if (o == null) return null;
		if (!(o instanceof String)) return null;
		String choiceId = (String) o;

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
					// does this answer's entry for this match id = the choice id?
					// Note: assume that the answer data is match id, choice id, etc...
					String[] answers = a.getTypeSpecificAnswer().getData();
					if (answers != null)
					{
						for (int i = 0; i < answers.length; i++)
						{
							String answerMatchId = answers[i++];
							String answerChoiceId = answers[i];
							if ((answerMatchId != null) && (answerMatchId.equals(matchId)))
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
		return value.toString();
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
