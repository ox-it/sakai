/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/2127issues/mneme-tool/tool/src/java/org/etudes/mneme/tool/AssessmentPrintView.java $
 * $Id: AssessmentPrintView.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /assessment_preview view for the mneme tool.
 */
public class AssessmentPrintView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentPrintView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

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
		// we need an aid, then any number of parameters to form the return destination
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

		// if not specified, go to the main assessment page
		else
		{
			destination = "/assessments";
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

		context.put("assessment", assessment);
		
		// format an invalid message
		if (!assessment.getIsValid())
		{
			context.put("invalidMsg", AssessmentInvalidView.formatInvalidDisplay(assessment, this.messages));
		}

		if (params.length == 4) context.put("testprintformat", params[3]);
		else context.put("testprintformat", "testonly");
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
		// we need an aid, then any number of parameters to form the return destination
		if (params.length < 2)
		{
			throw new IllegalArgumentException();
		}

		// read form
		String destination = uiService.decode(req, context);
		StringBuffer buf = new StringBuffer();
		buf.append(destination);
		buf.append("/" + req.getParameter("printformat"));
		destination = buf.toString();
		
		// go there!
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
