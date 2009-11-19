/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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
import org.etudes.mneme.api.Question;

/**
 * The "FormatQuestionTitlePositional" format delegate for the mneme tool.
 */
public class FormatQuestionTitlePositionalDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatQuestionTitlePositionalDelegate.class);

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
		if (!(value instanceof Question)) return null;

		Question question = (Question) value;
		Boolean continuous = question.getPart().getAssessment().getParts().getContinuousNumbering();

		Object[] args = new Object[2];
		if (continuous)
		{
			args[0] = question.getAssessmentOrdering().getPosition();
			args[1] = question.getPart().getAssessment().getParts().getNumQuestions();
		}
		else
		{
			args[0] = question.getPartOrdering().getPosition();
			args[1] = question.getPart().getNumQuestions();
		}

		String template = "format-question-title-positional";
		if (question.getPart().getAssessment().getIsSingleQuestion())
		{
			template = "format-question-single-title-positional";
		}

		return context.getMessages().getFormattedMessage(template, args);
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
