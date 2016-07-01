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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Ent;
import org.etudes.mneme.api.ImportService;
import org.etudes.mneme.api.PoolService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /import_mneme_site view for the mneme tool.
 */
public class ImportMnemeSiteView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ImportTqSiteView.class);

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
		String destination = null;
		if (params.length > 2)
		{
			destination = "/" + StringUtil.unsplit(params, 2, params.length - 2, "/");
		}

		// if not specified, go to the main assessments page
		else
		{
			destination = "/assessments";
		}
		context.put("return", destination);

		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// the list of site for this user with Mneme access
		List<Ent> sites = this.importService.getMnemeSites(null, toolManager.getCurrentPlacement().getContext());
		context.put("sites", sites);

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
		String returnDestination = null;
		if (params.length > 2)
		{
			returnDestination = "/" + StringUtil.unsplit(params, 2, params.length - 2, "/");
		}

		// if not specified, go to the main assessments page
		else
		{
			returnDestination = "/assessments";
		}

		// TODO: change to assessment service ...
		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		Value selectedSite = this.uiService.newValue();
		context.put("selectedSite", selectedSite);

		// read the form
		String destination = uiService.decode(req, context);

		// import the pools
		if ("IMPORT".equals(destination))
		{
			String siteId = selectedSite.getValue();
			destination = "/import_mneme/" +  siteId + "/" + returnDestination;
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
