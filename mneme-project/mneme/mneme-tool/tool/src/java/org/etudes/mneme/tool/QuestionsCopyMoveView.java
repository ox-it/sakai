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
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /question_copy view for the mneme tool.
 */
public class QuestionsCopyMoveView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(QuestionsCopyMoveView.class);

	/** Pool Service */
	protected PoolService poolService = null;

	/** Dependency: Question service. */
	protected QuestionService questionService = null;

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
		// [, question_move, pool_edit, 21(pool id), 2A, 1-30, pools, 0A, 56, -] - second to last parameter is the list of question ids, last is sort
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		// check security
		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		String questionIds = params[params.length - 2];

		// if canceled, a place to return to
		String returnDest = "/" + StringUtil.unsplit(params, 2, params.length - 4, "/");
		context.put("return", returnDest);

		// for sort, this destination without the sort
		String here = "/" + params[1] + returnDest + "/" + questionIds;
		context.put("here", here);

		// header and icon dependent on which function
		if (path.startsWith("question_copy"))
		{
			context.put("headerText", messages.get("copy-header-text"));
			context.put("headerIcon", "/icons/copy.png");
		}
		else if (path.startsWith("question_move"))
		{
			context.put("headerText", messages.get("move-header-text"));
			context.put("headerIcon", "/icons/page_go.png");
		}

		// sort
		String sortCode = "0A";
		if (!"-".equals(params[params.length - 1])) sortCode = params[params.length - 1];
		if ((sortCode == null) || (sortCode.length() != 2))
		{
			throw new IllegalArgumentException();
		}
		context.put("sort_column", sortCode.charAt(0));
		context.put("sort_direction", sortCode.charAt(1));
		PoolService.FindPoolsSort sort = null;
		// 0 is title
		if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'A'))
		{
			sort = PoolService.FindPoolsSort.title_a;
		}
		else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'D'))
		{
			sort = PoolService.FindPoolsSort.title_d;
		}
		else
		{
			throw new IllegalArgumentException();
		}

		// pools - all but the one we came from
		String pid = params[3];
		Pool pool = this.poolService.getPool(pid);
		if (pool == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		List<Pool> pools = this.poolService.findPools(toolManager.getCurrentPlacement().getContext(), sort, null);
		pools.remove(pool);
		context.put("pools", pools);

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
		// [, question_move, pool_edit, 21(pool id), 2A, 1-30, pools, 0A, 56] - last parameter is the list of question ids
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		// check security
		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		String questionIds = params[params.length - 2];

		// for the selected pool
		Value value = this.uiService.newValue();
		context.put("selectedPoolId", value);

		// read form
		String destination = this.uiService.decode(req, context);

		if (destination.equals("DOIT"))
		{
			String selectedPoolId = value.getValue();
			if (selectedPoolId != null)
			{
				Pool pool = this.poolService.getPool(selectedPoolId);
				try
				{
					String qids[] = StringUtil.split(questionIds, "+");
					for (String qid : qids)
					{
						// get the question
						Question question = this.questionService.getQuestion(qid);
						if (question != null)
						{
							// which function to perform
							if (path.startsWith("question_copy"))
							{
								this.questionService.copyQuestion(question, pool);
							}
							else if (path.startsWith("question_move"))
							{
								this.questionService.moveQuestion(question, pool);
							}
						}
					}

					// back to the pool
					destination = "/" + StringUtil.unsplit(params, 2, params.length - 4, "/");
				}
				catch (AssessmentPermissionException e)
				{
					// redirect to error
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
					return;
				}
			}
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
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
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}
}
