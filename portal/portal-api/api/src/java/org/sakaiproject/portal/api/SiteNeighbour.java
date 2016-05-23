package org.sakaiproject.portal.api;

import org.sakaiproject.site.api.Site;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a site related to the current one.
 */
public class SiteNeighbour implements Neighbour {

    private Site site;

    public SiteNeighbour(Site site){
        this.site = Objects.requireNonNull(site);
    }

    @Override
    public Optional<Site> getSite() {
        return Optional.of(site);
    }

    @Override
    public Optional<Redirect> getRedirect() {
        return Optional.empty();
    }


}
