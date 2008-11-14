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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * The /question_cleanup view for the mneme test tool.
 */
public class QuestionCleanupView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(QuestionCleanupView.class);

	/** The question service. */
	protected QuestionService questionService = null;

	/** The security service. */
	protected SecurityService securityService = null;

	/** The ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

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
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
	{
		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			throw new IllegalArgumentException();
		}

		// no parameters expected
		if (params.length != 2)
		{
			throw new IllegalArgumentException();
		}

		// clean up the questions
		try
		{
			String rv = cleanup();

			context.put("rv", rv);
		}
		catch (AssessmentPermissionException e)
		{
			M_log.warn(e);
		}

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Set the QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionService service)
	{
		this.questionService = service;
	}

	/**
	 * Set the security service.
	 * 
	 * @param service
	 *        The security service.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Set the thread local manager.
	 * 
	 * @param service
	 *        The thread local manager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		this.threadLocalManager = service;
	}

	protected String cleanup() throws AssessmentPermissionException
	{
		StringBuilder results = new StringBuilder();

		// find all non-historical question ids
		List<String> questionIds = this.questionService.findAllNonHistoricalIds();

		// process each one
		for (String id : questionIds)
		{
			// get the question
			Question question = this.questionService.getQuestion(id);
			if (question == null) continue;

			// save it, without needing to make any changes, and without changing attribution
			this.questionService.forceSave(question);
		}

		results.append(questionIds.size() + " questions saved.");
		return results.toString();
	}
}
