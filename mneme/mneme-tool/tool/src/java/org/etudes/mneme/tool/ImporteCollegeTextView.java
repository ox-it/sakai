/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/ImporteCollegeTextView.java $
 * $Id: ImporteCollegeTextView.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2012 Etudes, Inc.
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
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.ImporteCollegeTextService;
import org.etudes.mneme.api.PoolService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /import_eCollege_text view for the mneme tool.
 */
public class ImporteCollegeTextView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ImporteCollegeTextView.class);

	/** Dependency: ImportTextService */
	protected ImporteCollegeTextService importeCollegeTextService = null;

	/** Configuration: the instructions URL. */
	protected String instructionsUrl = null;

	/** Pool Service */
	protected PoolService poolService = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService serverConfigurationService = null;

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
		// [2] pools sort
		if (params.length != 3)
		{
			throw new IllegalArgumentException();
		}
		String poolsSort = params[2];
		context.put("poolsSort", poolsSort);
		if (this.instructionsUrl != null) context.put("instructionsUrl", this.instructionsUrl);

		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
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
		this.instructionsUrl = StringUtil.trimToNull(this.serverConfigurationService
				.getString("questionPasteInstructionUrl@org.etudes.mneme.tool.ImporteCollegeTextView"));

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2] pools sort
		if (params.length != 3)
		{
			throw new IllegalArgumentException();
		}
		String poolsSort = params[2];

		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// for the text
		Value textValue = this.uiService.newValue();
		context.put("text", textValue);

		// read the form
		String destination = uiService.decode(req, context);

		// the text
		String text = textValue.getValue();

		// import the pools
		if ("IMPORT".equals(destination))
		{
			try
			{
				this.importeCollegeTextService.importQuestions(toolManager.getCurrentPlacement().getContext(), null, text);
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}

			destination = "/pools/" + poolsSort;
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the ImportTextService
	 * 
	 * @param service
	 *        the ImportQtiService.
	 */
	public void setImporteCollegeTextService(ImporteCollegeTextService service)
	{
		this.importeCollegeTextService = service;
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
	 * Set the ServerConfigurationService.
	 * 
	 * @param service
	 *        the ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		this.serverConfigurationService = service;
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
