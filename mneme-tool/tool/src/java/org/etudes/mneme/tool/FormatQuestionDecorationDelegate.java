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
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.Answer;
import org.sakaiproject.util.StringUtil;

/**
 * The "FormatQuestionDecoration" format delegate for the mneme tool.
 */
public class FormatQuestionDecorationDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatQuestionDecorationDelegate.class);

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

		Object o = context.get("submission");
		if (!(o instanceof Submission)) return value.toString();
		Submission submission = (Submission) o;

		Assessment assessment = submission.getAssessment();
		if (assessment == null) return value.toString();

		// search for our answer without creating it, and if found check the answered and mark for review setting
		boolean answered = false;
		boolean markForReview = false;
		boolean missingReason = false;
		for (Answer answer : submission.getAnswers())
		{
			if (answer.getQuestion().equals(question))
			{
				answered = answer.getIsAnswered().booleanValue() && answer.getIsComplete().booleanValue();
				markForReview = answer.getMarkedForReview().booleanValue();
				missingReason = (question.getExplainReason() && (StringUtil.trimToNull(answer.getReason()) == null));
				break;
			}
		}

		// if not found, or not answered, use the unanswered icon
		if (!answered)
		{
			return "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/unanswered.png\" title=\""
					+ context.getMessages().getString("format-question-decoration-unanswered") + "\" alt=\""
					+ context.getMessages().getString("format-question-decoration-unanswered") + "\"/>";
		}

		// if mark for review, use that icon
		else if (markForReview)
		{
			return "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/markedforreview.png\" title=\""
					+ context.getMessages().getString("format-question-decoration-mark-for-review") + "\" alt=\""
					+ context.getMessages().getString("format-question-decoration-mark-for-review") + "\"/>";
		}

		// if rationale is needed and not present, use the no-rationale icon
		else if (missingReason)
		{
			return "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/reason.png\" title=\""
					+ context.getMessages().getString("format-question-decoration-rationale") + "\" alt=\""
					+ context.getMessages().getString("format-question-decoration-rationale") + "\"/>";
		}

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
