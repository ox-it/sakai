/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 Etudes, Inc.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.Ent;
import org.etudes.mneme.api.ImportService;
import org.etudes.mneme.api.PoolService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /import_mneme view for the mneme tool.
 */
public class ImportMnemeView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ImportMnemeView.class);

	/** Dependency: ImportService */
	protected ImportService importService = null;

	/** Pool Service */
	protected PoolService poolService = null;

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
		// [2] source context - rest the return destination
		if (params.length < 3)
		{
			throw new IllegalArgumentException();
		}
		String sourceContext = params[2];
		String destination = null;

		if (params.length > 3)
		{
			destination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");
		}

		// if not specified, go to the main assessments page
		else
		{
			destination = "/assessments";
		}
		context.put("return", destination);

		// TODO: change to assessment service ...
		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// the list of assessments for this site
		List<Ent> assessments = this.importService.getAssessments(sourceContext, toolManager.getCurrentPlacement().getContext());
		context.put("assessments", assessments);

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
		// [2] source context - rest the return destination
		if (params.length < 3)
		{
			throw new IllegalArgumentException();
		}
		String sourceContext = params[2];
		String returnDestination = null;

		if (params.length > 3)
		{
			returnDestination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");
		}

		// if not specified, go to the main assessments page
		else
		{
			returnDestination = "/assessments";
		}

		String toolContext = toolManager.getCurrentPlacement().getContext();

		// TODO: change to assessment service ...
		if (!this.poolService.allowManagePools(toolContext))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		Values selectedAssessments = this.uiService.newValues();
		context.put("selectedAssessments", selectedAssessments);

		// read the form
		String destination = uiService.decode(req, context);

		// import the assessments
		if ("IMPORT".equals(destination))
		{
			Set<String> assessmentIds = new HashSet<String>();
			for (String id : selectedAssessments.getValues())
			{
				assessmentIds.add(id);
			}

			try
			{
				this.importService.importMneme(assessmentIds, sourceContext, toolContext);
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}

			destination = returnDestination;
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the ImportService
	 * 
	 * @param service
	 *        the ImportService.
	 */
	public void setImportService(ImportService service)
	{
		this.importService = service;
	}

	/**
	 * @param poolService
	 *        the poolService to set
	 */
	public void setPoolService(PoolService poolService)
	{
		this.poolService = poolService;
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
