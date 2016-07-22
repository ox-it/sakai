/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.util.DecisionDelegateImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.QuestionPick;

/**
 * The "QuestionReferencedDecision" decision delegate for the mneme tool.
 */
public class QuestionReferencedDecisionDelegate extends DecisionDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(QuestionReferencedDecisionDelegate.class);

	/**
	 * {@inheritDoc}
	 */
	public boolean decide(Decision decision, Context context, Object focus)
	{
		// true if the focus, a question id, is directly referenced by the "assessment" as a manual pick

		// focus is the question id
		if (focus == null) return false;
		if (!(focus instanceof String)) return false;
		String questionId = (String) focus;

		// get the assessment
		Assessment assessment = (Assessment) context.get("assessment");
		if (assessment == null) return false;

		// if the question can be found in the assessment, it is referenced
		for (Part part : assessment.getParts().getParts())
		{
			for (PartDetail detail : part.getDetails())
			{
				if (detail instanceof QuestionPick)
				{
					QuestionPick pick = (QuestionPick) detail;

					if (pick.getQuestionId().equals(questionId))
					{
						return true;
					}
				}
			}
		}

		return false;
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
