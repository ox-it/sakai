package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.util.PortalSiteHelper;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.util.Web;

public class HierarchyHandler extends SiteHandler {
	
	public final static String INCLUDE_HIERARCHY_PAGE_NAV = "include-hierarchy-page-nav";
	
	private static Log log = LogFactory.getLog(HierarchyHandler.class);
	private PortalSiteHelper siteHelper;
	private SiteService siteService;
	
	public HierarchyHandler(SiteService siteService) {
		this.siteService = siteService;
		urlFragment = "hierarchy";
		siteHelper = new PortalSiteHelper();
	}
	
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if ( (parts.length >= 2) && parts[1].equals(urlFragment) || parts.length == 0)
		{
			log.debug("Matched");
			try
			{
				PortalHierarchyService phs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
				Hierarchy hierarchy = null;
				int hierarchyPartNo = parts.length;
				for (; hierarchy == null && hierarchyPartNo >= 2; hierarchyPartNo--) {
					StringBuffer hierarchyPath = new StringBuffer();
					for (int partNo = 2; partNo < hierarchyPartNo; partNo++) {
						hierarchyPath.append("/");
						hierarchyPath.append(parts[partNo]);
					}
					log.debug("Looking for: "+ hierarchyPath.toString());
					hierarchy = phs.getNode(hierarchyPath.toString());
				}
				
				if (hierarchy == null) {
					log.debug("Using root site.");
						hierarchy = phs.getNode(null);
				}
				phs.setCurrentPortalPath(hierarchy.getPath());
				log.debug("Path is: "+ hierarchy.getPath());
				
				HierarchyProperty prop = hierarchy.getProperty(PortalHierarchyService.CONTENT);
				String siteId = (prop != null)?prop.getPropvalue():null;
				
				String pageId = null;
					
				
				if (parts.length >= hierarchyPartNo+2 && "page".equals(parts[hierarchyPartNo+1])){
					pageId = parts[hierarchyPartNo+2];
				} 				
				
				log.debug("siteId: "+ siteId+ " pageId: "+ pageId);
				doSite(req, res, session, siteId, pageId, req.getContextPath()
						+ req.getServletPath()+hierarchy.getPath(), hierarchy);
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
	

	public void doSite(HttpServletRequest req, HttpServletResponse res, Session session,
			String siteId, String pageId, String toolContextPath, Hierarchy node) throws ToolException,
			IOException
	{
		
		// default site if not set
		if (siteId == null)
		{
			if (session.getUserId() == null)
			{
				siteId = ServerConfigurationService.getGatewaySiteId();
			}
			else
			{
				siteId = siteService.getUserSiteId(session.getUserId());
			}
		}


		// find the site, for visiting
		Site site = null;
		try
		{
			site = siteHelper.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			portal.doError(req, res, session, Portal.ERROR_SITE);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				StoredState ss = portalService.newStoredState("directtool", "tool");
				ss.setRequest(req);
				ss.setToolContextPath(toolContextPath);
				portalService.setStoredState(ss);
				portal.doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				portal.doError(req, res, session, Portal.ERROR_SITE);
			}
			return;
		}
		Site hierarchySite = null;
		try
		{
			hierarchySite = siteService.getSite("!hierarchy");
		}
		catch (IdUnusedException e)
		{
			log.warn("Hierarchy site not found.");
		}
				// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(Portal.ATTR_SITE_PAGE + node.getId());
		}

		// find the page, or use the first page if pageId not found
		SitePage page = site.getPage(pageId);
		if (page == null)
		{
			// Look in the hierarchy site.
			page = hierarchySite.getPage(pageId);
			if (page != null) 
			{
				// Fix up the skin.
				page = new AdoptedSitePage(site, page);
			}
		}
		
		if (page == null)
		{
			// List pages = site.getOrderedPages();
			List pages = siteHelper.getPermittedPagesInOrder(site);
			if (!pages.isEmpty())
			{
				page = (SitePage) pages.get(0);
			}
		}
		if (page == null)
		{
			portal.doError(req, res, session, Portal.ERROR_SITE);
			return;
		}

		// store the last page visited
		session.setAttribute(Portal.ATTR_SITE_PAGE + node.getId(), page.getId());

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		String siteType = portal.calcSiteType(siteId);
		PortalRenderContext rcontext = portal.startPageContext(siteType, title, site
				.getSkin(), req);


		String prefix = urlFragment + node.getPath();
		if (prefix.endsWith("/")) 
		{
			prefix = prefix.substring(0, prefix.length()-1);
		}
		

		// the 'full' top area
		includeHierarchyNav(rcontext, req, session, site, page, toolContextPath, prefix, hierarchySite, node);
		includeWorksite(rcontext, res, req, session, site, page, toolContextPath, prefix);

		portal.includeBottom(rcontext);

		// end the response
		portal.sendResponse(rcontext, res, "site", null);
		StoredState ss = portalService.getStoredState();
		if (ss != null && toolContextPath.equals(ss.getToolContextPath()))
		{
			// This request is the destination of the request
			portalService.setStoredState(null);
		}
	}
	
