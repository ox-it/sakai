package org.sakaiproject.portal.impl;

import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.site.api.SiteAliasProvider;

public class HierarchySiteAliasProvider implements SiteAliasProvider {

	private PortalHierarchyService portalHierarchyService;
	
	public void setPortalHierarchyService(
			PortalHierarchyService portalHierarchyService) {
		this.portalHierarchyService = portalHierarchyService;
	}

	public String lookupAlias(String siteId) {
		PortalNode defaultNode = portalHierarchyService.getDefaultNode(siteId);
		if (defaultNode != null) {
			String path = defaultNode.getPath();
			// Trim the leading slash
			if (path != null && path.length() > 0) {
				return path.substring(1);
			}
		}
		return null;
	}

}
