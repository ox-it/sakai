/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Part;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.Web;

/**
 * The /assessment_invalid view for the mneme tool.
 */
public class AssessmentInvalidView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentInvalidView.class);

	/**
	 * Format an invalid message as a
	 * <ul>
	 * for this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return The invalid display for this assessment.
	 */
	public static String formatInvalidDisplay(Assessment assessment, InternationalizedMessages msgs)
	{
		// what is invalid?
		StringBuilder msg = new StringBuilder();
		msg.append("<ul>");
		if (!assessment.getIsValid())
		{
			// could be title
			if (assessment.getTitle().length() == 0)
			{
				msg.append("<li>" + msgs.getString("missing-title") + "</li>");
			}

			// could be grading
			if (!assessment.getGrading().getIsValid())
			{
				msg.append("<li>" + msgs.getString("invalid-grading") + "</li>");
			}

			// could be dates
			if (!assessment.getDates().getIsValid())
			{
				msg.append("<li>" + msgs.getString("invalid-dates") + "</li>");
			}

			// could be parts
			if (!assessment.getParts().getIsValid())
			{
				// if no parts
				if (assessment.getParts().getParts().isEmpty())
				{
					msg.append("<li>" + msgs.getString("invalid-parts") + "</li>");
				}

				// could be a specific part
				for (Part part : assessment.getParts().getParts())
				{
					if (!part.getIsValid())
					{
						msg.append("<li>" + part.getInvalidMessage() + "</li>");
					}
				}
			}

			// could be points
			if (!assessment.getIsPointsValid())
			{
				msg.append("<li>" + msgs.getString("invalid-points") + "</li>");
			}

			// could be formal course evaluation
			if (assessment.getFormalCourseEval())
			{
				if (assessment.getResultsEmail() == null)
				{
					msg.append("<li>" + msgs.getString("formal-needs-email") + "</li>");
				}
			}
			
			// could be missing dates with auto-send
			if (assessment.getResultsEmail() != null)
			{
				if ((assessment.getDates().getDueDate() == null) && (assessment.getDates().getAcceptUntilDate() == null))
				{
					msg.append("<li>" + msgs.getString("email-results-needs-dates") + "</li>");
				}
			}
		}

		msg.append("</ul>");

		return msg.toString();
	}

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

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
		// sort code, aid
		if (params.length != 4)
		{
			throw new IllegalArgumentException();
		}

		String sortCode = params[2];
		String assessmentId = params[3];

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

		context.put("message", formatInvalidDisplay(assessment, this.messages));
		context.put("assessment", assessment);
		context.put("sortcode", sortCode);

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
		if (params.length != 4)
		{
			throw new IllegalArgumentException();
		}

		// read the form
		String destination = uiService.decode(req, context);

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
}
