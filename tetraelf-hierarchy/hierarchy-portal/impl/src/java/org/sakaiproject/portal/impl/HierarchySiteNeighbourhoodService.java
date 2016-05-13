package org.sakaiproject.portal.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;

public class HierarchySiteNeighbourhoodService implements SiteNeighbourhoodService
{
	
	private static final String CACHE_KEY = HierarchySiteNeighbourhoodService.class+ "cache";

	private static Log log = LogFactory.getLog(HierarchySiteNeighbourhoodService.class);

	private SiteNeighbourhoodService proxy;
	
	private PortalHierarchyService portalHierarchyService;
	
	public List<Site> getSitesAtNode(HttpServletRequest request, Session session,
			boolean includeMyWorksite)
	{
		// Point we can probably set threadlocal later.
		return proxy.getSitesAtNode(request, session, includeMyWorksite);
	}

	public String lookupSiteAlias(String siteReferenced, String context)
	{
		Map<String, String> cache = (Map<String, String>)ThreadLocalManager.get(CACHE_KEY);
		if (cache == null)
		{
			cache = new HashMap<String, String>();
			ThreadLocalManager.set(CACHE_KEY, cache);
		}
		String key = siteReferenced+context;
		if (cache.containsKey(key))
		{
			return cache.get(key);
		}
		else
		{
			String value = findSiteAlias(siteReferenced, context);
			cache.put(key, value);
			return value;
		}
	}
	
	
	public String findSiteAlias(String siteReferenced, String content)
	{
		// Get the threadlocal, if found use that, otherwise use proxy
		PortalNode node = portalHierarchyService.getCurrentPortalNode();
		if (node instanceof PortalNodeSite)
		{
			PortalNodeSite siteNode = (PortalNodeSite)node;
			// Need to check current site, then children, then parents.
			if (siteNode.getSite().getReference().equals(siteReferenced) || siteNode.getManagementSite().getReference().equals(siteReferenced))
			{
				return siteNode.getPath();
			}
			for (PortalNode child: portalHierarchyService.getNodeChildren(siteNode.getId()))
			{
				if (child instanceof PortalNodeSite && ((PortalNodeSite)child).getSite().getReference().equals(siteReferenced))
				{
					return child.getPath();
				}
			}
			for (PortalNode parent: portalHierarchyService.getNodesFromRoot(siteNode.getId()))
			{
				if (parent instanceof PortalNodeSite && ((PortalNodeSite)parent).getSite().getReference().equals(siteReferenced))
				{
					return parent.getPath();
				}
			}
		}
		// If we aren't at a node but came through hierarchy (eg MyWorkspace URLs).
		if (ThreadLocalManager.get("sakai:portal:hierarchy") != null)
		{
			// Don't do this at the moment unless we are coming through the hierarchy handler
			String siteId = EntityManager.newReference(siteReferenced).getId();
			PortalNode defaultNode = portalHierarchyService.getDefaultNode(siteId);
			if (defaultNode != null) {
				return defaultNode.getPath();
			}
		}
		
		return proxy.lookupSiteAlias(siteReferenced, content);
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
