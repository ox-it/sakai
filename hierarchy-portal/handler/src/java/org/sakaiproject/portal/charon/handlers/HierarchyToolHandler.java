package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.TwoFactorAuthentication;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.Portal.LoginRoute;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;


/**
 * Currently isn't used as we can get enough information from the placement about what the current tool it.
 * @author buckett
 *
 */
public class HierarchyToolHandler extends ToolHandler {

	private static Log log = LogFactory.getLog(HierarchyToolHandler.class);
	
	private PortalHierarchyService portalHierarchyService;
	
	public HierarchyToolHandler(PortalHierarchyService portalHierarchyService) {
		this.portalHierarchyService = portalHierarchyService;
		setUrlFragment("hierarchytool");
	}
	
	public void doTool(HttpServletRequest req, HttpServletResponse res, Session session,
			String placementId, String toolContextPath, String toolPathInfo)
			throws ToolException, IOException
	{

		if (portal.redirectIfLoggedOut(res)) return;

		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		ActiveTool tool;
		String skin = null;
		if (siteTool == null)
		{
			// Find the placement and hierarchy node.
			String parts[] = placementId.split(":");
			
			if (parts.length == 2 )
			{
				siteTool = SiteService.findTool(parts[0]);
				PortalNode node = portalHierarchyService.getNodeById(parts[1]);
				if (node != null)
				{
					portalHierarchyService.setCurrentPortalNode(node);
					skin = node.getSite().getSkin();
					siteTool = new AdoptedToolConfiguration(new AdoptedSitePage(node, siteTool.getContainingPage()), siteTool);
				} else {
					log.info("Node not found: "+ parts[1]);
				}
			}
			else
			{
				portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				return;
			}
		}
		else
		{
			skin = siteTool.getSkin();
		}
		tool = ActiveToolManager.getActiveTool(siteTool.getToolId());

		// Reset the tool state if requested
		if (portalService.isResetRequested(req))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
		}
 
		if (tool == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{
			TwoFactorAuthentication twoFactorAuthentication = 
				(TwoFactorAuthentication)ComponentManager.get(TwoFactorAuthentication.class);
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(siteTool.getSiteId());
			}
			catch (IdUnusedException e)
			{
				portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				return;
			}
			catch (PermissionException e)
			{
				// if not logged in, give them a chance
				if (session.getUserId() == null)
				{
					portal.doLogin(req, res, session, req.getPathInfo(), false);
				}
				else if (twoFactorAuthentication.isTwoFactorRequired("/site/"+siteTool.getSiteId())
						&& !twoFactorAuthentication.hasTwoFactor())
				{
					portal.doLogin(req, res, session, req.getPathInfo(), LoginRoute.TWOFACTOR);
				}
				else
				{
					portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				}
				return;
			}
		}

		portal.forwardTool(tool, req, res, siteTool, skin, toolContextPath,
				toolPathInfo);
	}

}
