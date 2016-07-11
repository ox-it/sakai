/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2013, 2014 Etudes, Inc.
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
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.Submission;

/**
 * The "FormatScore" format delegate for the mneme tool.
 */
public class SubmissionScoreDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(SubmissionScoreDelegate.class);

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
		Float score = null;
		if ((review != null) && review && (submission != null) && submission.getIsReleased())
		{
			// the total score
			rv.append("<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/grade.png\" alt=\""
					+ context.getMessages().getString("grade") + "\" />");
			score = submission.getTotalScore();
			rv.append(context.getMessages().getString("grade") + ":&nbsp;" + FormatScoreDelegate.formatScore(score) + "&nbsp;&nbsp;&nbsp;");

			selector = "of-points";

			boolean partial = submission.getHasUnscoredAnswers();
			if (partial)
			{
				selector += "-partial";
			}
			if (assessment.getMinScoreSet().booleanValue() && (assessment.getType() != AssessmentType.survey))
			{
				float minScore = (assessment.getMinScore() * assessment.getPoints().floatValue()) / 100;
				if (score.floatValue() >= minScore)
				{
					context.put("minscoremet", Boolean.TRUE);
				}
			}

		}

		// add the total possible points for the assessment
		Float maxPoints = assessment.getPoints();
		//Float maxPoints = assessment.getParts().getTotalPoints();
		if (maxPoints.equals(Float.valueOf(1)))
		{
			selector += "-singular";
		}
		Object[] args = new Object[1];
		args[0] = FormatScoreDelegate.formatScore(maxPoints);
		rv.append(context.getMessages().getFormattedMessage(selector, args));

		
		Float passMark = assessment.getPassMark(); 
		if (passMark != null)
		{
			rv.append("&nbsp;&nbsp;&nbsp;");
			if (score != null) 
			{
				if ((maxPoints > 0 ? score / maxPoints * 100 : 0) >= passMark)
				{
					rv.append("<img src=\"").append(context.get("sakai.return.url"));
					rv.append("/icons/correct.png\" alt=\"");
					rv.append(context.getMessages().getString("pass")).append("\" />&nbsp;");
					rv.append(context.getMessages().getString("pass")).append("&nbsp;");
				}
				else
				{
					rv.append("<img src=\"").append(context.get("sakai.return.url"));
					rv.append("/icons/incorrect.png\" alt=\"");
					rv.append(context.getMessages().getString("fail")).append("\" />&nbsp;");
					rv.append(context.getMessages().getString("fail")).append("&nbsp;");
				}
			}
			rv.append("(Pass Mark:&nbsp;");
			rv.append(FormatScoreDelegate.formatScore(passMark)).append("&nbsp;%)");
		}
		
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
