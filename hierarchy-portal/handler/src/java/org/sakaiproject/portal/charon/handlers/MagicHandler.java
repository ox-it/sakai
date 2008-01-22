/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Web;

/**
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class MagicHandler extends BasePortalHandler
{
	public MagicHandler()
	{
		urlFragment = "magic";
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		return doGet(parts, req, res, session);
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		// recognize and dispatch the 'tool' option: [1] = "tool", [2] =
		// placement id (of a site's tool placement), rest for the tool
		if ((parts.length > 2) && (parts[1].equals(urlFragment)))
		{
			try
			{
				ActiveTool tool = ActiveToolManager.getActiveTool(parts[2]);

				if (tool == null)
				{
					return ABORT;
				}
				
				doTool(req, res, session, tool, req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
				return END;
			}
			catch (Exception ex)
			{
				throw new PortalHandlerException(ex);
			}
		}
		else
		{
			return NEXT;
		}
	}

	public void doTool(HttpServletRequest req, HttpServletResponse res, Session session,
			ActiveTool tool, String toolContextPath, String toolPathInfo)
			throws ToolException, IOException
	{

		if (portal.redirectIfLoggedOut(res)) return;

		Placement p = new org.sakaiproject.util.Placement("new", tool.getId(), tool, null, null, tool.getTitle());
		
		portal.forwardTool(tool, req, res, p, ServerConfigurationService.getString("skin.default"), toolContextPath,
				toolPathInfo);
	}

}