	protected void includeHierarchyNav(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, Site site, SitePage page, String context, String prefix, Site hierarchySite, Hierarchy node)
	{
			boolean loggedIn = session.getUserId() != null;


			String accessibilityURL = ServerConfigurationService
					.getString("accessibility.url");
			rcontext.put("siteNavHasAccessibilityURL", Boolean
					.valueOf((accessibilityURL != null && accessibilityURL != "")));
			rcontext.put("siteNavAccessibilityURL", accessibilityURL);

			rcontext.put("siteNavLoggedIn", Boolean.valueOf(loggedIn));
			
			String cssClass = (site.getType() != null) ? "siteNavWrap " + site.getType()
					: "siteNavWrap";

			rcontext.put("tabsCssClass", cssClass);

			try {
				includeLogo(rcontext, req, session, site.getId());
				includeHierarchy(rcontext, req, session, site, page, context, prefix, hierarchySite, node);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void includeHierarchy(PortalRenderContext rcontext, HttpServletRequest req, Session session, Site site, SitePage page, String context, String portalPrefix, Site hierarchySite, Hierarchy node) {
		// Need to get list of parents
		
		Hierarchy currentNode = node;
		
		List<Map<String, Object>> parents = new ArrayList<Map<String, Object>>();
		
		while (currentNode != null) {
			Map<String, Object> map = convertToMap(currentNode);
			if (map == null) {
				parents.add(getUnknownSite(currentNode));
			} else {
				parents.add(map);
			}
			currentNode = currentNode.getParent();
		}
		
		Collections.reverse(parents);
		rcontext.put("parents", parents);
		
		// Details of children.
		// List of site.visit but also display joinable.
		
		List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
		
		for (Hierarchy currentChild : ((Map<String,Hierarchy>)node.getChildren()).values()) {
			Map<String, Object> map = convertToMap(currentChild);
			if (map != null) {
				children.add(map);
			} else {
				log.debug("Ignored node: "+ currentChild);
			}
		}
		
		rcontext.put("children", children);
		
		rcontext.put("tabsSites", Boolean.TRUE);
		
		// Name - from Site or fallback to hierarchy.
		
		
			String pageUrl = Web.returnUrl(req, "/" + portalPrefix 
					+ "/page/");
			String toolUrl = Web.returnUrl(req, "/" + portalPrefix 
					+ Web.escapeUrl(siteHelper.getSiteEffectiveId(site)));
			String pagePopupUrl = Web.returnUrl(req, "/page/");
			
			List<Map> hierarchyPages = convertPagesToMap(hierarchySite, page, portalPrefix,
					/* doPages */true,
					/* resetTools */"true".equals(ServerConfigurationService.getString(Portal.CONFIG_AUTO_RESET)),
					/* includeSummary */false, pageUrl, toolUrl, pagePopupUrl);
		rcontext.put("hierarchyPageNavTools", hierarchyPages);
		
		// What todo if you can't see current site?
		
	}


	private Map<String, Object> getUnknownSite(Hierarchy currentNode) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "Unknown Site");
		map.put("url", getNodeURL(currentNode));
		return null;
	}

