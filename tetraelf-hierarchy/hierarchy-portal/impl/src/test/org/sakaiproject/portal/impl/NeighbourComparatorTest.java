package org.sakaiproject.portal.impl;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.portal.api.Neighbour;
import org.sakaiproject.portal.api.SiteNeighbour;
import org.sakaiproject.site.api.Site;

import java.util.Arrays;
import java.util.List;

/**
 * Created by buckett on 23/05/2016.
 */
public class NeighbourComparatorTest {

    @Test
    public void testSimpleSort() {
        PortalNodeRedirect redirect = Mockito.mock(PortalNodeRedirect.class);
        Mockito.when(redirect.getTitle()).thenReturn("Redirect");
        RedirectNeighbour redirectNeighbour = new RedirectNeighbour(redirect);

        Site site = Mockito.mock(Site.class);
        Mockito.when(site.getTitle()).thenReturn("Site");
        SiteNeighbour siteNeighbour = new SiteNeighbour(site);

        List<Neighbour> neighbours = Arrays.asList(new Neighbour[]{siteNeighbour, redirectNeighbour});
        neighbours.sort(new HierarchySiteNeighbourhoodService.NeighbourComparator());
    }

}
