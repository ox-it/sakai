package org.sakaiproject.portal.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.portal.api.Neighbour;
import org.sakaiproject.portal.api.PortalSiteHelper;
import org.sakaiproject.portal.api.SiteNeighbour;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Web;

public class HierarchySiteNeighbourhoodService implements SiteNeighbourhoodService
{
	
	private static final String CACHE_KEY = HierarchySiteNeighbourhoodService.class+ "cache";

	private static Log log = LogFactory.getLog(HierarchySiteNeighbourhoodService.class);

	private SiteNeighbourhoodService proxy;
	
	private PortalHierarchyService portalHierarchyService;

	@Override
	public List<Site> getSitesAtNode(HttpServletRequest request, Session session, String nodeId,
										  boolean includeMyWorksite)
	{
		// 
		return proxy.getSitesAtNode(request, session, nodeId, includeMyWorksite);
	}

	@Override
	public List<Neighbour> getNeighboursAtNode(HttpServletRequest request, Session session, String nodeId, boolean includeMyWorksite) {
		// Point we can probably set threadlocal later.
		if( nodeId != null) {
			PortalNode node = portalHierarchyService.getNodeByUrl(nodeId);
			if (node instanceof PortalNodeSite) {
				List<PortalNode> nodeChildren = portalHierarchyService.getNodeChildren(node.getId());
				List<Neighbour> neighbours = new ArrayList<>();
				for (PortalNode child: nodeChildren) {

					if (child.canView() && child instanceof PortalNodeSite) {
						PortalNodeSite siteNode = (PortalNodeSite) child;
						neighbours.add(new SiteNeighbour(siteNode.getSite()));
					}
					if (child instanceof PortalNodeRedirect) {
						PortalNodeRedirect redirect = (PortalNodeRedirect) child;
						neighbours.add(new RedirectNeighbour(redirect));
					}

				}
				Collections.sort(neighbours, new NeighbourComparator());
				return neighbours;
			}
		}
		return null;
	}

	@Override
	public List<Site> getParentsAtNode(HttpServletRequest request, Session session, String nodeId, boolean includeMyWorksite) {
		if (nodeId != null) {
			PortalNode node = portalHierarchyService.getNodeByUrl(nodeId);
			if (node != null) {
				List<Site> parentSites = portalHierarchyService.getNodesFromRoot(node.getId())
						.stream().map(PortalNodeSite::getSite).collect(Collectors.toList());
				return parentSites;
			}
		}
		return null;
	}


	@Override
	public String lookupSiteAlias(String siteReferenced, String context)
	{
		Map<String, String> cache = (Map<String, String>)ThreadLocalManager.get(CACHE_KEY);
		if (cache == null)
		{
			cache = new HashMap<>();
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
	
	
	String findSiteAlias(String siteReferenced, String context)
	{
		String path = context;
		if (context == null) {
			path = (String)ThreadLocalManager.get("portal.original.siteId");
		}
		if (path != null) {
			// Get the threadlocal, if found use that, otherwise use proxy
			PortalNode node = portalHierarchyService.getNodeByUrl(path);
			if (node instanceof PortalNodeSite) {
				PortalNodeSite siteNode = (PortalNodeSite) node;
				// Need to check current site, then children, then parents.
				if (siteNode.getSite().getReference().equals(siteReferenced) || siteNode.getManagementSite().getReference().equals(siteReferenced)) {
					return siteNode.getUrlPath();
				}
				for (PortalNode child : portalHierarchyService.getNodeChildren(siteNode.getId())) {
					if (child instanceof PortalNodeSite && ((PortalNodeSite) child).getSite().getReference().equals(siteReferenced)) {
						return child.getUrlPath();
					}
				}
				for (PortalNode parent : portalHierarchyService.getNodesFromRoot(siteNode.getId())) {
					if (parent instanceof PortalNodeSite && ((PortalNodeSite) parent).getSite().getReference().equals(siteReferenced)) {
						return parent.getUrlPath();
					}
				}
			}
		}
		// If we aren't at a node but came through hierarchy (eg MyWorkspace URLs).
		String siteId = EntityManager.newReference(siteReferenced).getId();
		PortalNode defaultNode = portalHierarchyService.getDefaultNode(siteId);
		if (defaultNode != null) {
			return defaultNode.getUrlPath();
		}

		return proxy.lookupSiteAlias(siteReferenced, context);
	}

	/**
	 * Lookup site ID.
	 * @param alias An alias for a site ID, at the moment we're supporting parts seperated by colons as this allows
	 *              the existing SiteHandler to continue to work.
	 * @return The site reference for this alias.
	 */
	@Override
	public Site parseSiteAlias(String alias) throws PermissionException {
		if (alias != null) {
			// TODO Have the site handler deal with slashes in siteIds.
			PortalNode node = portalHierarchyService.getNodeByUrl(alias);
			if (node != null && node instanceof  PortalNodeSite) {
				PortalNodeSite siteNode = (PortalNodeSite) node;
				if (!node.canView()) {
					throw new PermissionException(null, null, siteNode.getSite().getReference());
				}
				// We use an AdoptedSite so that the site gets the additional pages from the management site.
				return new AdoptedSite(siteNode.getSite(), siteNode.getManagementSite());
			}

		}
		return  proxy.parseSiteAlias(alias);
	}

	@Override
	public String getRedirect(String siteId) {
		if (siteId != null) {
			PortalNode node = portalHierarchyService.getNodeByUrl(siteId);
			if (node instanceof PortalNodeRedirect) {
				return ((PortalNodeRedirect)node).getUrl();
			}
		}
		return null;
	}

	public void setProxy(SiteNeighbourhoodService proxy)
	{
		this.proxy = proxy;
	}

	public void setPortalHierarchyService(PortalHierarchyService portalHierarchyService)
	{
		this.portalHierarchyService = portalHierarchyService;
	}

	/**
	 * This sorts all the neighbours into alphanetical order based on their title.
	 */
	static class NeighbourComparator implements Comparator<Neighbour> {
		@Override
		public int compare(Neighbour s1, Neighbour s2) {
			String s1Title = or(s1.getSite().map(Site::getTitle), s1.getRedirect().map(Neighbour.Redirect::getTitle))
					.orElseThrow(() -> new IllegalStateException("Must have a redirect or site."));
			String s2Title = or(s2.getSite().map(Site::getTitle), s2.getRedirect().map(Neighbour.Redirect::getTitle))
					.orElseThrow(() -> new IllegalStateException("Must have a redirect or site."));
			return s1Title.compareTo(s2Title);
		}

		/**
		 * Just returns the first present optional or the last one if none are present.
		 */
		static <T> Optional<T> or(Optional<T> first, Optional<T> second) {
			return first.isPresent() ? first : second;
		}
	}
}
