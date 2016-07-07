/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/ConfirmGradesImportView.java $
 * $Id: ConfirmGradesImportView.java 9578 2014-12-18 03:27:47Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /confirm_grades_import view for the mneme tool.
 */
public class ConfirmGradesImportView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ConfirmGradesImportView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

	/** Configuration: the page sizes for the view. */
	protected List<Integer> pageSizes = new ArrayList<Integer>();

	/** Submission Service */
	protected SubmissionService submissionService = null;

	/** Dependency: ToolManager */
	protected ToolManager toolManager = null;

	/** UserDirectoryService */
	protected UserDirectoryService userDirectoryService = null;

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
	@SuppressWarnings("unchecked")
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// check for user permission to access the assessments for grading
		if (!this.submissionService.allowEvaluate(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		ToolSession toolSession = m_sessionManager.getCurrentToolSession();
		List<GradeImportSet> importSets = (List<GradeImportSet>) toolSession.getAttribute(GradeImportSet.ATTR_NAME);
		if (importSets == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// return destination - or the main assessments view if not specified
		String destination = null;
		if (params.length > 2)
		{
			int len = params.length - 2;
			destination = "/" + StringUtil.unsplit(params, 2, len, "/");
		}
		else
		{
			destination = "/assessments";
		}
		context.put("return", destination);

		context.put("targets", importSets);

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
	@SuppressWarnings("unchecked")
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// check for user permission to access the assessments for grading
		if (!this.submissionService.allowEvaluate(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// return destination - or the main assessments view if not specified
		String destination = null;
		if (params.length > 2)
		{
			int len = params.length - 2;
			destination = "/" + StringUtil.unsplit(params, 2, len, "/");
		}
		else
		{
			destination = "/assessments";
		}

		// get and clear the sets stored in the tool state
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();
		List<GradeImportSet> importSets = (List<GradeImportSet>) toolSession.getAttribute(GradeImportSet.ATTR_NAME);
		toolSession.removeAttribute(GradeImportSet.ATTR_NAME);

		if (importSets == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		context.put("targets", importSets);

		// read form
		String outcome = this.uiService.decode(req, context);

		if (outcome.equals("IMPORT"))
		{
			// process each set
			for (GradeImportSet set : importSets)
			{
				// if selected for import
				if (!set.getSelected()) continue;

				// create if needed
				if (set.assessment == null)
				{
					Assessment assessment = null;
					try
					{
						assessment = this.assessmentService.newAssessment(toolManager.getCurrentPlacement().getContext());
						assessment.setTitle(set.assessmentTitle);
						assessment.setType(AssessmentType.offline);
						if (set.points != null)
						{
							assessment.setPoints(set.points);
							assessment.setPublished(Boolean.TRUE);
						}
						this.assessmentService.saveAssessment(assessment);
						set.assessment = assessment;
					}
					catch (AssessmentPermissionException e)
					{
					}
					catch (AssessmentPolicyException e)
					{
					}
				}

				if (set.assessment == null) continue;

				// ignore any not qualified for import
				if (set.assessment.getFormalCourseEval() || set.assessment.getType() == AssessmentType.survey || !set.assessment.getHasPoints())
					continue;

				// get the assesssment's submissions
				List<Submission> subs = this.submissionService.findAssessmentSubmissions(set.assessment, null, true, null, null, null, null);

				for (GradeImport r : set.rows)
				{
					if (r.userId == null) continue;

					try
					{
						User user = this.userDirectoryService.getUser(r.userId);

						// find the submission
						Submission forUser = null;
						for (Submission submission : subs)
						{
							if (submission.getUserId().equals(user.getId()))
							{
								forUser = submission;
								break;
							}
						}

						if (forUser != null)
						{
							try
							{
								forUser.setTotalScore(r.score);

								// save
								try
								{
									this.submissionService.evaluateSubmission(forUser);
								}
								catch (AssessmentPermissionException e)
								{
								}
							}
							catch (NumberFormatException e)
							{
							}
						}
					}
					catch (UserNotDefinedException e)
					{
					}
				}
			}
		}

		else
		{
			destination = outcome;
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * @param assessmentService
	 *        the assessmentService to set
	 */
	public void setAssessmentService(AssessmentService assessmentService)
	{
		this.assessmentService = assessmentService;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		m_sessionManager = service;
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
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	/**
	 * @param userDirectoryService
	 *        the userDirectoryService to set
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}
}
