package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.TwoFactorAuthentication;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.PortalNodeComparator;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.api.Portal.LoginRoute;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.presence.cover.PresenceService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.util.Web;

public class HierarchyHandler extends SiteHandler {

	public final static String INCLUDE_HIERARCHY_PAGE_NAV = "include-hierarchy-page-nav";

	private static Log log = LogFactory.getLog(HierarchyHandler.class);
	private SiteService siteService;
	private PortalHierarchyService portalHierarchyService;
	private SecurityService securityService;
	private boolean resetTools;

	private Comparator<PortalNode> nodeTitleSorter = new PortalNodeComparator();

	public HierarchyHandler(SiteService siteService, PortalHierarchyService portalHierarchyService, SecurityService securityService, String fragment) {
		this.siteService = siteService;
		this.portalHierarchyService = portalHierarchyService;
		this.securityService = securityService;
		setUrlFragment(fragment);
		resetTools = ServerConfigurationService.getBoolean(Portal.CONFIG_AUTO_RESET, false);
	}

	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{

		// Set a threadlocal so lower levels can know we are using the hierarchy handler (eg aliases).
		ThreadLocalManager.set("sakai:portal:hierarchy", Boolean.TRUE);

		// If we are specified (/portal/hierarchy)
		if ( (parts.length >= 2) && parts[1].equals(getUrlFragment()))
		{
			return doFindSite(parts, 2, req, res, session);
		}
		// If we have an extra slash (/portal/)
		else if (parts.length > 0)
		{
			return doFindSite(parts, 1, req, res, session);
		}
		// Default handling (/portal)
		else if (parts.length == 0)
		{
			return doFindSite(parts, 0, req, res, session);
		}
		else
		{
			return NEXT;
		}
	}

	// / - root site / default page
	// /page/site - root site / page site
	// /college - college site / default page
	// /college/page/site - college site / page site
	// /asdasd/asdasd - Error
	// /college/asdsad - Error

