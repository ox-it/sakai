/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
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

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.ImportQti2Service;
import org.etudes.mneme.api.ImportQtiService;
import org.etudes.mneme.api.PoolService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;
import org.w3c.dom.Document;

/**
 * The /import_qti view for the mneme tool.
 */
public class ImportQtiView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ImportQtiView.class);

	/** Dependency: ImportQtiService */
	protected ImportQtiService importQtiService = null;
	
	/** Dependency: ImportQtiService */
	protected ImportQti2Service importQti2Service = null;

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

		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		String returnUrl = (params.length > 3) ? params[2] : "";
		String sort = (params.length > 3) ? params[3] : "0A";
		
		context.put("returnUrl", returnUrl);
		context.put("sort", sort);
		
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
		
		String returnUrl = (params.length > 3) ? params[2] : "";
		String sort = (params.length > 3) ? params[3] : "0A";
		
		if (!this.poolService.allowManagePools(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// an XML uploader for the QTI file
		UploadXml upload = new UploadXml();
		context.put("upload", upload);

		// read the form
		String destination = uiService.decode(req, context);

		// import the pools
		if ("IMPORT".equals(destination))
		{
			// the DOM is in the upload
			Document doc = upload.getUpload();

			try
			{
				String unzipBackUpLocation = upload.getUnzipLocation();
				if ("".equals(unzipBackUpLocation))
				{
					this.importQtiService.importPool(doc, toolManager.getCurrentPlacement().getContext());
				}
				else
				{
					//QTI 1 zip file 
					boolean doneQTI1 = false;
					doneQTI1 = this.importQtiService.importPool(doc, toolManager.getCurrentPlacement().getContext(), unzipBackUpLocation);
					// QTI 2 zip file
					if (!doneQTI1) this.importQti2Service.importPool(doc, toolManager.getCurrentPlacement().getContext(), unzipBackUpLocation);
					upload.deleteFiles(new File(unzipBackUpLocation));
				}
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}		
			destination = "/" + returnUrl + "/" + sort;
		}
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
		
	}

	/**
	 * Set the ImportQtiService
	 * 
	 * @param service
	 *        the ImportQtiService.
	 */
	public void setImportQtiService(ImportQtiService service)
	{
		this.importQtiService = service;
	}

	/**
	 * Set the ImportQti2Service
	 * 
	 * @param service
	 *        the ImportQti2Service.
	 */
	public void setImportQti2Service(ImportQti2Service importQti2Service)
	{
		this.importQti2Service = importQti2Service;
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
