/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.Ent;
import org.etudes.mneme.api.ImportService;
import org.etudes.mneme.api.PoolService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;

/**
 * The /import_assignment view for the mneme tool.
 */
public class ImportAssignmentView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ImportAssignmentView.class);

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
		// [2] pools sort, [3] source context
		if (params.length != 4)
		{
			throw new IllegalArgumentException();
		}
		String poolsSort = params[2];
		context.put("poolsSort", poolsSort);
		String sourceContext = params[3];

		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// the list of importable assignments for this site
		List<Ent> assessments = this.importService.getAssignments(sourceContext);
		context.put("assignments", assessments);

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
		// [2] pools sort, [3] source context
		if (params.length != 4)
		{
			throw new IllegalArgumentException();
		}
		String poolsSort = params[2];
		String sourceContext = params[3];

		String toolContext = toolManager.getCurrentPlacement().getContext();

		// if the source is the same site as this one, we will draftSource when we import
		boolean draftSource = (sourceContext.equals(toolContext));

		if (!this.poolService.allowManagePools(toolContext))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		Values selectedPools = this.uiService.newValues();
		context.put("selectedAssignments", selectedPools);

		// read the form
		String destination = uiService.decode(req, context);

		// import the pools
		if ("IMPORT".equals(destination))
		{
			for (String id : selectedPools.getValues())
			{
				try
				{
					this.importService.importAssignment(id, toolContext, draftSource);
				}
				catch (AssessmentPermissionException e)
				{
					// redirect to error
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
					return;
				}
			}

			destination = "/pools/" + poolsSort;
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
