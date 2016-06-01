package org.sakaiproject.portal.service;

import org.sakaiproject.portal.api.PageFilter;
import org.sakaiproject.site.api.Site;

import java.util.List;
import java.util.Map;

/**
 * Page filter that just passes everything through
 */
public class PassingPageFilter implements PageFilter {

    public List filter(List newPages, Site site) {
        return newPages;
    }

    public List<Map> filterPlacements(List<Map> l, Site site) {
        return l;
    }

}
