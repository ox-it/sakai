package org.sakaiproject.portal.api;

import org.sakaiproject.site.api.Site;

import java.util.Optional;

/**
 * A neighbour to the current location.
 * This exposed sites and redirects because it's only inside the portal that we know how to encode
 * the title and the ID and the list of neighbours comes from outside the portal.
 */
public interface Neighbour {

    Optional<Site> getSite();

    Optional<Redirect> getRedirect();

    /**
     * A redirect that should be shown to the user.
     */
    interface Redirect {
        /**
         * The ID of the redirect.
         * @return Cannot be <code>null</code>.
         */
        String getId();

        /**
         * The title of the redirect.
         * @return Cannot be <code>null</code>.
         */
        String getTitle();

        /**
         * Should this redirect be shown  to users. A hidden redirect still works for users they just
         * don't know it exists until they access it.
         * @return <code>true</code> if users shouldn't see the redirect.
         */
        boolean isHidden();

    }

}
