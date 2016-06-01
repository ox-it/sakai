package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Web;

/**
 * This exists to find previous nodes and redirect to their new URLs.
 * It parses URLs that useds to exist and redirects to their new locations.
 */
public class HierarchyHandler extends BasePortalHandler {

	private static Log log = LogFactory.getLog(HierarchyHandler.class);
	private PortalHierarchyService portalHierarchyService;
	private String destination;


	public HierarchyHandler(PortalHierarchyService portalHierarchyService, String fragment, String destination) {
		this.portalHierarchyService = portalHierarchyService;
		setUrlFragment(fragment);
		this.destination = destination;
	}

	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		// If we are specified (/portal/hierarchy)
		if ( (parts.length >= 2) && parts[1].equals(getUrlFragment()))
		{
			return doFindSite(parts, 2, req, res, session);
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
			// Set a threadlocal so lower levels can know we are using the hierarchy handler (eg aliases).
			ThreadLocalManager.set("sakai:portal:hierarchy", Boolean.TRUE);

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
						site = portal.getSiteHelper().getSite(parts[start]);
					}
					catch (IdUnusedException iuue)
					{
						req.setAttribute("siteId", parts[start]);
						portal.doError(req, res, session, Portal.ERROR_SITE);
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
				// Just redirect to the same URL with /site/
				String siteUrl = req.getContextPath() + destination;
				if (parts.length > 2) {
					siteUrl = siteUrl + Web.makePath(parts, 2, parts.length);
				}
				// Make sure to add the parameters such as panel=Main
				String queryString = req.getQueryString();
				if (queryString != null)
				{
					siteUrl = siteUrl + "?" + queryString;
				}
				res.sendRedirect(siteUrl);
			} else {
				if (node instanceof PortalNodeSite) {
					PortalNodeSite siteNode = (PortalNodeSite) node;
					portalHierarchyService.setCurrentPortalNode(siteNode);
					String redirectUrl = req.getContextPath() + destination+ "/"+ node.getUrlPath();
					if (pageId != null) {
						redirectUrl += "/page/"+pageId;
					}
					String queryString = req.getQueryString();
					if (queryString != null)
					{
						redirectUrl += "?" + queryString;
					}
					res.sendRedirect(redirectUrl);
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


}
