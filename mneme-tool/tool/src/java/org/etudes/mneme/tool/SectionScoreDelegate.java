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

package org.etudes.mneme.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Submission;

/**
 * The "SectionScoreDelegate" format delegate for the mneme tool.
 */
public class SectionScoreDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(SectionScoreDelegate.class);

	/**
	 * Format a score to 2 decimal places, trimming ".0" if present.
	 * 
	 * @param score
	 *        The score to format.
	 * @return The formatted score
	 */
	protected static String formatScore(Float score)
	{
		if (score == null) return "-";

		// round to a single place
		String rv = Float.toString(Math.round(score * 100.0f) / 100.0f);

		// get rid of ".00"
		if (rv.endsWith(".00"))
		{
			rv = rv.substring(0, rv.length() - 3);
		}

		// get rid of ".0"
		if (rv.endsWith(".0"))
		{
			rv = rv.substring(0, rv.length() - 2);
		}

		return rv;
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
		if (!(value instanceof Part)) return value.toString();
		Part part = (Part) value;

		Object o = context.get("submission");
		if (!(o instanceof Submission)) return value.toString();
		Submission submission = (Submission) o;

		Assessment assessment = submission.getAssessment();
		if (assessment == null) return value.toString();

		// use the {}/{} format if doing feedback, or just {} if not.
		StringBuffer rv = new StringBuffer();

		Boolean review = (Boolean) context.get("review");

		String selector = "worth-points";

		// if we are doing review and the submission has been graded
		if ((review != null) && review && submission.getIsReleased())
		{
			// add the sum of scores for any answered question in this section
			float score = 0;

			// find the section's answers to AssessmentQuestions that are in this section.
			boolean partial = false;
			for (Answer answer : submission.getAnswers())
			{
				if (answer.getQuestion().getPart().equals(part))
				{
					Float answerScore = answer.getTotalScore();
					if (answerScore != null)
					{
						score += answerScore.floatValue();
					}
					else
					{
						partial = true;
					}
				}
			}

			rv.append("<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/grade.png\" alt=\""
					+ context.getMessages().getString("score") + "\" />");
			rv.append(context.getMessages().getString("score") + ":&nbsp;" + formatScore(score) + "&nbsp;&nbsp;&nbsp;");

			selector = "of-points";
			if (partial)
			{
				selector += "-partial";
			}
		}

		// add the total possible points for the section
		Float score = part.getTotalPoints();
		if (score.equals(Float.valueOf((1))))
		{
			selector += "-singular";
		}
		Object[] args = new Object[1];
		args[0] = formatScore(score);
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