	private int doFindSite(String[] parts, int start, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException {
		try
		{
			// This is so that when the site service builds URLs it uses hierarchy.
			session.setAttribute("sakai-controlling-portal", getUrlFragment());
			PortalNode node = null;
			Site site = null;
			String pageId = null;
			int end = parts.length;

			//First see if we need to trim off a page
			if (start + 2 <= end && "page".equals(parts[end-2]))
			{
				pageId = parts[end-1];
				end -=2;
			}
			String hierarchyPath = buildPath(parts, start, end);
			node = portalHierarchyService.getNode(hierarchyPath);
			if (node == null)
			{
				int partsRemaining = end - start;
				// The reason we check the number of parts is that looking for the redirect involved lots of
				// calls through to the DB and so when we can see we're looking for a site we don't need
				// to do this.
				if (partsRemaining > 1)
				{
					// Before we show the error site we need to check for redirects on the leaves of the tree.
					int currentEnd = start;
					// Don't include end as we've already checked the full path.
					while (++currentEnd < end) {
						hierarchyPath = buildPath(parts, start, currentEnd);
						PortalNode possibleRedirect = portalHierarchyService.getNode(hierarchyPath);
						if (possibleRedirect instanceof PortalNodeRedirect)
						{
							// Do redirect, we don't check permission on redirects.
							doRedirect(buildPath(parts, start, parts.length), res, possibleRedirect);
							return END;
						}
					}
				}
				else if (partsRemaining == 1)
				{
					try
					{
						site = portal.getSiteHelper().getSiteVisit(parts[start]);
					}
					catch (IdUnusedException iuue)
					{
						req.setAttribute("siteId", parts[start]);
						portal.doError(req, res, session, Portal.ERROR_SITE);
						return END;
					}
					catch (PermissionException pe)
					{
						site = siteService.getSite(parts[start]);
						doPermissionDenied(req, res, session, site, req.getContextPath()+req.getServletPath());
						return END;
					}
				}
				else
				{
					node = portalHierarchyService.getNode(null);
				}
			}

			log.debug("siteId: "+ ((site==null)?"null":site.getId())+ " pageId: "+ pageId);

			if (node == null) {
				if (site == null)
				{
					req.setAttribute("siteId", buildPath(parts, start, parts.length));
					portal.doError(req, res, session, Portal.ERROR_SITE);
				} else {
					super.doSite(req, res, session, site.getId(), pageId, null, null, parts, req.getContextPath()+req.getServletPath());
				}
			} else {
				if (node instanceof PortalNodeSite) {
					PortalNodeSite siteNode = (PortalNodeSite) node;
					portalHierarchyService.setCurrentPortalNode(siteNode);
					site = siteNode.getSite();
					doSite(req, res, session, site, pageId, req.getContextPath()
						+ req.getServletPath()+siteNode.getPath(), siteNode);
				} else if (node instanceof PortalNodeRedirect) {
					doRedirect(buildPath(parts, start, parts.length), res, node);
				} else {
					throw new IllegalStateException("We only know about 2 node types.");
				}
			}
			return END;
		}
		catch (Exception ex)
		{
			throw new PortalHandlerException(ex);
		}
	}

	/**
	 * This sends the redirect to the user.
	 * @param fullPath The full path of the parts that matched including the extra parts.
	 * @param res The response to which we send the redirect.
	 * @param node The redirect node that the user ended up at.
	 */
	private void doRedirect(String fullPath, HttpServletResponse res, PortalNode node)
			throws IOException
	{
		PortalNodeRedirect redirectNode = (PortalNodeRedirect) node;
		String redirect = redirectNode.getUrl();
		if (redirectNode.isAppendPath())
		{
			String nodePath = node.getPath();
			if (fullPath.startsWith(nodePath))
			{
				String extraPath = fullPath.substring(nodePath.length());
				if (redirect.endsWith("/") && extraPath.length() > 1)
				{
					extraPath = extraPath.substring(1);
				}
				redirect = redirect + extraPath;
			}
			else
			{
				// This shouldn't happen.
				log.warn("Failed to match node path against request path, extra parameters not appended. Path: "+ fullPath+ " NodePath: "+ nodePath);
			}
		}
		res.sendRedirect(redirect);
	}

	/**
	 * Builds a hierarchy path from subset of the parts supplied.
	 * The resulting path never ends with a '/'.
	 * @return The built string.
	 */
	private String buildPath(String[] parts, int start, int stop)
	{
		StringBuffer hierarchyPath = new StringBuffer();
		for (int partNo = start; partNo < stop; partNo++) {
			hierarchyPath.append("/");
			hierarchyPath.append(parts[partNo]);
		}
		return hierarchyPath.toString();
	}


	public void doSite(HttpServletRequest req, HttpServletResponse res, Session session,
			final Site site, String pageId, String toolContextPath, PortalNodeSite node) throws ToolException,
			IOException
	{
		Site hierarchySite = null;

		// default site if not set
		if (site == null || node == null)
		{
			portal.doError(req, res, session, Portal.ERROR_SITE);
			return;
		}

		if (!node.canView())
		{
			doPermissionDenied(req, res, session, site, toolContextPath);
			return;
		}
		try
		{
			hierarchySite = siteService.getSite("!hierarchy");
		}
		catch (IdUnusedException e)
		{
			log.warn("Hierarchy site not found.");
		}

		if (hierarchySite != null)
		{
			// Do permission checks against the current site rather than the hierarchy site.
			// This isn't a good way of doing this and should change later.
			final Site otherSite = hierarchySite;
			securityService.pushAdvisor(new SecurityAdvisor(){

				public SecurityAdvice isAllowed(String userId, String function,
						String reference) {
					if (reference.equals(otherSite.getReference()) && !site.getReference().equals(otherSite.getReference())) {
						// Use the security service so it picks up from the templates.
						boolean allowed = securityService.unlock(userId, function, site.getReference());
						return (allowed)?SecurityAdvice.ALLOWED:SecurityAdvice.NOT_ALLOWED;
					}
					return SecurityAdvice.PASS;
				}

			});
		}

		// find the page, or use the first page if pageId not found

		SitePage page = lookupSitePage(pageId, site);
		if (page == null && pageId != null && node != null)
		{
			// Look in the hierarchy site.
			page = lookupSitePage(pageId, hierarchySite);
			if (page != null)
			{
				// Fix up the skin.
				page = new AdoptedSitePage(node, page);
			}

		}

		if (page == null)
		{
			List pages =  portal.getSiteHelper().getPermittedPagesInOrder(site);
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

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : "
				+ site.getTitle() + " : " + page.getTitle();

		// start the response
		String siteType = portal.calcSiteType(site.getId());
		PortalRenderContext rcontext = portal.startPageContext(siteType, title, site
				.getSkin(), req);

		// Include normal site nav details.
		includeSiteNav(rcontext, req, session, site.getId());

		String prefix = getUrlFragment();
		String siteUrl = node.getPath();
		if (siteUrl.endsWith("/"))
		{
			siteUrl = siteUrl.substring(0, siteUrl.length()-1);
		}



		if (hierarchySite != null)
		{

			includeHierarchyNav(rcontext, req, session, site, page, toolContextPath, prefix, siteUrl, hierarchySite, node);
		}

		includeSiteBanner(rcontext, site);

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
		try
		{
			boolean presenceEvents = ServerConfigurationService.getBoolean("presence.events.log", true);
			if (presenceEvents)
				PresenceService.setPresence(site.getId() + "-presence");
		}
		catch(Exception e)
		{
			log.info("Failed to set presence: "+ e.getMessage());
		}
	}

	private void doPermissionDenied(HttpServletRequest req,
			HttpServletResponse res, Session session, final Site site,
			String toolContextPath) throws ToolException,
			IOException {
		TwoFactorAuthentication twoFactorAuthentication =
			(TwoFactorAuthentication)ComponentManager.get(TwoFactorAuthentication.class);
		String userId = session.getUserId();

		// if not logged in, give them a chance
		if (userId == null)
		{
			StoredState ss = portalService.newStoredState("directtool", "tool");
			ss.setRequest(req);
			ss.setToolContextPath(toolContextPath);
			portalService.setStoredState(ss);
			portal.doLogin(req, res, session, req.getPathInfo(), Portal.LoginRoute.NONE);
		}

		else if (twoFactorAuthentication.isTwoFactorRequired(site.getReference())
				&& !twoFactorAuthentication.hasTwoFactor())
		{
			portal.doLogin(req, res, session, req.getPathInfo(), LoginRoute.TWOFACTOR);
		}

		else
		{
			String siteId = site.getId();

			if (ServerConfigurationService.getBoolean("portal.redirectJoin", true) &&
					userId != null && portal.getSiteHelper().isJoinable(siteId, userId))
			{
				String redirectUrl = Web.returnUrl(req, "/join/"+siteId);
				res.sendRedirect(redirectUrl);
				return;
			}
			else
			{
				req.setAttribute("siteId", siteId);
				portal.doError(req, res, session, Portal.ERROR_SITE);
			}
		}
	}

	protected void includeHierarchyNav(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, Site site, SitePage page, String toolContextPath, String prefix, String siteUrl, Site hierarchySite, PortalNodeSite node)
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
				includeHierarchy(rcontext, req, session, site, page, toolContextPath, prefix, siteUrl, hierarchySite, node);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

	private void includeHierarchy(PortalRenderContext rcontext, HttpServletRequest req, Session session, final Site site, SitePage page, String toolContextPath, String portalPrefix, String siteUrl, final Site hierarchySite, PortalNodeSite node) {
		// Need to get list of parents

		List<PortalNodeSite> parentNodes = portalHierarchyService.getNodesFromRoot(node.getId());

		boolean loggedIn = session.getUserId() != null;

		List<Site> parentSites = new ArrayList<Site>(parentNodes.size());
		for (PortalNodeSite parentNode: parentNodes) {
			parentSites.add(parentNode.getSite());
		}
		String currentSiteId = node.getSite().getId();
		Site myWorkspace = portal.getSiteHelper().getMyWorkspace(session);
		String myWorkspaceId = null;
		if (myWorkspace != null) {
			myWorkspaceId = myWorkspace.getId();
		}

		List<Map> parentSiteMaps = portal.getSiteHelper().convertSitesToMaps(req, parentSites, getUrlFragment(), currentSiteId, myWorkspaceId, false, false, resetTools, false, null, loggedIn);
		rcontext.put("parents", parentSiteMaps);

		// Details of children.
		// List of site.visit but also display joinable.
		// Also list redirect nodes that should be shown.

		List<PortalNode> childNodes = new ArrayList<PortalNode>();

		for (PortalNode currentChild : portalHierarchyService.getNodeChildren(node.getId())) {
			if (currentChild.canView()) {
				childNodes.add(currentChild);
			} else if (currentChild instanceof PortalNodeSite) {
				PortalNodeSite siteChild = (PortalNodeSite) currentChild;
				if (siteChild.getSite() != null && ((siteChild.getSite().isJoinable() && siteChild.getSite().isPublished()) && (loggedIn || siteChild.getSite().isPubView()))) {
					childNodes.add(currentChild);
				}
			} else if (currentChild instanceof PortalNodeRedirect) {
				// No special rules.
			}
		}

		// Sort the nodes.
		Collections.sort(childNodes, nodeTitleSorter);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>(childNodes.size());
		for (PortalNode child: childNodes) {
			Map<String,String> detail = null;
			if (child instanceof PortalNodeSite) {
				Site childSite = ((PortalNodeSite)child).getSite();
				detail = portal.getSiteHelper().convertSiteToMap(req, childSite, getUrlFragment(), site.getId(), myWorkspaceId, false, false, resetTools, false, null, loggedIn);
				detail.put("iconClass", "icon-sakai-subsite");
			} else if (child instanceof PortalNodeRedirect) {
				detail = convertRedirectToMap(node, child);
			}
			list.add(detail);
		}

		List<Map<String,String>> childSiteMaps = list;
		rcontext.put("children", childSiteMaps);

		String siteEffectiveId = node.getPath();


		List<Map<String, Object>> hierarchyPages = new ArrayList<Map<String, Object>>();
		for (SitePage hierarchyPage: (List<SitePage>)portal.getSiteHelper().getPermittedPagesInOrder(hierarchySite)) {
			String pageAlias = portal.getSiteHelper().lookupPageToAlias(hierarchySite.getId(), hierarchyPage);

			String pagerefUrl = ToolUtils.getPageUrl(req, site, hierarchyPage, portalPrefix,
					resetTools, siteEffectiveId, pageAlias);
			boolean current = hierarchyPage.equals(page) && !(hierarchyPage.isPopUp());
			hierarchyPages.add(portal.getSiteHelper().pageToMap(req, site, false, hierarchyPage, hierarchyPage.getTools(), null, null, current, pagerefUrl));
		}
		rcontext.put("hierarchyPages", hierarchyPages);


		// What todo if you can't see current site?

	}

	private Map<String, String> convertRedirectToMap(PortalNodeSite node, PortalNode child) {
		Map<String, String> detail;
		detail = new HashMap<String,String>();
		PortalNodeRedirect redirect = (PortalNodeRedirect)child;
		detail.put("isPublished", "true");
		detail.put("iconClass", redirect.getUrl().contains("://")?
		        "icon-sakai-redirect-external":
		        "icon-sakai-redirect-internal");
		String url = node.getSite().getUrl();
		if (!url.endsWith("/")) {
		    url += "/";
		}
		url += child.getName() + "/";
		detail.put("siteUrl", url);
		detail.put("shortDescription", "");
		detail.put("siteTitle", Web.escapeHtml(child.getTitle()));
		return detail;
	}


	private Map<String, Object> getUnknownSite(PortalNode currentNode) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "Unknown Site");
		map.put("url", getNodeURL(currentNode));
		return null;
	}

