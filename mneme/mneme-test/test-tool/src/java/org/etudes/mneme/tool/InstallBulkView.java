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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.db.api.SqlService;

/**
 * The /install_bulk view for the mneme admin tool.
 */
public class InstallBulkView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(InstallBulkView.class);

	/** Dependency: the InstallView. */
	protected InstallView installView = null;

	/** The security service. */
	protected SecurityService securityService = null;

	/** The sql service. */
	protected SqlService sqlService = null;

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

		// one parameter expected
		if (params.length != 3)
		{
			throw new IllegalArgumentException();
		}

		String siteTitlePattern = params[2];

		// convert "*" to the db wildcard, %
		siteTitlePattern = siteTitlePattern.replaceAll("\\*", "%");

		// do the install
		String rv = installBulk(siteTitlePattern);

		context.put("rv", rv);

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
		throw new IllegalArgumentException();
	}

	/**
	 * Set the InstallView.
	 * 
	 * @param view
	 *        The InstallView.
	 */
	public void setInstallView(InstallView view)
	{
		this.installView = view;
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

	/**
	 * Set the sql service.
	 * 
	 * @param service
	 *        The sql serivce.
	 */
	public void setSqlService(SqlService service)
	{
		this.sqlService = service;
	}

	/**
	 * Add Mneme to all the sites that meet the site title pattern.
	 * 
	 * @param siteTitlePattern
	 *        A where site.title like string for selecting the sites. "*" means all.
	 */
	protected String installBulk(String siteTitlePattern)
	{
		StringBuffer rv = new StringBuffer();
		rv.append("Installing Test Center:<br />");

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT S.SITE_ID ");
		sql.append(" FROM SAKAI_SITE S ");
		sql.append(" WHERE S.TITLE LIKE ?");

		Object[] fields = new Object[1];
		fields[0] = siteTitlePattern;

		List sites = sqlService.dbRead(sql.toString(), fields, null);

		// for each one, install
		for (Iterator i = sites.iterator(); i.hasNext();)
		{
			String site = (String) i.next();
			String res = this.installView.installMneme(site);
			rv.append(res);
			rv.append("<br />");
		}

		return rv.toString();
	}
}
