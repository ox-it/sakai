package org.sakaiproject.portal.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;

public class HierarchySiteNeighbourhoodService implements SiteNeighbourhoodService
{
	
	private static Log log = LogFactory.getLog(HierarchySiteNeighbourhoodService.class);

	private SiteNeighbourhoodService proxy;
	
	private PortalHierarchyService portalHierarchyService;
	
	public List<Site> getSitesAtNode(HttpServletRequest request, Session session,
			boolean includeMyWorksite)
	{
		// Point we can probably set threadlocal later.
		return proxy.getSitesAtNode(request, session, includeMyWorksite);
	}

	public String lookupSiteAlias(String siteReferenced, String content)
	{
		// Get the threadlocal, if found use that, otherwise use proxy
		PortalNode node = (PortalNode) ThreadLocalManager.get("sakai:portal:node");
		if (node == null)
		{
			return proxy.lookupSiteAlias(siteReferenced, content);
		}
		// Need to check current site, then children, then parents.
		if (node.getSite().getReference().equals(siteReferenced))
		{
			return node.getPath();
		}
		for (PortalNode child: portalHierarchyService.getNodeChildren(node.getId()))
		{
			if (child.getSite().getReference().equals(siteReferenced))
			{
				return child.getPath();
			}
		}
		for (PortalNode parent: portalHierarchyService.getNodesFromRoot(node.getId()))
		{
			if (parent.getSite().getReference().equals(siteReferenced))
			{
				return parent.getPath();
			}
		}
		return null;
	}

	public String parseSiteAlias(String alias)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public SiteNeighbourhoodService getProxy()
	{
		return proxy;
	}

	public void setProxy(SiteNeighbourhoodService proxy)
	{
		this.proxy = proxy;
	}

	public PortalHierarchyService getPortalHierarchyService()
	{
		return portalHierarchyService;
	}

	public void setPortalHierarchyService(PortalHierarchyService portalHierarchyService)
	{
		this.portalHierarchyService = portalHierarchyService;
	}

}
