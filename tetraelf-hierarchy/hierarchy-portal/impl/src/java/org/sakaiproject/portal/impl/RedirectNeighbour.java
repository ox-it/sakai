package org.sakaiproject.portal.impl;

import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.portal.api.Neighbour;
import org.sakaiproject.site.api.Site;

import java.util.Optional;

/**
 * Neighbour that is a redirect to somewhere else.
 */
public class RedirectNeighbour implements Neighbour {

    private PortalNodeRedirect redirect;

    public RedirectNeighbour(PortalNodeRedirect redirect) {
        this.redirect = redirect;
    }

    @Override
    public Optional<Site> getSite() {
        return Optional.empty();
    }

    @Override
    public Optional<Redirect> getRedirect() {
        return Optional.of(new Redirect(){

            @Override
            public String getId() {
                return redirect.getUrlPath();
            }

            @Override
            public String getTitle() {
                // Hidden redirects don't have titles.
                if (redirect.getTitle() == null) {
                    return redirect.getName();
                } else {
                    return redirect.getTitle();
                }
            }
        });
    }
}