	private Map<String, Object> convertToMap(Hierarchy currentNode) {
		Map<String, Object> siteDetails = new HashMap<String, Object>();
		HierarchyProperty contentProperty = currentNode.getProperty(PortalHierarchyService.CONTENT);
		if (contentProperty != null) {
			String currentSiteId = contentProperty.getPropvalue();
			if (currentSiteId != null) {
				try {
					
					Site currentSite = siteService.getSite(currentSiteId);
					if (allowSeeSite(currentSite)) {
						siteDetails.put("url", getNodeURL(currentNode));
						
						HierarchyProperty titleProperty = currentNode.getProperty(PortalHierarchyService.TITLE);
						if (titleProperty != null && titleProperty.getPropvalue() != null) {
							siteDetails.put("title", Web.escapeHtml(titleProperty.getPropvalue()));
						} else {
							siteDetails.put("title", Web.escapeHtml(currentSite.getTitle()));
						}
						siteDetails.put("path", currentNode.getPath());
						siteDetails.put("shortDescription", (currentSite.getShortDescription()== null)?null:Web.escapeHtml(currentSite.getShortDescription()));
						return siteDetails;
					}
					
				} catch (IdUnusedException iue) {
					log.debug("Couldn't find content site: "+ currentSiteId+ " at node "+ currentNode.getPath());
				}
			}	
		}
		
		return null;
	}
	
	private boolean allowSeeSite(Site site) {
		return site.isPubView() || site.isJoinable() || siteService.allowAccessSite(site.getId());
	}
	
	private String getNodeURL(Hierarchy node) {
		return ServerConfigurationService.getPortalUrl()+ "/"+ urlFragment + Web.escapeUrl(node.getPath());
	}
	
	public void includeWorksite(PortalRenderContext rcontext, HttpServletResponse res,
			HttpServletRequest req, Session session, Site site, SitePage page,
			String toolContextPath, String portalPrefix) throws IOException
	{
		if (rcontext.uses(INCLUDE_WORKSITE))
		{

			// add the page navigation with presence
			includePageNav(rcontext, req, session, site, page, toolContextPath,
					portalPrefix);

			// add the page
			includePage(rcontext, res, req, page, toolContextPath, "content");
		}

	}
	
