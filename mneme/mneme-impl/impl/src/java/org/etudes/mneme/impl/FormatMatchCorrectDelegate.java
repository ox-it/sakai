/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.etudes.mneme.impl.MatchQuestionImpl.MatchQuestionPair;

/**
 * The "FormatMatchCorrect" format delegate for the mneme tool.
 */
public class FormatMatchCorrectDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatMatchCorrectDelegate.class);

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

		if (question.getHasCorrect())
		{
			TypeSpecificQuestion tsq = question.getTypeSpecificQuestion();
			if (!(tsq instanceof MatchQuestionImpl)) return null;

			MatchQuestionImpl plugin = (MatchQuestionImpl) tsq;
			List<MatchQuestionPair> pairs = plugin.getPairs();

			boolean correct = false;
			for (MatchQuestionPair pair : pairs)
			{
				if (pair.getId().equals(matchId))
				{
					if (pair.getCorrectChoiceId().equals(choiceId))
					{
						correct = true;
						break;
					}
				}
			}

			if (correct)
			{
				return "<img src=\"" + context.get("sakai.return.url") + "/icons/correct.png\" style=\"border-style: none;\" alt=\""
						+ context.getMessages().getString("correct") + "\" title=\"" + context.getMessages().getString("correct") + "\"/>";
			}
			else
			{
				return "<img src=\"" + context.get("sakai.return.url") + "/icons/incorrect.png\" style=\"border-style: none;\" alt=\""
						+ context.getMessages().getString("incorrect") + "\" title=\"" + context.getMessages().getString("incorrect") + "\"/>";
			}
		}
		else
		{
			return "<div style=\"float:left;width:16px\">&nbsp;</div>";
		}
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
