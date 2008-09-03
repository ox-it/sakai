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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The question edit view for the mneme tool.
 */
public class QuestionEditView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(QuestionEditView.class);

	/** Question Service */
	protected QuestionService questionService = null;

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
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2] pool_sort / [3] pool_id / [4] question_sort / [5] question_page / [6] question_id
		if ((params.length != 7)) throw new IllegalArgumentException();
		String questionId = params[6];

		// put the extra parameters all together
		String extras = StringUtil.unsplit(params, 2, 4, "/");
		context.put("extras", extras);

		// get the question to work on
		Question question = this.questionService.getQuestion(questionId);
		if (question == null) throw new IllegalArgumentException();

		// check security
		if (!this.questionService.allowEditQuestion(question))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// put the question in the context
		context.put("question", question);

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
		// [2] pool_sort / [3] pool_id / [4] question_sort / [5] question_page / [6] question_id
		if ((params.length != 7)) throw new IllegalArgumentException();
		String questionId = params[6];

		// get the question to work on
		Question question = this.questionService.getQuestion(questionId);
		if (question == null) throw new IllegalArgumentException();

		// put the question in the context
		context.put("question", question);

		// read form
		String destination = this.uiService.decode(req, context);

		// consolidate the question
		destination = question.getTypeSpecificQuestion().consolidate(destination);

		// save
		try
		{
			this.questionService.saveQuestion(question);
		}
		catch (AssessmentPermissionException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// if destination became null, or is the stay here
		if ((destination == null) || ("STAY".equals(destination)))
		{
			destination = context.getDestination();
		}

		// redirect
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * @param questionService
	 *        the questionService to set
	 */
	public void setQuestionService(QuestionService questionService)
	{
		this.questionService = questionService;
	}
}
