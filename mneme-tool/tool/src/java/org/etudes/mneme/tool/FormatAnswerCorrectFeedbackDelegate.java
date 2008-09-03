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

package org.etudes.mneme.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;

/**
 * The "FormatAnswerCorrectFeedback" format delegate for the mneme tool.
 */
public class FormatAnswerCorrectFeedbackDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatAnswerCorrectFeedbackDelegate.class);

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	// TODO: revisit how this is used - it seems question type specific

	/**
	 * {@inheritDoc}
	 */
	public String format(Context context, Object focus)
	{
		// if (focus == null) return null;
		// if (!(focus instanceof Answer)) return null;
		// Answer answer = (Answer) focus;
		//
		// // the question this is an answer to
		// Question question = answer.getQuestion();
		// if (question == null) return null;
		//
		// Object o = context.get("submission");
		// if (!(o instanceof Submission)) return null;
		// Submission submission = (Submission) o;
		//
		// Assessment assessment = submission.getAssessment();
		// if (assessment == null) return null;
		//
		// // if we are doing review just now
		// if (assessment.getReview().getNowAvailable())
		// {
		// // if we are doing currect answer review
		// if (assessment.getReview().getShowCorrectAnswer())
		// {
		// // search for our answer without creating it, and if found check if it is this QuestionAnswer
		// for (Answer subAnswer : submission.getAnswers())
		// {
		// // is this submission answer the answer to our assessment question answer's question?
		// if (subAnswer.getQuestion().equals(question))
		// {
		// // not for survey
		// if (question.getType() != QuestionType.survey)
		// {
		// // is the submission answer this answer?
		// if (StringUtil.contains(subAnswer.getEntryAnswerIds(), answer.getId()))
		// {
		// // correct
		// if ((answer.getIsCorrect() != null) && answer.getIsCorrect().booleanValue())
		// {
		// return "<img src=\"" + context.get("sakai.return.url") + "/icons/correct.png\" alt=\""
		// + context.getMessages().getString("correct") + "\" />";
		// }
		//
		// // incorrect
		// else
		// {
		// return "<img src=\"" + context.get("sakai.return.url") + "/icons/wrong.png\" alt=\""
		// + context.getMessages().getString("incorrect") + "\" />";
		// }
		// }
		// }
		// }
		// }
		// }
		// }

		return null;
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
