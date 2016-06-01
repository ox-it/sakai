package org.sakaiproject.portal.impl;

import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.SiteAliasProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This looks up alias for sites in the hierarchy. If a site is located in multiple places in the hierarchy then
 * it attempts to find one close to the current site (parents and children).
 */
public class HierarchySiteAliasProvider implements SiteAliasProvider {

	private PortalHierarchyService portalHierarchyService;
	
	public void setPortalHierarchyService(
			PortalHierarchyService portalHierarchyService) {
		this.portalHierarchyService = portalHierarchyService;
	}

	public String lookupAlias(String siteId) {
		PortalNodeSite currentPortalNode = portalHierarchyService.getCurrentPortalNode();
		if (currentPortalNode != null) {
			List<PortalNodeSite> closeNodes = new ArrayList<>();
			closeNodes.addAll(portalHierarchyService.getNodesFromRoot(currentPortalNode.getId()));
			portalHierarchyService.getNodeChildren(currentPortalNode.getId()).stream()
					.filter(node -> node instanceof PortalNodeSite)
					.map(portalNode -> (PortalNodeSite) portalNode)
					.forEachOrdered(closeNodes::add);
			for (PortalNodeSite node : closeNodes) {
				if (node.getSite().getId().equals(siteId)) {
					return node.getUrlPath();
				}
			}
		}
		PortalNode defaultNode = portalHierarchyService.getDefaultNode(siteId);
		if (defaultNode != null) {
			String path = defaultNode.getUrlPath();
			return path;
		}
		return null;
	}

}