	private Map<String, Object> convertToMap(PortalNodeSite currentNode) {
		Map<String, Object> siteDetails = new HashMap<String, Object>();
		if (currentNode.canView()) {
			Site currentSite = currentNode.getSite();
			siteDetails.put("url", getNodeURL(currentNode));
			siteDetails.put("title", Web.escapeHtml(currentSite.getTitle()));
			siteDetails.put("path", currentNode.getPath());
			siteDetails.put("shortDescription", (currentSite.getShortDescription()== null)?null:Web.escapeHtml(currentSite.getShortDescription()));
			return siteDetails;
		}

		return null;
	}

	private String getNodeURL(PortalNode node) {
		return ServerConfigurationService.getPortalUrl()+ "/"+ getUrlFragment() + Web.escapeUrl(node.getPath());
	}

	public SitePage lookupSitePage(String pageId, Site site)
	{
		// Make sure we have some permitted pages
		List pages = portal.getSiteHelper().getPermittedPagesInOrder(site);
		if (pages.isEmpty()) return null;
		SitePage page = site.getPage(pageId);
		if (page == null)
		{
			page = portal.getSiteHelper().lookupAliasToPage(pageId, site);
		}
		if (page != null) {

			// Make sure that they user has permission for the page.
			// If the page is not in the permitted list go to the first
			// page.
			for (Iterator i = pages.iterator(); i.hasNext();)
			{
				SitePage p = (SitePage) i.next();
				if (p.getId().equals(page.getId())) return page;
			}
		}

		return null;
	}

}
