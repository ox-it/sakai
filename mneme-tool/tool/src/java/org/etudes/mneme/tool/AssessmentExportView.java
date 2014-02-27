/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/AssessmentExportView.java $
 * $Id: AssessmentExportView.java 6527 2013-12-06 23:01:08Z rashmim $
 ***********************************************************************************
 *
 * Copyright (c) 2013 Etudes, Inc.
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
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AttachmentService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /question_copy view for the mneme tool.
 */
public class AssessmentExportView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentExportView.class);

	/** Dependency: ToolManager */
	protected ToolManager toolManager = null;

	private AssessmentService assessmentService = null;

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
		//[, assessment_export, 1679] - params

		String assessmentIds = params[2];
		
		// security
		if (!this.assessmentService.allowManageAssessments(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		if (assessmentIds == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		String[] ids = StringUtil.split(assessmentIds, "+");
		List<Assessment> assessments = new ArrayList<Assessment>();

		for (String aId : ids)
		{
			Assessment assessment = this.assessmentService.getAssessment(aId);
			if (assessment != null) assessments.add(assessment);
		}
		context.put("assessments", assessments);

		//exportZipName
		String zipTitle;
		String siteId = null;
		try
		{
			siteId = toolManager.getCurrentPlacement().getContext();
			zipTitle = SiteService.getSite(siteId).getTitle();
		}
		catch (IdUnusedException e)
		{
			zipTitle = "ExportAssessments";
		}
		Integer countAssessments = this.assessmentService.countAssessments(siteId);
		if (countAssessments.intValue() == ids.length)
			zipTitle = zipTitle + "_allAssessments.zip";
		else
			zipTitle = zipTitle + "_someAssessments.zip";

		Reference ref = EntityManager.newReference("/mneme/" + AttachmentService.DOWNLOAD + "/" + AttachmentService.ASMT_EXPORT
				+ "/" + siteId + "/" + assessmentIds +".zip");
		
		context.put("exportZipName", ref);		
		
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
		// read form
		String destination = this.uiService.decode(req, context);
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
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}
}
