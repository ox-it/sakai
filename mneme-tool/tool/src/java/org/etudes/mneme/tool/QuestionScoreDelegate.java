/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.Submission;

/**
 * The "FormatQuestionDecoration" format delegate for the mneme tool.
 */
public class QuestionScoreDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(QuestionScoreDelegate.class);

	/**
	 * Format a score to 2 decimal places, trimming ".0" if present.
	 * 
	 * @param score
	 *        The score to format.
	 * @return The formatted score
	 */
	protected static String formatScore(Context context, Float score)
	{
		if (score == null) return context.getMessages().getString("question-score-ungraded");

		return FormatScoreDelegate.formatScore(score);
	}

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
		if (value == null) return null;
		if (!(value instanceof Question)) return value.toString();
		Question question = (Question) value;

		Submission submission = null;
		Object o = context.get("submission");
		if ((o != null) && (o instanceof Submission))
		{
			submission = (Submission) o;
		}

		Assessment assessment = null;
		if (submission != null)
		{
			assessment = submission.getAssessment();
		}
		else
		{
			o = context.get("assessment");
			if (o instanceof Assessment)
			{
				assessment = (Assessment) o;
			}
		}
		if (assessment == null) return value.toString();

		// use the {}/{} format if doing feedback, or just {} if not.
		StringBuffer rv = new StringBuffer();

		Boolean review = (Boolean) context.get("review");
		if (review == null) review = Boolean.FALSE;
		Boolean grading = (Boolean) context.get("grading");
		if (grading == null) grading = Boolean.FALSE;

		String selector = "worth-points";

		// if we are doing review just now, and if we are needing review and it's set, and if the submission has been graded
		if ((review || grading) && (submission != null) && (submission.getIsReleased() || grading))
		{
			// find the answer
			Answer answer = null;
			for (Answer a : submission.getAnswers())
			{
				if (a.getQuestion().equals(question))
				{
					answer = a;
					break;
				}
			}

			if (answer != null)
			{
				// if we are doing question score feedback
				if (answer.getShowCorrectReview() || grading)
				{
					// the auto-scores for this answered question
					Float score = null;

					if (grading)
					{
						score = answer.getAutoScore();
					}
					else
					{
						score = answer.getTotalScore();
					}

					rv.append(context.getMessages().getString("score") + ":&nbsp;" + formatScore(context, score) + "&nbsp;&nbsp;&nbsp;");

					selector = "of-points";
				}
			}
		}

		// add the possible points for the question
		Float score = question.getPoints();
		if (score.equals(Float.valueOf(1)))
		{
			selector += "-singular";
		}
		Object[] args = new Object[1];
		args[0] = formatScore(context, score);
		rv.append(context.getMessages().getFormattedMessage(selector, args));

		return rv.toString();
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
