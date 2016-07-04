package org.sakaiproject.portal.impl;

import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.portal.api.PageFilter;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;

import java.util.List;
import java.util.Map;

/**
 * This adds the management site pages to the list of site pages.
 */
public class HierarchyPageFilter implements PageFilter {

    private PortalHierarchyService portalHierarchyService;

    public void setPortalHierarchyService(PortalHierarchyService portalHierarchyService) {
        this.portalHierarchyService = portalHierarchyService;
    }

    @Override
    public List<SitePage> filter(List<SitePage> newPages, Site site) {
        PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();
        if (node != null) {
            // This adds on the pages from the management site.
            Site managementSite = node.getManagementSite();
            managementSite.getPages().stream()
                    .map(page -> new AdoptedSitePage(site, page))
                    .forEachOrdered(newPages::add);
        }
        return newPages;
    }

    @Override
    public List<Map> filterPlacements(List<Map> l, Site site) {
        // Doesn't do anything.
        return l;
    }
}
