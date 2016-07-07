/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/AccessFillinPositionsDelegate.java $
 * $Id: AccessFillinPositionsDelegate.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificQuestion;

/**
 * The "AccessFillInlinePositionsDelegate" format delegate for the mneme tool.
 */
public class AccessFillInlinePositionsDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AccessFillInlinePositionsDelegate.class);

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
		return value.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object formatObject(Context context, Object value)
	{
		// value is the question
		if (value == null) return null;
		if (!(value instanceof Question)) return value;
		Question question = (Question) value;

		TypeSpecificQuestion tsq = question.getTypeSpecificQuestion();
		if (!(tsq instanceof FillInlineQuestionImpl)) return value;

		FillInlineQuestionImpl plugin = (FillInlineQuestionImpl) tsq;

		List<String> corrects = plugin.getCorrectAnswers();
		List<Integer> rv = new ArrayList<Integer>();
		for (int i = 1; i <= corrects.size(); i++)
		{
			rv.add(Integer.valueOf(i));
		}

		return rv;
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
