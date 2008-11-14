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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;

/**
 * The /install view for the mneme admin tool.
 */
public class InstallView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(InstallView.class);

	/** The site service. */
	protected static SiteService siteService = null;

	/** The tool manager. */
	protected static ToolManager toolManager = null;

	/** Configuration: the list of standard access class role names. */
	protected List<String> accessRoles = new ArrayList<String>();

	/** Configuration: the list of standard maintain class role names. */
	protected List<String> maintainRoles = new ArrayList<String>();

	/** The security service. */
	protected SecurityService securityService = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService serverConfigurationService = null;

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

		String contextId = params[2];

		// do the install
		String rv = installMneme(contextId);

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

		// configure the access roles
		String roles = StringUtil.trimToNull(this.serverConfigurationService.getString("accessRoles@org.muse.mneme.tool.InstallView"));
		if (roles != null) setAccessRoles(roles);
		if (this.accessRoles.isEmpty())
		{
			setAccessRoles("access,student");
		}

		// configure the maintain roles
		roles = StringUtil.trimToNull(this.serverConfigurationService.getString("maintainRoless@org.muse.mneme.tool.InstallView"));
		if (roles != null) setMaintainRoles(roles);
		if (this.maintainRoles.isEmpty())
		{
			setMaintainRoles("maintain,instructor,teaching assistant");
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
	 * Set the access class role names with a comma separated list.
	 * 
	 * @param ids
	 *        The comma separated list of the standard access class role names.
	 */
	public void setAccessRoles(String ids)
	{
		this.accessRoles.clear();

		String[] parts = StringUtil.split(ids, ",");
		for (String id : parts)
		{
			this.accessRoles.add(id.toLowerCase());
		}
	}

	/**
	 * Set the maintain class role names with a comma separated list.
	 * 
	 * @param ids
	 *        The comma separated list of the standard maintain class role names.
	 */
	public void setMaintainRoles(String ids)
	{
		this.maintainRoles.clear();

		String[] parts = StringUtil.split(ids, ",");
		for (String id : parts)
		{
			this.maintainRoles.add(id.toLowerCase());
		}
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
	 * Set the site service.
	 * 
	 * @param service
	 *        the site service.
	 */
	public void setSiteService(SiteService service)
	{
		this.siteService = service;
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
	 * Is the roleId a access class role?
	 * 
	 * @param roleId
	 *        The role id.
	 * @return true if this is a access class role, false if not.
	 */
	protected boolean accessRole(String roleId)
	{
		if (this.accessRoles.contains(roleId.toLowerCase())) return true;
		return false;
	}

	/**
	 * Add Mneme to the named context.
	 * 
	 * @param context
	 *        The context id.
	 */
	protected String installMneme(String context)
	{
		if (siteService.isSpecialSite(context) || "!admin".equals(context))
		{
			return "Site " + context + " is special - skipping.";
		}
		if (siteService.isUserSite(context))
		{
			return "Site " + context + " is a myWorkspace - skipping.";
		}

		// get the Test Center tool
		Tool tcTool = toolManager.getTool("sakai.mneme");

		try
		{
			Site site = siteService.getSite(context);

			// find the site page with Mneme already
			boolean mnemeFound = false;
			for (Iterator i = site.getPages().iterator(); i.hasNext();)
			{
				SitePage page = (SitePage) i.next();
				String[] mnemeToolIds = {"sakai.mneme"};
				Collection mnemeTools = page.getTools(mnemeToolIds);
				if (!mnemeTools.isEmpty())
				{
					mnemeFound = true;
					break;
				}
			}

			if (mnemeFound)
			{
				return "Test Center already installed in site " + site.getTitle() + " (" + context + ")";
			}

			// add a new page
			SitePage newPage = site.addPage();
			newPage.setTitle(tcTool.getTitle());
			// TODO: newPage.setPosition(?);

			// add the tool
			ToolConfiguration config = newPage.addTool();
			config.setTitle(tcTool.getTitle());
			config.setTool("sakai.mneme", tcTool);

			// add permissions to realm
			for (Iterator i = site.getRoles().iterator(); i.hasNext();)
			{
				Role role = (Role) i.next();
				if (this.maintainRole(role.getId()))
				{
					role.allowFunction("mneme.manage");
					role.allowFunction("mneme.grade");
				}
				else if (this.accessRole(role.getId()))
				{
					role.allowFunction("mneme.submit");
				}
			}

			// work around a "feature" of the Site impl - role changes do not trigger an azg save
			site.setMaintainRole(site.getMaintainRole());

			// save the site
			siteService.save(site);

			return "Test Center installed in site " + site.getTitle() + " (" + context + ")";
		}
		catch (IdUnusedException e)
		{
			return e.toString();
		}
		catch (PermissionException e)
		{
			return e.toString();
		}
	}

	/**
	 * Is the roleId a maintain class role?
	 * 
	 * @param roleId
	 *        The role id.
	 * @return true if this is a maintain class role, false if not.
	 */
	protected boolean maintainRole(String roleId)
	{
		if (this.maintainRoles.contains(roleId.toLowerCase())) return true;
		return false;
	}
}
