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
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;

/**
 * The /grading view for the mneme tool.
 */
public class GradesView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(GradesView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Submission Service */
	protected SubmissionService submissionService = null;

	/** tool manager reference. */
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
		// sort (optional)
		if ((params.length != 2) && (params.length != 3))
		{
			throw new IllegalArgumentException();
		}

		// security
		if (!this.submissionService.allowEvaluate(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// sort
		String sortCode = "0A";
		if (params.length > 2) sortCode = params[2];
		if ((sortCode == null) || (sortCode.length() != 2))
		{
			throw new IllegalArgumentException();
		}
		context.put("sort_column", sortCode.charAt(0));
		context.put("sort_direction", sortCode.charAt(1));
		AssessmentService.AssessmentsSort sort = findSortCode(sortCode);

		// collect the assessments in this context
		List<Assessment> assessments = this.assessmentService.getContextAssessments(this.toolManager.getCurrentPlacement().getContext(), sort,
				Boolean.TRUE);
		context.put("assessments", assessments);

		// disable the tool navigation to this view
		context.put("disableGrades", Boolean.TRUE);

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
		// security
		if (!this.submissionService.allowEvaluate(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// read the form
		String destination = uiService.decode(req, context);

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * @param submissionService
	 *        the submissionService to set
	 */
	public void setSubmissionService(SubmissionService submissionService)
	{
		this.submissionService = submissionService;
	}

	/**
	 * Set the tool manager.
	 * 
	 * @param manager
	 *        The tool manager.
	 */
	public void setToolManager(ToolManager manager)
	{
		toolManager = manager;
	}

	/**
	 * Figure out the sort from the sort parameter.
	 * 
	 * @param sortCode
	 *        The sort parameter.
	 * @return The sort.
	 */
	protected AssessmentService.AssessmentsSort findSortCode(String sortCode)
	{
		AssessmentService.AssessmentsSort sort = null;

		// 0 is ddate
		if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'A'))
		{
			sort = AssessmentService.AssessmentsSort.ddate_a;
		}
		else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'D'))
		{
			sort = AssessmentService.AssessmentsSort.ddate_d;
		}
		// 1 is odate
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'A'))
		{
			sort = AssessmentService.AssessmentsSort.odate_a;
		}
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'D'))
		{
			sort = AssessmentService.AssessmentsSort.odate_d;
		}
		// 2 is title
		else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'A'))
		{
			sort = AssessmentService.AssessmentsSort.title_a;
		}
		else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'D'))
		{
			sort = AssessmentService.AssessmentsSort.title_d;
		}

		return sort;
	}
}
