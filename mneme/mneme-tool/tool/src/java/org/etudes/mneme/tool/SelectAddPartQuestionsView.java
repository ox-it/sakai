/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2013 Etudes, Inc.
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
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.PoolService.FindPoolsSort;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /select_add_mpart_question view for the mneme tool.
 */
public class SelectAddPartQuestionsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(SelectAddPartQuestionsView.class);

	/** Dependency: Pool service. */
	protected AssessmentService assessmentService = null;

	/** Dependency: mneme service. */
	protected MnemeService mnemeService = null;

	/** Configuration: the page sizes for the view. */
	protected List<Integer> pageSizes = new ArrayList<Integer>();

	/** Dependency: PoolService. */
	protected PoolService poolService = null;

	/** Dependency: Question service. */
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
		// [2] assessment id, [3] part id
		// [4] sort, [5] page, [6] type filter [7] pool filter [8] survey filter
		// return address in the rest
		if (params.length < 9) throw new IllegalArgumentException();

		String destination = null;
		if (params.length > 9)
		{
			destination = "/" + StringUtil.unsplit(params, 9, params.length - 9, "/");
		}
		// if not specified, go to the main list page
		else
		{
			destination = "/assessments";
		}
		context.put("return", destination);

		String assessmentId = params[2];
		Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		context.put("assessment", assessment);

		// security check
		if (!assessmentService.allowEditAssessment(assessment))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// part
		String partId = params[3];
		Part part = assessment.getParts().getPart(partId);
		if (part == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		context.put("part", part);

		// sort
		String sortCode = params[4];
		context.put("sort_column", sortCode.charAt(0));
		context.put("sort_direction", sortCode.charAt(1));
		QuestionService.FindQuestionsSort sort = findQuestionSortCode(sortCode);

		String typeFilter = params[6];
		context.put("typeFilter", typeFilter);

		// the question types
		List<QuestionPlugin> questionTypes = this.mnemeService.getQuestionPlugins();
		context.put("questionTypes", questionTypes);

		String poolFilter = params[7];
		context.put("poolFilter", poolFilter);
		Pool pool = poolFilter.equals("0") ? null : this.poolService.getPool(poolFilter);

		String surveyFilter = params[8];
		context.put("surveyFilter", surveyFilter);
		Boolean surveyFilterValue = null;
		if ("A".equals(surveyFilter))
		{
			surveyFilterValue = Boolean.FALSE;
		}
		else if ("S".equals(surveyFilter))
		{
			surveyFilterValue = Boolean.TRUE;
		}

		// the pools
		List<Pool> pools = this.poolService.findPools(toolManager.getCurrentPlacement().getContext(), FindPoolsSort.title_a, null);
		
		context.put("pools", pools);

		// paging
		Integer maxQuestions = null;
		if (pool == null)
		{
			maxQuestions = this.questionService.countQuestions(this.toolManager.getCurrentPlacement().getContext(), null,
					(typeFilter.equals("0") ? null : typeFilter), surveyFilterValue, Boolean.TRUE);
		}
		else
		{
			maxQuestions = this.questionService.countQuestions(pool, null, (typeFilter.equals("0") ? null : typeFilter), surveyFilterValue,
					Boolean.TRUE);
		}
		String pagingParameter = params[5];
		if ("-".equals(pagingParameter))
		{
			pagingParameter = "1-" + Integer.toString(this.pageSizes.get(0));
		}
		Paging paging = uiService.newPaging();
		paging.setMaxItems(maxQuestions);
		paging.setCurrentAndSize(pagingParameter);
		context.put("paging", paging);

		// get questions - even invalids
		List<Question> questions = null;

		if (pool == null)
		{
			questions = questionService.findQuestions(this.toolManager.getCurrentPlacement().getContext(), sort, null, (typeFilter.equals("0") ? null
					: typeFilter), paging.getSize() == 0 ? null : paging.getCurrent(), paging.getSize() == 0 ? null : paging.getSize(),
					surveyFilterValue, null);
		}
		else
		{
			questions = questionService.findQuestions(pool, sort, null, (typeFilter.equals("0") ? null : typeFilter), paging.getSize() == 0 ? null
					: paging.getCurrent(), paging.getSize() == 0 ? null : paging.getSize(), surveyFilterValue, null);
		}
		context.put("questions", questions);

		// compute the current destination, except for being at page one
		// this will match "current" in the filter drop-down values, which are all set to send to page one.
		String newDestination = "/" + params[1] + "/" + params[2] + "/" + params[3] + "/" + sortCode + "/" + "1" + "-" + paging.getSize().toString()
				+ "/" + typeFilter + "/" + poolFilter + "/" + surveyFilter + destination;

		// for the selected question type
		Value value = this.uiService.newValue();
		value.setValue(newDestination);
		context.put("selectedQuestionType", value);

		// for the selected pool
		value = this.uiService.newValue();
		value.setValue(newDestination);
		context.put("selectedPool", value);

		// for the survey filter
		value = this.uiService.newValue();
		value.setValue(newDestination);
		context.put("selectedQuestionSurvey", value);

		// pages sizes
		if (this.pageSizes.size() > 1)
		{
			context.put("pageSizes", this.pageSizes);
		}

		// for the selected "for" part
		value = this.uiService.newValue();
		value.setValue(part.getId());
		context.put("partId", value);

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		String pageSize = StringUtil.trimToNull(this.serverConfigurationService.getString("pageSize@org.etudes.mneme.tool.SelectAddPartQuestionsView"));
		if (pageSize != null) setPageSize(pageSize);

		if (this.pageSizes.isEmpty())
		{
			this.pageSizes.add(Integer.valueOf(50));
		}

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2] assessment id, [3] part id
		// [4] sort "0A", [5] page, [6] type filter [7] pool filter [8] survey filter
		// return address in the rest
		if (params.length < 9) throw new IllegalArgumentException();

		String assessmentId = params[2];
		Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// security check
		if (!assessmentService.allowEditAssessment(assessment))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		String partId = params[3];
		Part part = assessment.getParts().getPart(partId);
		if (part == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		Values values = this.uiService.newValues();
		context.put("questionids", values);

		// for the selected "for" part
		Value value = this.uiService.newValue();
		context.put("partId", value);

		// read form
		String destination = this.uiService.decode(req, context);

		// get the new part id
		String newPartId = value.getValue();
		if (!part.getId().equals(newPartId))
		{
			// create a new part?
			if ("0".equals(newPartId))
			{
				try
				{
					Part created = assessment.getParts().addPart();
					this.assessmentService.saveAssessment(assessment);
					newPartId = created.getId();
				}
				catch (AssessmentPermissionException e)
				{
					// redirect to error
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
					return;
				}
				catch (AssessmentPolicyException e)
				{
					// redirect to error
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
					return;
				}
			}

			Part newPart = assessment.getParts().getPart(newPartId);
			if (newPart != null)
			{
				part = newPart;

				// adjust the destination to use this part, if the destination is back to me
				String[] destParts = StringUtil.split(destination, "/");
				if (destParts[1].equals("select_add_mpart_question"))
				{
					destParts[3] = part.getId();
					destination = StringUtil.unsplit(destParts, 0, destParts.length, "/");
				}
			}
		}

		for (String id : values.getValues())
		{
			Question question = this.questionService.getQuestion(id);
			if (question != null)
			{
				part.addPickDetail(question);
			}
		}

		// commit the save
		try
		{
			this.assessmentService.saveAssessment(assessment);
		}
		catch (AssessmentPermissionException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}
		catch (AssessmentPolicyException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
			return;
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * @param mnemeService
	 *        the mnemeService to set
	 */
	public void setMnemeService(MnemeService mnemeService)
	{
		this.mnemeService = mnemeService;
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
	 * Set the PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setPoolService(PoolService service)
	{
		this.poolService = service;
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
	 *        the ToolManager.
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	/**
	 * Figure out the sort from the code.
	 * 
	 * @param sortCode
	 *        The sort code.
	 * @return The sort.
	 */
	protected QuestionService.FindQuestionsSort findQuestionSortCode(String sortCode)
	{
		QuestionService.FindQuestionsSort sort = QuestionService.FindQuestionsSort.type_a;
		// 0 is question type
		if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'A'))
		{
			sort = QuestionService.FindQuestionsSort.pool_title_a;
		}
		else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'D'))
		{
			sort = QuestionService.FindQuestionsSort.pool_title_d;
		}
		// 1 is pool title
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'A'))
		{
			sort = QuestionService.FindQuestionsSort.type_a;
		}
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'D'))
		{
			sort = QuestionService.FindQuestionsSort.type_d;
		}
		// 2 is pool points
		else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'A'))
		{
			sort = QuestionService.FindQuestionsSort.pool_points_a;
		}
		else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'D'))
		{
			sort = QuestionService.FindQuestionsSort.pool_points_d;
		}
		// 3 is question description
		else if ((sortCode.charAt(0) == '3') && (sortCode.charAt(1) == 'A'))
		{
			sort = QuestionService.FindQuestionsSort.description_a;
		}
		else if ((sortCode.charAt(0) == '3') && (sortCode.charAt(1) == 'D'))
		{
			sort = QuestionService.FindQuestionsSort.description_d;
		}

		return sort;
	}
}
