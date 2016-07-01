/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1145/mneme-impl/impl/src/java/org/etudes/mneme/impl/FormatMatchUserChoiceDelegate.java $
 * $Id: FormatMatchUserChoiceDelegate.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2013 Etudes, Inc.
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

/**
 * The "FormatMatchUserChoice" format delegate for the mneme tool.
 */
public class FormatMatchUserChoiceDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatMatchUserChoiceDelegate.class);

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
		o = context.get("submission");
		if (o == null) return null;
		if (!(o instanceof Submission)) return null;
		Submission submission = (Submission) o;

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

		Answer a = submission.getAnswer(question);
		if (a != null)
		{
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
								Object[] args = new Object[1];
								args[0] = context.getUrl("/icons/your-choice.png");

								String template = "format-image";
								return context.getMessages().getFormattedMessage(template, args);
							}
						}
					}
				}
			}
		}

		String template = "format-image-none";
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
