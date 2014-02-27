/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;

/**
 * The /list view for the mneme tool.
 */
public class ListView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ListView.class);

	/** Dependency: AssessmentService. */
	protected AssessmentService assessmentService = null;

	/** Dependency: SubmissionService. */
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
		// 0 or 1 parameters
		if ((params.length != 2) && (params.length != 3))
		{
			throw new IllegalArgumentException();
		}

		// optional sort parameter
		String sortCode = null;
		if (params.length == 3)
		{
			sortCode = params[2];
		}

		// check security
		if (!assessmentService.allowListDeliveryAssessment(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// SORT: 0|1|2|3 A|D - 2 chars, column | direction
		if ((sortCode != null) && (sortCode.length() != 2))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		SubmissionService.GetUserContextSubmissionsSort sort = SubmissionService.GetUserContextSubmissionsSort.title_a;
		if (sortCode != null)
		{
			context.put("sort_column", sortCode.charAt(0));
			context.put("sort_direction", sortCode.charAt(1));

			// 0 is title
			if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'A'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.title_a;
			}
			else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'D'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.title_d;
			}

			// 1 is status
			else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'A'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.status_a;
			}
			else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'D'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.status_d;
			}

			// 2 is due date
			else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'A'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.dueDate_a;
			}
			else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'D'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.dueDate_d;
			}

			// 3 is type
			else if ((sortCode.charAt(0) == '3') && (sortCode.charAt(1) == 'A'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.type_a;
			}
			else if ((sortCode.charAt(0) == '3') && (sortCode.charAt(1) == 'D'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.type_d;
			}
			
			// 4 is published status
			else if ((sortCode.charAt(0) == '4') && (sortCode.charAt(1) == 'A'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.published_a;
			}
			else if ((sortCode.charAt(0) == '4') && (sortCode.charAt(1) == 'D'))
			{
				sort = SubmissionService.GetUserContextSubmissionsSort.published_d;
			}

			else
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
				return;
			}
		}

		// default sort: status descending
		if (sortCode == null)
		{
			context.put("sort_column", '1');
			context.put("sort_direction", 'D');
			sort = SubmissionService.GetUserContextSubmissionsSort.status_d;
		}

		// collect information: submissions / assessments
		// TODO: get unpublished as well for test drive
		List<Submission> submissions = this.submissionService.getUserContextSubmissions(toolManager.getCurrentPlacement().getContext(), null, sort);
		context.put("submissions", submissions);

		// disable the tool navigation to this view
		context.put("disableDelivery", Boolean.TRUE);

		// for the tool navigation
		if (this.assessmentService.allowManageAssessments(toolManager.getCurrentPlacement().getContext()))
		{
			context.put("maintainer", Boolean.TRUE);
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
		// read form
		String destination = this.uiService.decode(req, context);

		// go there
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the assessment service.
	 * 
	 * @param service
	 *        The assessment service.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Set the submission service.
	 * 
	 * @param service
	 *        The submission service.
	 */
	public void setSubmissionService(SubmissionService service)
	{
		this.submissionService = service;
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
}