	protected void includePageList(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, Site site, SitePage page, String toolContextPath,
			String portalPrefix, boolean doPages, boolean resetTools,
			boolean includeSummary) throws IOException
	{
		boolean loggedIn = session.getUserId() != null;

		String pageUrl = Web.returnUrl(req, "/" + portalPrefix 
				+ "/page/");
		String toolUrl = Web.returnUrl(req, "/" + portalPrefix 
				+ Web.escapeUrl(siteHelper.getSiteEffectiveId(site)));
		if (resetTools)
		{
			toolUrl = toolUrl + "/tool-reset/";
		}
		else
		{
			toolUrl = toolUrl + "/tool/";
		}

		String pagePopupUrl = Web.returnUrl(req, "/page/");
		
		if (rcontext.uses(INCLUDE_PAGE_NAV))
		{
			boolean showHelp = ServerConfigurationService.getBoolean("display.help.menu",
					true);
			String iconUrl = site.getIconUrlFull();
			boolean published = site.isPublished();
			String type = site.getType();

			rcontext.put("pageNavPublished", Boolean.valueOf(published));
			rcontext.put("pageNavType", type);
			rcontext.put("pageNavIconUrl", iconUrl);
			// rcontext.put("pageNavSitToolsHead",
			// Web.escapeHtml(rb.getString("sit_toolshead")));

			// order the pages based on their tools and the tool order for the
			// site type
			// List pages = site.getOrderedPages();
			List<Map> l = convertPagesToMap(site, page, portalPrefix, doPages,
					resetTools, includeSummary, pageUrl, toolUrl, pagePopupUrl);
			rcontext.put("pageNavTools", l);

			String helpUrl = ServerConfigurationService.getHelpUrl(null);
			rcontext.put("pageNavShowHelp", Boolean.valueOf(showHelp));
			rcontext.put("pageNavHelpUrl", helpUrl);

			// rcontext.put("pageNavSitContentshead",
			// Web.escapeHtml(rb.getString("sit_contentshead")));

			// Handle Presense
			boolean showPresence = ServerConfigurationService.getBoolean(
					"display.users.present", true);
			String presenceUrl = Web.returnUrl(req, "/presence/"
					+ Web.escapeUrl(site.getId()));

			// rcontext.put("pageNavSitPresenceTitle",
			// Web.escapeHtml(rb.getString("sit_presencetitle")));
			// rcontext.put("pageNavSitPresenceFrameTitle",
			// Web.escapeHtml(rb.getString("sit_presenceiframetit")));
			rcontext.put("pageNavShowPresenceLoggedIn", Boolean.valueOf(showPresence
					&& loggedIn));
			rcontext.put("pageNavPresenceUrl", presenceUrl);
		}

	}

	protected List<Map> convertPagesToMap(Site site, SitePage page,
			String portalPrefix, boolean doPages, boolean resetTools,
			boolean includeSummary, String pageUrl, String toolUrl,
			String pagePopupUrl) {
		List pages = siteHelper.getPermittedPagesInOrder(site);

		List<Map> l = new ArrayList<Map>();
		for (Iterator i = pages.iterator(); i.hasNext();)
		{

			SitePage p = (SitePage) i.next();
			List pTools = p.getTools();

			boolean current = (page != null && p.getId().equals(page.getId()) && !p
					.isPopUp());
			String pagerefUrl = pageUrl + Web.escapeUrl(p.getId());
			if (resetTools)
			{
				pagerefUrl = pagerefUrl.replaceFirst("/" + portalPrefix + "/", "/"
						+ portalPrefix + "-reset/");
			}

			if (doPages || p.isPopUp())
			{
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("isPage", Boolean.valueOf(true));
				m.put("current", Boolean.valueOf(current));
				m.put("ispopup", Boolean.valueOf(p.isPopUp()));
				m.put("pagePopupUrl", pagePopupUrl);
				m.put("pageTitle", Web.escapeHtml(p.getTitle()));
				m.put("jsPageTitle", Web.escapeJavascript(p.getTitle()));
				m.put("pageId", Web.escapeUrl(p.getId()));
				m.put("jsPageId", Web.escapeJavascript(p.getId()));
				m.put("pageRefUrl", pagerefUrl);
				if (includeSummary) siteHelper.summarizePage(m, site, p);
				l.add(m);
				continue;
			}

			// Loop through the tools again and Unroll the tools
			Iterator iPt = pTools.iterator();

			while (iPt.hasNext())
			{
				ToolConfiguration placement = (ToolConfiguration) iPt.next();

				String toolrefUrl = toolUrl + Web.escapeUrl(placement.getId());

				Map<String, Object> m = new HashMap<String, Object>();
				m.put("isPage", Boolean.valueOf(false));
				m.put("toolId", Web.escapeUrl(placement.getId()));
				m.put("jsToolId", Web.escapeJavascript(placement.getId()));
				m.put("toolRegistryId", placement.getToolId());
				m.put("toolTitle", Web.escapeHtml(placement.getTitle()));
				m.put("jsToolTitle", Web.escapeJavascript(placement.getTitle()));
				m.put("toolrefUrl", toolrefUrl);
				l.add(m);
			}

		}
		return l;
	}
}
