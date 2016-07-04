package org.sakaiproject.portal.impl;

import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.site.api.Site;

/**
 * This is to make a redirect look like a site.
 */
public class RedirectSite  extends  SiteDelegate {

    private PortalNodeRedirect redirect;

    public RedirectSite(Site delegate, PortalNodeRedirect redirect) {
        super(delegate);
        this.redirect = redirect;
    }

    @Override
    public String getId() {
        return redirect.getUrlPath();
    }

    @Override
    public String getTitle() {
        return redirect.getTitle();
    }

}
