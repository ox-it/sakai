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
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Submission;

/**
 * The "FormatScore" format delegate for the mneme tool.
 */
public class SubmissionScoreDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(SubmissionScoreDelegate.class);

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
		// submission or assessment
		if (value == null) return null;
		Submission submission = null;
		Assessment assessment = null;

		if (value instanceof Submission)
		{
			submission = (Submission) value;
			assessment = submission.getAssessment();
		}

		else if (value instanceof Assessment)
		{
			assessment = (Assessment) value;
		}

		if (assessment == null) return value.toString();

		// use the {}/{} format if doing feedback, or just {} if not.
		StringBuffer rv = new StringBuffer();

		Boolean review = (Boolean) context.get("review");

		String selector = "worth-points";

		// if we are doing review and the submission has been graded
		if ((review != null) && review && (submission != null) && submission.getIsReleased())
		{
			// the total score
			rv.append("<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/grade.png\" alt=\""
					+ context.getMessages().getString("grade") + "\" />");
			Float score = submission.getTotalScore();
			rv.append(context.getMessages().getString("grade") + ":&nbsp;" + formatScore(score) + "&nbsp;&nbsp;&nbsp;");

			selector = "of-points";

			boolean partial = submission.getHasUnscoredAnswers();
			if (partial)
			{
				selector += "-partial";
			}
		}

		// add the total possible points for the assessment
		Float score = assessment.getParts().getTotalPoints();
		if (score.equals(Float.valueOf(1)))
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
