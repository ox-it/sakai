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
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /assessment_access view for the mneme tool.
 */
public class AssessmentAccessView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentAccessView.class);

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
		// we need 3 parameters: sort, aid, access id - all else is a return url
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}
		String sort = params[2];
		String assessmentId = params[3];
		String accessId = params[4];

		// get the assessment
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

		// if access id is actually a user id, find the access that is for this user (only)
		AssessmentAccess access = null;
		if (accessId.startsWith("USER:"))
		{
			String[] parts = StringUtil.splitFirst(accessId, ":");
			access = assessment.getSpecialAccess().assureUserAccess(parts[1]);

			// this may have altered the assessment - save
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

			// don't let the user be changed
			context.put("fixed_user", parts[1]);
		}
		else
		{
			access = assessment.getSpecialAccess().getAccess(accessId);
		}

		if (access == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// setup the model
		context.put("assessment", assessment);
		context.put("access", access);
		context.put("sort", sort);

		// return
		String destination = null;
		if (params.length > 5)
		{
			destination = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
		}
		else
		{
			destination = "/assessment_special/" + sort + "/" + assessment.getId();
		}
		context.put("return", destination);

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
		// we need 3 parameters: sort, aid, access id
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}
		String sort = params[2];
		String assessmentId = params[3];
		String accessId = params[4];

		// get the assessment
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

		// if access id is actually a user id, find the access that is for this user (only),
		AssessmentAccess access = null;
		if (accessId.startsWith("USER:"))
		{
			String[] parts = StringUtil.splitFirst(accessId, ":");
			access = assessment.getSpecialAccess().assureUserAccess(parts[1]);
		}
		else
		{
			access = assessment.getSpecialAccess().getAccess(accessId);
		}
		if (access == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// setup the model
		context.put("access", access);

		// read the form
		String destination = uiService.decode(req, context);

		if ("DELETE".equals(destination))
		{
			if (params.length > 5)
			{
				destination = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
			}
			else
			{
				destination = "/assessment_special/" + sort + "/" + assessment.getId();
			}

			// delete
			assessment.getSpecialAccess().removeAccess(access);
		}

		// save
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
