/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2012 Etudes, Inc.
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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;

/**
 * The /freeze view for the mneme tool.
 */
public class FreezeView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FreezeView.class);

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
		// [2]sort for /grades, [3]aid
		if (params.length != 4) throw new IllegalArgumentException();

		// grades sort parameter
		String gradesSortCode = params[2];
		context.put("sort_grades", gradesSortCode);

		// security
		if (!this.submissionService.allowEvaluate(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		Assessment assessment = this.assessmentService.getAssessment(params[3]);
		if (assessment == null)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check that we have a survey
		if (assessment.getType() != AssessmentType.survey)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check that the assessment is not a formal course evaluation
		if (assessment.getFormalCourseEval())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// check that it has NOT been frozen
		if (assessment.getFrozen())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// collect all the submissions for the assessment
		List<Submission> submissions = this.submissionService.findAssessmentSubmissions(assessment,
				SubmissionService.FindAssessmentSubmissionsSort.sdate_a, Boolean.FALSE, null, null, null, null);
		AssessmentStatsView.computePercentComplete(assessment, submissions, context);

		// see if we have any in-progress
		boolean incomplete = false;
		for (Submission s : submissions)
		{
			// if non-phantom and incomplete, count it
			if ((!s.getIsPhantom()) && (!s.getIsComplete()) && (!s.getIsTestDrive()))
			{
				incomplete = true;
				break;
			}
		}
		context.put("incomplete", Boolean.valueOf(incomplete));

		context.put("assessment", assessment);

		// pick up any later special access date
		Date specialClose = null;
		for (AssessmentAccess a : assessment.getSpecialAccess().getAccess())
		{
			if (a.getOverrideAcceptUntilDate())
			{
				if ((specialClose == null) || a.getAcceptUntilDate().after(specialClose)) specialClose = a.getAcceptUntilDate();
			}
			else if (a.getOverrideDueDate())
			{
				if ((specialClose == null) || a.getDueDate().after(specialClose)) specialClose = a.getDueDate();
			}
		}
		if ((assessment.getDates().getSubmitUntilDate() != null) && (specialClose != null)
				&& (specialClose.before(assessment.getDates().getSubmitUntilDate())))
		{
			specialClose = null;
		}
		if (specialClose != null) context.put("specialClose", specialClose);

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
		// [2]sort for /grades, [3]aid
		if (params.length != 4) throw new IllegalArgumentException();

		// grades sort parameter
		String gradesSortCode = params[2];

		// security
		if (!this.submissionService.allowEvaluate(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		Assessment assessment = this.assessmentService.getAssessment(params[3]);
		if (assessment == null)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check that we have a survey
		if (assessment.getType() != AssessmentType.survey)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check that the assessment is not a formal course evaluation
		if (assessment.getFormalCourseEval())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// check that it has NOT been frozen
		if (assessment.getFrozen())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// read the form
		String destination = uiService.decode(req, context);

		// if publish, set
		if ("FREEZE".equals(destination))
		{
			assessment.setFrozen();
			destination = "/assessment_stats/" + gradesSortCode + "/" + assessment.getId();

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
		}

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
}
