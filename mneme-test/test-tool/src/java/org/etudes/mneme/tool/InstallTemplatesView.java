/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;

/**
 * The /install_templates view for the mneme admin tool.
 */
public class InstallTemplatesView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(InstallTemplatesView.class);

	/** The site service. */
	protected static AuthzGroupService azgService = null;

	/** The tool manager. */
	protected static ToolManager toolManager = null;

	/** Dependency: the InstallView. */
	protected InstallView installView = null;

	/** The security service. */
	protected SecurityService securityService = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService serverConfigurationService = null;

	/** Configuration: the list of standard site templates. */
	protected List<String> templateIds = new ArrayList<String>();

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

		// do the installs
		StringBuilder rv = new StringBuilder();
		for (String id : this.templateIds)
		{
			String result = installMnemeTemplate(id);
			rv.append(result);
		}

		context.put("rv", rv.toString());

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		// configure the templates
		String templates = StringUtil.trimToNull(this.serverConfigurationService.getString("templates@org.muse.mneme.tool.InstallTemplatesView"));
		if (templates != null) setTemplates(templates);
		if (this.templateIds.isEmpty())
		{
			setTemplates("!site.template,!site.template.course,!site.template.project");
		}

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
	 * Set the AuthzGroup.
	 * 
	 * @param service
	 *        the AuthzGroup.
	 */
	public void setAuthzGroupService(AuthzGroupService service)
	{
		this.azgService = service;
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
	 * Set the template ids with a comma separated list of the standard site azg templates.
	 * 
	 * @param ids
	 *        The comma separated list of the standard site azg templates.
	 */
	public void setTemplates(String ids)
	{
		this.templateIds.clear();

		String[] parts = StringUtil.split(ids, ",");
		for (String id : parts)
		{
			this.templateIds.add(id);
		}
	}

	/**
	 * Set the tool manager.
	 * 
	 * @param service
	 *        The tool manager.
	 */
	public void setToolManager(ToolManager service)
	{
		this.toolManager = service;
	}

	/**
	 * Add Mneme permissions to the named authzGroup.
	 * 
	 * @param azgId
	 *        The authz group id.
	 */
	protected String installMnemeTemplate(String azgId)
	{
		try
		{
			AuthzGroup azg = azgService.getAuthzGroup(azgId);

			// add permissions to realm
			for (Iterator i = azg.getRoles().iterator(); i.hasNext();)
			{
				Role role = (Role) i.next();
				if (this.installView.maintainRole(role.getId()))
				{
					role.allowFunction("mneme.manage");
					role.allowFunction("mneme.grade");
				}
				else if (this.installView.accessRole(role.getId()))
				{
					role.allowFunction("mneme.submit");
				}
			}

			// save the site
			azgService.save(azg);

			return "Test Center installed in template " + azgId + "<br />";
		}
		catch (GroupNotDefinedException e)
		{
			return e.toString();
		}
		catch (AuthzPermissionException e)
		{
			return e.toString();
		}
	}
}
