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
import org.etudes.mneme.api.Submission;

/**
 * The "FormatListGrade" format delegate for the mneme tool.
 */
public class FormatListGradeDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatListGradeDelegate.class);

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	public String format(Context context, Object value)
	{
		if (!(value instanceof Submission)) return context.getMessages().getString("dash");
		Submission submission = (Submission) value;

		if (!submission.getAssessment().getHasPoints())
		{
			return context.getMessages().getString("na");
		}

		if (submission.getIsComplete())
		{
			if (submission.getIsReleased())
			{
				if (submission.getHasUnscoredAnswers())
				{
					Object[] args = new Object[1];
					args[0] = FormatScoreDelegate.formatScore(submission.getTotalScore());
					return context.getMessages().getFormattedMessage("format-list-grade-partial", args);
				}
				else
				{
					return FormatScoreDelegate.formatScore(submission.getTotalScore());
				}
			}
			else
			{
				return context.getMessages().getString("not-graded");
			}
		}

		return context.getMessages().getString("dash");
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
