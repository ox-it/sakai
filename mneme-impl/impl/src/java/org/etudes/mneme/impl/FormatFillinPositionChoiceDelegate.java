/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1145/mneme-impl/impl/src/java/org/etudes/mneme/impl/FormatFillinPositionChoiceDelegate.java $
 * $Id: FormatFillinPositionChoiceDelegate.java 3635 2012-12-02 21:26:23Z ggolden $
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
 * The "FormatFillinPositionChoice" format delegate for the mneme tool.
 */
public class FormatFillinPositionChoiceDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatFillinPositionChoiceDelegate.class);

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

		// "question" is the Question
		Object o = context.get("question");
		if (o == null) return value.toString();
		if (!(o instanceof Question)) return value.toString();
		Question question = (Question) o;

		TypeSpecificQuestion tsq = question.getTypeSpecificQuestion();
		if (!(tsq instanceof FillBlanksQuestionImpl)) return value.toString();
		FillBlanksQuestionImpl plugin = (FillBlanksQuestionImpl) tsq;
		boolean caseSensitive = Boolean.valueOf(plugin.getCaseSensitive());

		// "submissions" is the Submissions list
		o = context.get("submission");
		if (o == null) return null;
		if (!(o instanceof Submission)) return value.toString();
		Submission submission = (Submission) o;

		// "position" is the 1 based fill-in position
		o = context.get("position");
		if (o == null) return value.toString();
		if (!(o instanceof Integer)) return value.toString();
		Integer position = (Integer) o;
		int pos = position - 1;

		Answer a = submission.getAnswer(question);
		if (a != null)
		{
			if (a.getIsAnswered())
			{
				// does the answer's value match our target answer?
				// Note: assume that the answer for this position is the nth data element
				String[] answers = a.getTypeSpecificAnswer().getData();
				if ((answers != null) && (answers.length > pos) && (answers[pos] != null))
				{
					if (caseSensitive)
					{
						if (answers[pos].equals(target))
						{
							Object[] args = new Object[1];
							args[0] = context.getUrl("!/ambrosia_library/icons/your-choice.png");

							String template = "format-image";
							return context.getMessages().getFormattedMessage(template, args);
						}
					}
					else
					{
						if (answers[pos].equalsIgnoreCase(target))
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
