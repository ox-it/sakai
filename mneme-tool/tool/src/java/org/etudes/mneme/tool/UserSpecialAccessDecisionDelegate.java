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
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.util.DecisionDelegateImpl;
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.Submission;

/**
 * The "UserSpecialAccessDecision" decision delegate for the mneme tool.
 */
public class UserSpecialAccessDecisionDelegate extends DecisionDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(UserSpecialAccessDecisionDelegate.class);

	/**
	 * {@inheritDoc}
	 */
	public boolean decide(Decision decision, Context context, Object focus)
	{
		// focus is the submission
		if (focus == null) return false;
		if (!(focus instanceof Submission)) return false;
		Submission submission = (Submission) focus;

		AssessmentAccess found = submission.getAssessment().getSpecialAccess().getUserAccess(submission.getUserId());
		if (found == null) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
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
