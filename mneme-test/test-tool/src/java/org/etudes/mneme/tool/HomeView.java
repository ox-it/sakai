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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.util.Web;

/**
 * The /home view for the mneme admin tool.
 */
public class HomeView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(HomeView.class);

	/** Dependency: SecurityService. */
	protected SecurityService securityService = null;

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
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
	{
		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			throw new IllegalArgumentException();
		}

		// no parameters expected
		if (params.length != 2)
		{
			throw new IllegalArgumentException();
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
		if (!context.getPostExpected())
		{
			throw new IllegalArgumentException();
		}

		// no parameters expected
		if (params.length != 2)
		{
			throw new IllegalArgumentException();
		}

		// read form
		Value installValue = this.uiService.newValue();
		context.put("installValue", installValue);
		Value installBulkValue = this.uiService.newValue();
		context.put("installBulkValue", installBulkValue);

		String destination = uiService.decode(req, context);

		if ("INSTALL".equals(destination))
		{
			if (installValue.getValue() != null)
			{
				// add the specs
				destination = "/install/" + installValue.getValue();
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
				return;
			}
		}

		else if ("INSTALL_BULK".equals(destination))
		{
			if (installBulkValue.getValue() != null)
			{
				// add the specs
				destination = "/install_bulk/" + installBulkValue.getValue();
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
				return;
			}
		}

		else if ("QUESTION_CLEANUP".equals(destination))
		{
			destination = "/question_cleanup";
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
			return;
		}

		else if ("INSTALL_TEMPLATES".equals(destination))
		{
			destination = "/install_templates";
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
			return;
		}

		destination = "/home";
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the security service.
	 * 
	 * @param service
	 *        The security service.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}
}
