/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /instructions view for the mneme tool.
 */
public class InstructionsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(InstructionsView.class);

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
		// sid and return
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		String submissionId = params[2];
		String returnDestination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");

		// collect the submission
		Submission submission = submissionService.getSubmission(submissionId);
		if (submission == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		if (!submissionService.allowCompleteSubmission(submission))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// Separate out the final return path from the immediate return - the timers that submit need to know the final return path (like /list)
		// Note: this is nasty hard coded to all the places that instructions can be called from.
		String finalReturn = "/list";
		if ("part_instructions".equals(params[3]))
		{
			finalReturn = "/" + StringUtil.unsplit(params, 6, params.length - 6, "/");
		}
		else if ("question".equals(params[3]))
		{
			finalReturn = "/" + StringUtil.unsplit(params, 7, params.length - 7, "/");
		}
		else if ("toc".equals(params[3]))
		{
			finalReturn = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
		}
		context.put("return", finalReturn);

		context.put("submission", submission);
		context.put("destination", returnDestination);

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
		// sid and return
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		String submissionId = params[2];

		String returnDestination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");

		// read form
		String destination = this.uiService.decode(req, context);

		// if other than the /submitted destination, just go there
		if (!destination.startsWith("/submitted"))
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
			return;
		}

		// this post is from the timer, and completes the submission
		// TODO: this returnDestination may not be quite right -ggolden
		TocView.submissionCompletePost(req, res, context, submissionId, this.uiService, this.submissionService, returnDestination);
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
