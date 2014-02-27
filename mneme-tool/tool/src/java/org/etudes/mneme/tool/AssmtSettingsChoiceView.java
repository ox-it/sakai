/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1145/mneme-tool/tool/src/java/org/etudes/mneme/tool/AssmtSettingsChoiceView.java $
 * $Id: AssmtSettingsChoiceView.java 3635 2012-12-02 21:26:23Z ggolden $
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.Settings;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /assmt_settings_choice view for the mneme tool.
 */
public class AssmtSettingsChoiceView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssmtSettingsChoiceView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;
	
	/** Dependency: GradesService */
	protected GradesService gradesService = null;

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
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		Settings settings = (Settings) assessmentService.newEmptySettings();
		if (settings == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		// collect information: the selected assessment
		context.put("settings", settings);
		
		// we carry a sort for the /tests mode
		String sort = "";
		if (params.length == 4)
		{
			sort = params[2];
		}
		context.put("sort", sort);
		
		// if we have a focus parameter
		String focus = req.getParameter("focus");
		if (focus != null) context.addFocusId(focus);
		
		// check if we have gradebook
		context.put("gradebookAvailable", this.gradesService.available(toolManager.getCurrentPlacement().getContext()));
		
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
		if (params.length < 4)
		{
			throw new IllegalArgumentException();
		}

		String sort = params[2];
		String assmtIds = params[3];

		Settings settings = (Settings) assessmentService.newEmptySettings();
		if (settings == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// setup the model: the selected assessment
		context.put("settings", settings);

		// read the form
		String destination = uiService.decode(req, context);

		settings = (Settings) context.get("settings");

		boolean optionSet = false;

		StringBuilder buf = new StringBuilder();

		if (settings.getAllOptionsSetting())
		{
			buf.append("ao");
			optionSet = true;
		}
		else
		{
			if (settings.getAnonGradingSetting())
			{
				buf.append("ag");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getAutoEmailSetting())
			{
				buf.append("ae");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getFinalMessageSetting())
			{
				buf.append("fm");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getHintsSetting())
			{
				buf.append("hns");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getHonorPledgeSetting())
			{
				buf.append("hp");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getModelAnswerSetting())
			{
				buf.append("ma");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getNavlaySetting())
			{
				buf.append("navlay");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getPartNumberSetting())
			{
				buf.append("pn");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getPasswordSetting())
			{
				buf.append("pw");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getReleaseSubSetting())
			{
				buf.append("rs");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getAwardCertSetting())
			{
				buf.append("ac");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getReviewOptionsSetting())
			{
				buf.append("ro");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getSendGBSetting())
			{
				buf.append("sg");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getShuffleChoicesSetting())
			{
				buf.append("sc");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getTimeLimitSetting())
			{
				buf.append("tls");
				buf.append("+");
				optionSet = true;
			}
			if (settings.getTriesSetting())
			{
				buf.append("ts");
				buf.append("+");
				optionSet = true;
			}
		}
		if (optionSet)
		{
			if (buf.toString().endsWith("+")) buf.deleteCharAt(buf.length() - 1);
		}

		if ("CONTINUE".equals(destination))
		{
			if (optionSet)
				destination = "/multi_assmt_settings/" + sort + "/" + assmtIds + "/" + buf.toString();
			else
				destination = "/assessments/" + sort;
		}

		// if destination became null
		if (destination == null)
		{
			destination = context.getDestination();
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
	
	/**
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
}
}
