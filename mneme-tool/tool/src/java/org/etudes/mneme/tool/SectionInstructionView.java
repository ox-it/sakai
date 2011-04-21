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
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.QuestionGrouping;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /part_instructions view for the mneme tool.
 */
public class SectionInstructionView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(SectionInstructionView.class);

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
		// we need two parameters (submissionId, partId) then return
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		String submissionId = params[2];
		String partId = params[3];

		String destination = null;
		if (params.length > 4)
		{
			destination = "/" + StringUtil.unsplit(params, 4, params.length - 4, "/");
		}

		// if not specified, go to the main list view
		else
		{
			destination = "/list";
		}
		context.put("return", destination);

		// collect the submission
		Submission submission = submissionService.getSubmission(submissionId);
		if (submission == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// make sure by-question is valid for this assessment
		if (submission.getAssessment().getQuestionGrouping() != QuestionGrouping.question)
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

		// collect the part
		Part part = submission.getAssessment().getParts().getPart(partId);
		if (part == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		context.put("submission", submission);
		context.put("part", part);

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
		// we need two parameters (submissionId, sectionId) then return
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		String returnDestination = null;
		if (params.length > 3)
		{
			returnDestination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");
		}

		// if not specified, go to the main list view
		else
		{
			returnDestination = "/list";
		}

		// read form
		String destination = this.uiService.decode(req, context);

		// if other than the /submitted destination, just go there
		if (!destination.startsWith("/submitted") && (!destination.equals("SUBMIT")))
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
			return;
		}

		String submissionId = params[2];

		// this post is from the timer, and completes the submission
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
