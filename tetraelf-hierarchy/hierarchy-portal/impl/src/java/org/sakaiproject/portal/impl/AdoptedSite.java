package org.sakaiproject.portal.impl;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;

import java.util.ArrayList;
import java.util.List;

/**
 * This adopts the pages of another site into this one.
 * This is used so that we can include the management pages in the site.
 */
public class AdoptedSite extends SiteDelegate {

    private Site other;

    public AdoptedSite(Site site, Site other) {
        super(site);
        this.other = other;
    }

    @Override
    public SitePage getPage(String id) {
        SitePage page = super.getPage(id);
        if (page == null) {
            page = other.getPage(id);
        }
        return page;
    }

    @Override
    public List<SitePage> getPages() {
        List<SitePage> pages = new ArrayList<>();
        pages.addAll(super.getPages());
        pages.addAll(other.getPages());
        return pages;
    }

    @Override
    public List<SitePage> getOrderedPages() {
        List<SitePage> pages = new ArrayList<>();
        pages.addAll(super.getOrderedPages());
        pages.addAll(other.getOrderedPages());
        return pages;
    }
}

