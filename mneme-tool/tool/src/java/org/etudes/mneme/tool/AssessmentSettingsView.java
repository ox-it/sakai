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
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /assessment_settings view for the mneme tool.
 */
public class AssessmentSettingsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentSettingsView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Dependency: GradesService */
	protected GradesService gradesService = null;

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
		// aid and return
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

		// if not specified, go to the main assessment view
		else
		{
			destination = "/assessments";
		}
		context.put("return", destination);

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

		// check for formal course evaluation permission
		if (assessmentService.allowSetFormalCourseEvaluation(assessment.getContext()))
		{
			context.put("allowEval", Boolean.TRUE);
		}

		// format an invalid message
		if ((!assessment.getIsValid()) && (!assessment.getPublished()))
		{
			context.put("invalidMsg", AssessmentInvalidView.formatInvalidDisplay(assessment, this.messages));
		}

		// format part list of zero parts
		if (assessment.getHasPoints())
		{
			boolean showWarning = false;
			StringBuilder buf = new StringBuilder("<ul>");
			Object args[] = new Object[1];
			for (Part part : assessment.getParts().getParts())
			{
				// for valid, 0 point parts that are not all survey questions (i.e. that have details that support points)
				if ((part.getTotalPoints().floatValue() == 0f) && (part.getIsValid()))
				{
					for (PartDetail detail : part.getDetails())
					{
						if (detail.getHasPoints())
						{
							args[0] = part.getTitle();
							if (args[0] == null) args[0] = part.getOrdering().getPosition().toString();
							buf.append("<li>" + this.messages.getFormattedMessage("part", args) + "</li>");
							showWarning = true;
							break;
						}
					}
				}
			}

			if (showWarning)
			{
				buf.append("</ul>");
				context.put("zeroMsg", buf.toString());
			}
		}

		// collect information: the selected assessment
		context.put("assessment", assessment);

		// check if we have gradebook
		context.put("gradebookAvailable", this.gradesService.available(assessment.getContext()));

		// if we have a focus parameter
		String focus = req.getParameter("focus");
		if (focus != null) context.addFocusId(focus);

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
		// aid and return
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

		// if not specified, go to the main assessment view
		else
		{
			returnDestination = "/assessments";
		}

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

		// setup the model: the selected assessment
		context.put("assessment", assessment);

		// read the form
		String destination = uiService.decode(req, context);

		boolean sendResultsEmail = false;

		// if publish, set
		if ("PUBLISH".equals(destination))
		{
			assessment.setPublished(Boolean.TRUE);
			destination = returnDestination;
		}

		else if ("SAVE".equals(destination))
		{
			destination = context.getDestination();
		}

		else if (destination.equals("SURVEY"))
		{
			// change to survey
			assessment.setType(AssessmentType.survey);

			destination = context.getDestination();
		}

		else if (destination.equals("SEND"))
		{
			// we will do it later, after we save
			sendResultsEmail = true;

			destination = context.getDestination();
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

		// if we need to send the results email, do so after the save
		if (sendResultsEmail)
		{
			this.assessmentService.sendResults(assessment);
		}

		// if destination became null
		if (destination == null)
		{
			destination = context.getDestination();
		}

		// if destination is stay here
		else if (destination.startsWith("STAY:"))
		{
			String[] parts = StringUtil.splitFirst(destination, ":");
			destination = context.getDestination() + "?focus=" + parts[1];
		}

		// redirect to the next destination
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
	 * Set the GradesService.
	 * 
	 * @param service
	 *        The GradesService.
	 */
	public void setGradesService(GradesService service)
	{
		this.gradesService = service;
	}
}
