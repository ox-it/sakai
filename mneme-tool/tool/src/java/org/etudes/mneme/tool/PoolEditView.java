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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Paging;
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The pools delete view for the mneme tool.
 */
public class PoolEditView extends ControllerImpl
{
	/** The default sort. */
	public static String DEFAULT_SORT = "2A";

	/** Our log. */
	private static Log M_log = LogFactory.getLog(PoolEditView.class);

	/**
	 * Figure the sort.
	 * 
	 * @param sortCode
	 *        The sort code string.
	 * @return The sort.
	 */
	public static QuestionService.FindQuestionsSort findSort(String sortCode)
	{
		QuestionService.FindQuestionsSort sort = null;

		// default 0A
		if ("-".equals(sortCode))
		{
			sort = QuestionService.FindQuestionsSort.description_a;
		}

		// 0 is description
		else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'A'))
			sort = QuestionService.FindQuestionsSort.description_a;
		else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'D'))
			sort = QuestionService.FindQuestionsSort.description_d;

		// 1 is type
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'A'))
			sort = QuestionService.FindQuestionsSort.type_a;
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'D'))
			sort = QuestionService.FindQuestionsSort.type_d;

		// 2 is creation date
		else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'A'))
			sort = QuestionService.FindQuestionsSort.cdate_a;
		else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'D'))
			sort = QuestionService.FindQuestionsSort.cdate_d;

		else
		{
			throw new IllegalArgumentException();
		}

		return sort;
	}

	/** Configuration: the page sizes for the view. */
	protected List<Integer> pageSizes = new ArrayList<Integer>();

	/** Pool Service */
	protected PoolService poolService = null;

	/** Question Service */
	protected QuestionService questionService = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService serverConfigurationService = null;

	/** Dependency: ToolManager */
	protected ToolManager toolManager = null;

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
		// Note: parameter and sort logic changes need to be coordinated with QuestionPreviewView.figurePrevNextForPoolEdit()

		// pool id, sort, paging, assessment id, all the rest is return parameters
		if (params.length < 6)
		{
			throw new IllegalArgumentException();
		}

		boolean fixMode = params[1].equals("pool_fix");
		if (fixMode) context.put("fix", Boolean.TRUE);

		String destination = null;
		if (params.length > 6)
		{
			destination = "/" + StringUtil.unsplit(params, 6, params.length - 6, "/");
		}

		// if not specified, go to the main pools page
		else
		{
			destination = "/pools";
		}
		context.put("return", destination);

		// this view
		context.put("view", params[1]);

		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// pool
		String pid = params[2];
		Pool pool = this.poolService.getPool(pid);
		if (pool == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		context.put("pool", pool);

		// sort
		String sortCode = DEFAULT_SORT;
		if (!params[3].equals("-")) sortCode = params[3];
		if ((sortCode == null) || (sortCode.length() != 2))
		{
			throw new IllegalArgumentException();
		}
		context.put("sort_column", sortCode.charAt(0));
		context.put("sort_direction", sortCode.charAt(1));
		QuestionService.FindQuestionsSort sort = findSort(sortCode);

		// paging
		String pagingParameter = "1-" + Integer.toString(this.pageSizes.get(0));
		if (!params[4].equals("-")) pagingParameter = params[4];
		Integer maxQuestions = this.questionService.countQuestions(pool, null, null, null, null);
		Paging paging = uiService.newPaging();
		paging.setMaxItems(maxQuestions);
		paging.setCurrentAndSize(pagingParameter);
		context.put("paging", paging);

		// assessment id (only if we are editing a historical pool in fix mode)
		String aid = params[5];
		context.put("aid", aid);

		// get questions
		List<Question> questions = questionService.findQuestions(pool, sort, null, null, paging.getSize() == 0 ? null : paging.getCurrent(), paging
				.getSize() == 0 ? null : paging.getSize(), null, null);
		context.put("questions", questions);

		// pages sizes
		if (this.pageSizes.size() > 1)
		{
			context.put("pageSizes", this.pageSizes);
		}

		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		String pageSize = StringUtil.trimToNull(this.serverConfigurationService.getString("pageSize@org.etudes.mneme.tool.PoolEditView"));
		if (pageSize != null) setPageSize(pageSize);

		if (this.pageSizes.isEmpty())
		{
			this.pageSizes.add(Integer.valueOf(30));
		}

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// pool id, sort, paging, all the rest is return parameters
		if (params.length < 6)
		{
			throw new IllegalArgumentException();
		}

		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		boolean fixMode = params[1].equals("pool_fix");

		// pool
		String pid = params[2];
		Pool pool = this.poolService.getPool(pid);
		if (pool == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// for the selected questions to delete
		Values values = this.uiService.newValues();
		context.put("questionids", values);

		// read form
		context.put("pool", pool);
		String destination = this.uiService.decode(req, context);

		try
		{
			this.poolService.savePool(pool);
		}
		catch (AssessmentPermissionException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		if ((!fixMode) && destination.equals("DELETE"))
		{
			for (String id : values.getValues())
			{
				Question question = this.questionService.getQuestion(id);
				if (question != null)
				{
					try
					{
						this.questionService.removeQuestion(question);
					}
					catch (AssessmentPermissionException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
						return;
					}
				}
			}

			// stay here
			destination = context.getDestination();
		}

		else if ((!fixMode) && destination.trim().startsWith("DUPLICATE:"))
		{
			String[] parts = StringUtil.split(destination, ":");
			if (parts.length != 2)
			{
				throw new IllegalArgumentException();
			}
			String qid = parts[1];
			try
			{
				Question question = this.questionService.getQuestion(qid);
				if (question != null)
				{
					// copy within the same pool
					this.questionService.copyQuestion(question, null);
				}

				// stay here
				destination = context.getDestination();
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}
		}

		else if ((!fixMode) && ((destination.trim().startsWith("/question_copy")) || (destination.trim().startsWith("/question_move")))
				|| (destination.trim().startsWith("/question_preview")))
		{
			// add the selected ids to the destination
			StringBuilder buf = new StringBuilder();
			buf.append(destination);
			buf.append("/");
			for (String id : values.getValues())
			{
				buf.append(id);
				buf.append("+");
			}
			buf.setLength(buf.length() - 1);

			// also add default sort parameter placeholder (for move and copy)
			if (!destination.trim().startsWith("/question_preview"))
			{
				buf.append("/-");
			}

			destination = buf.toString();
		}

		else if ((!fixMode) && "ADD".equals(destination))
		{
			// create a question - type? TODO:
			String type = "mneme:MultipleChoice";
			// create the question of the appropriate type (all the way to save)
			Question newQuestion = null;
			try
			{
				newQuestion = this.questionService.newQuestion(pool, type);
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}

			String returnDest = StringUtil.unsplit(params, "/");

			// create URL for add questions /select_question_type/POOL/RETURN
			destination = "/question_edit/" + newQuestion.getId() + "/0/0" + returnDest;
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the the page size for the view.
	 * 
	 * @param sizes
	 *        The the page sizes for the view - integers, comma separated.
	 */
	public void setPageSize(String sizes)
	{
		this.pageSizes.clear();
		String[] parts = StringUtil.split(sizes, ",");
		for (String part : parts)
		{
			this.pageSizes.add(Integer.valueOf(part));
		}
	}

	/**
	 * @param poolService
	 *        the poolService to set
	 */
	public void setPoolService(PoolService poolService)
	{
		this.poolService = poolService;
	}

	/**
	 * @param questionService
	 *        the questionService to set
	 */
	public void setQuestionService(QuestionService questionService)
	{
		this.questionService = questionService;
	}

	/**
	 * Set the ServerConfigurationService.
	 * 
	 * @param service
	 *        the ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		this.serverConfigurationService = service;
	}

	/**
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}
}
