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
public class AssessmentPreviewView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentPreviewView.class);

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
		context.put("return", destination);

		// format an invalid message
		if (!assessment.getIsValid())
		{
			context.put("invalidMsg", AssessmentInvalidView.formatInvalidDisplay(assessment, this.messages));
		}

		// if coming from restore, offer prev/next based on the archived list
		if (destination.startsWith("/assessments_restore"))
		{
			figurePrevNext(context, destination, assessment, true);
		}

		// if coming from assessments, we offer prev/next
		// assessments/0A
		else if (destination.startsWith("/assessments"))
		{
			figurePrevNext(context, destination, assessment, false);
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
		// we need an aid, then any number of parameters to form the return destination
		if (params.length < 2)
		{
			throw new IllegalArgumentException();
		}

		// read form
		String destination = uiService.decode(req, context);

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

	/**
	 * Figure the next and prev when coming from pool edit
	 * 
	 * @param context
	 *        The context.
	 * @param destination
	 *        The return path.
	 * @param question
	 *        The question.
	 */
	protected void figurePrevNext(Context context, String destination, Assessment assessment, boolean restore)
	{
		List<Assessment> assessments = null;

		// if from the /assessments view
		if (!restore)
		{
			// Note: must match the parameter and sort logic of AssessmentsView
			// /assessments/0A
			String[] params = StringUtil.split(destination, "/");

			// default is due date, ascending
			String sortCode = (params.length > 2) ? params[2] : "0A";
			if (sortCode.length() != 2) return;

			AssessmentService.AssessmentsSort sort = AssessmentsView.figureSort(sortCode);

			// collect the assessments in this context
			assessments = this.assessmentService.getContextAssessments(this.toolManager.getCurrentPlacement().getContext(), sort, Boolean.FALSE);
		}

		// if from assessments_restore view
		else
		{
			assessments = this.assessmentService.getArchivedAssessments(this.toolManager.getCurrentPlacement().getContext());
		}

		// figure this one's position (0 based)
		int position = 0;
		for (Assessment a : assessments)
		{
			if (a.equals(assessment)) break;
			position++;
		}

		// figure prev and next, w/ wrap
		Assessment prev = null;
		if (position > 0)
		{
			prev = assessments.get(position - 1);
		}
		else
		{
			prev = assessments.get(assessments.size() - 1);
		}

		Assessment next = null;
		if (position < assessments.size() - 1)
		{
			next = assessments.get(position + 1);
		}
		else
		{
			next = assessments.get(0);
		}

		if (prev != null) context.put("prevAssessmentId", prev.getId());
		if (next != null) context.put("nextAssessmentId", next.getId());

		context.put("position", Integer.valueOf(position + 1));
		context.put("size", Integer.valueOf(assessments.size()));
	}
}
