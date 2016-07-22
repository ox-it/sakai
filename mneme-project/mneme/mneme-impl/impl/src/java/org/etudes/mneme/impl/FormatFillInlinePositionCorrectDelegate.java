/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/FormatFillInlinePositionCorrectDelegate.java $
 * $Id: FormatFillInlinePositionCorrectDelegate.java 3635 2012-12-02 21:26:23Z ggolden $
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificQuestion;

/**
 * The "FormatFillInlinePositionCorrect" format delegate for the mneme tool.
 */
public class FormatFillInlinePositionCorrectDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatFillInlinePositionCorrectDelegate.class);

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
		if (!(tsq instanceof FillInlineQuestionImpl)) return value.toString();

		FillInlineQuestionImpl plugin = (FillInlineQuestionImpl) tsq;

		List<String> corrects = plugin.getCorrectAnswers();

		// "position" is the 1 based fill-in position
		o = context.get("position");
		if (!(o instanceof Integer)) return value.toString();
		Integer position = (Integer) o;
		int pos = position - 1;

		Boolean correct = null;
		if (question.getHasCorrect() && (corrects != null))
		{

			correct = Boolean.FALSE;
			for (String aCorrect : corrects)
			{
				boolean thisCorrect = FillInlineAnswerImpl.answerCorrect(target, aCorrect, new ArrayList<String>());
				if (thisCorrect)
				{
					correct = Boolean.TRUE;
					break;
				}
			}

		}

		if (correct != null)
		{
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
