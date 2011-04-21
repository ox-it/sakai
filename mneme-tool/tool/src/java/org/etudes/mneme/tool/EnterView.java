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
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentClosedException;
import org.etudes.mneme.api.AssessmentCompletedException;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionGrouping;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /enter view for the mneme tool.
 */
public class EnterView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(EnterView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

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
		// we need a single parameter (aid), then return
		if (params.length < 3)
		{
			throw new IllegalArgumentException();
		}

		String assessmentId = params[2];
		String destination = null;
		if (params.length > 3)
		{
			destination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");
		}

		// if not specified, go to the main list view
		else
		{
			destination = "/list";
		}
		context.put("return", destination);

		// get the assessment
		Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// get the submissions from the user to this assessment
		Submission submission = submissionService.getNewUserAssessmentSubmission(assessment, null);
		if (submission == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check for closed (test drive can skip this)
		if (!submission.getIsTestDrive())
		{
			if (submission.getAssessment().getDates().getIsClosed().booleanValue())
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.closed)));
				return;
			}
		}

		// security check (submissions count / allowed check)
		if (!submissionService.allowSubmit(submission))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// collect information: the selected assessment (id the request)
		context.put("assessment", submission.getAssessment());

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
		// we need a single parameter (aid)
		if (params.length < 3)
		{
			throw new IllegalArgumentException();
		}

		String assessmentId = params[2];
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

		// // check expected
		// if (!context.getPostExpected())
		// {
		// // redirect to error
		// res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unexpected)));
		// return;
		// }

		// for the password
		Value value = this.uiService.newValue();
		context.put("password", value);

		// for the honor pledge
		Value pledge = this.uiService.newValue();
		context.put("pledge", pledge);

		// read form
		String destination = this.uiService.decode(req, context);

		// if other than the ENTER destination, just go there
		if (!destination.equals("ENTER"))
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
			return;
		}

		// process: enter the assessment for this user, find the submission id and starting question
		Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// get the submissions from the user to this assessment
		Submission submission = submissionService.getNewUserAssessmentSubmission(assessment, null);
		if (submission == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check password
		if ((submission.getAssessment().getPassword().getPassword() != null)
				&& (!submission.getAssessment().getPassword().checkPassword(value.getValue())))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.password)));
			return;
		}

		// check pledge
		if (submission.getAssessment().getRequireHonorPledge() && (!"true".equals(pledge.getValue())))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.pledge)));
			return;
		}

		enterSubmission(req, res, submission, returnDestination);
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
	 * Send the user into the submission.
	 * 
	 * @param req
	 *        Servlet request.
	 * @param res
	 *        Servlet response.
	 * @param submission
	 *        The submission set for the user to the assessment so far.
	 * @param returnDestination
	 *        The final return destination path.
	 * @throws IOException
	 */
	protected void enterSubmission(HttpServletRequest req, HttpServletResponse res, Submission submission, String returnDestination)
			throws IOException
	{
		Submission enterSubmission = null;
		try
		{
			enterSubmission = submissionService.enterSubmission(submission);
		}
		catch (AssessmentClosedException e)
		{
		}
		catch (AssessmentCompletedException e)
		{
		}
		catch (AssessmentPermissionException e)
		{
		}

		if (enterSubmission == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		redirectToQuestion(req, res, enterSubmission, false, true, returnDestination);
	}

	/**
	 * Redirect to the appropriate question screen for this submission
	 * 
	 * @param req
	 *        Servlet request.
	 * @param res
	 *        Servlet response.
	 * @param submission
	 *        The submission.
	 * @param toc
	 *        if true, send to TOC if possible (not possible for linear).
	 * @param instructions
	 *        if true, send to section instructions for first question.
	 * @param returnDestination
	 *        The final return destination path.
	 */
	protected void redirectToQuestion(HttpServletRequest req, HttpServletResponse res, Submission submission, boolean toc, boolean instructions,
			String returnDestination) throws IOException
	{
		String destination = null;
		Assessment assessment = submission.getAssessment();

		// if we are random access, and allowed, and more than a single question, send to TOC
		if (toc && assessment.getRandomAccess() && !assessment.getIsSingleQuestion())
		{
			destination = "/toc/" + submission.getId() + returnDestination;
		}

		else
		{
			// find the first incomplete question
			Question question = submission.getFirstIncompleteQuestion();

			// if not found, and we have only one, go there
			if ((question == null) && (assessment.getIsSingleQuestion()))
			{
				question = submission.getFirstQuestion();
			}

			// if we don't have one, we will go to the toc (or final_review for linear)
			if (question == null)
			{
				if (!assessment.getRandomAccess())
				{
					destination = "/final_review/" + submission.getId() + returnDestination;
				}
				else
				{
					destination = "/toc/" + submission.getId() + returnDestination;
				}
			}

			else
			{
				// send to the section instructions if it's a first question and by-question
				// and we are showing part presentation and we have something authored for this part
				if (instructions && (question.getPartOrdering().getIsFirst()) && (assessment.getParts().getShowPresentation())
						&& (!question.getPart().getPresentation().getIsEmpty()) && (assessment.getQuestionGrouping() == QuestionGrouping.question))
				{
					// to instructions
					destination = "/part_instructions/" + submission.getId() + "/" + question.getPart().getId() + returnDestination;
				}

				// or to the question
				else
				{
					if (assessment.getQuestionGrouping() == QuestionGrouping.question)
					{
						destination = "/question/" + submission.getId() + "/q" + question.getId() + "/-" + returnDestination;
					}
					else if (assessment.getQuestionGrouping() == QuestionGrouping.part)
					{
						destination = "/question/" + submission.getId() + "/p" + question.getPart().getId();

						// include the question target if not the first question in the section
						if (!question.getPartOrdering().getIsFirst())
						{
							destination = destination + "/" + question.getId();
						}
						else
						{
							destination = destination + "/-";
						}

						destination = destination + returnDestination;
					}
					else
					{
						destination = "/question/" + submission.getId() + "/a";

						// include the question target if not the first question in the assessment
						if (!question.getAssessmentOrdering().getIsFirst().booleanValue())
						{
							destination = destination + "/" + question.getId();
						}
						else
						{
							destination = destination + "/-";
						}

						destination = destination + returnDestination;
					}
				}
			}
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
		return;
	}
}
