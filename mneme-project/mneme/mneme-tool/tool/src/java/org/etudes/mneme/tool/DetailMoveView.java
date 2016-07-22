/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Part;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /detail_move view for the mneme tool.
 */
public class DetailMoveView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(DetailMoveView.class);

	/** Dependency: AssessmentService. */
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
		// we need a aid[2], detail ids[3], then any number of parameters to form the return destination
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		String destination = null;
		if (params.length > 4)
		{
			destination = "/" + StringUtil.unsplit(params, 4, params.length - 4, "/");
		}

		// if not specified, go to the main pools page
		else
		{
			destination = "/pools";
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

		// security check
		if (!assessmentService.allowEditAssessment(assessment))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// parts
		List<Part> parts = assessment.getParts().getParts();
		context.put("parts", parts);

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
		// we need a aid[2], detail ids[3], then any number of parameters to form the return destination
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		String returnDest = null;
		if (params.length > 4)
		{
			returnDest = "/" + StringUtil.unsplit(params, 4, params.length - 4, "/");
		}

		// if not specified, go to the main pools page
		else
		{
			returnDest = "/pools";
		}

		String assessmentId = params[2];
		String detailIds = params[3];

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

		// for the selected part
		Value value = this.uiService.newValue();
		context.put("selectedPartId", value);

		// read form
		String destination = this.uiService.decode(req, context);

		if (destination.equals("MOVE"))
		{
			String selectedPartId = value.getValue();
			if (selectedPartId != null)
			{
				try
				{
					Part selectedPart = assessment.getParts().getPart(selectedPartId);
					if (selectedPart != null)
					{
						String dIds[] = StringUtil.split(detailIds, "+");
						assessment.getParts().moveDetails(dIds, selectedPart);
						this.assessmentService.saveAssessment(assessment);
					}
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
				// back to where we came from
				destination = returnDest;
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
}
