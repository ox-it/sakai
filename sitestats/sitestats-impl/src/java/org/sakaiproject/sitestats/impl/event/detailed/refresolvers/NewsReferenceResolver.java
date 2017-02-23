package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

/**
 * Resolves News references into meaningful details.
 *
 * @author bjones86
 */
public class NewsReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( NewsReferenceResolver.class );

    // Event types handled by this resolver
    private static final String NEWS_READ   = "news.read";
    private static final String NEWS_REVISE = "news.revise";

    // Event types handled by this resolver, as a list
    public static final List<String> NEWS_RESOLVABLE_EVENTS = Arrays.asList( NEWS_READ, NEWS_REVISE );

    /**
     * Resolves a News reference and returns a list of key-value pairs representing meaningful details about the event.
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param siteServ
     * @return a list of ResolvedRef objects containing key-value pairs
     */
    public static List<ResolvedRef> resolveReference( String eventType, String eventRef, SiteService siteServ )
    {
        /*
         * Our goal is to return a List with a capacity that matches exactly the amount of data we are returning.
         * But firstly, we'll set our return value - eventDetails - to Collections.emptyList(), this ensures we're returning *something*.
         * Collections.emptyList() is constant (doesn't insantiate anything), and it's immutable.
         */
        List<ResolvedRef> eventDetails = Collections.emptyList();

        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || siteServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return eventDetails;
        }

        // Format will always be: /news/site/<siteID>/placement/<toolID>
        String[] tokens = eventRef.split( "/" );
        String siteID = tokens[3];
        String toolID = tokens[5];

        // Attempt to get additional details
        Site site = RefResolverUtils.getSiteByID( siteID, siteServ, LOG );
        if( site != null )
        {
            ToolConfiguration toolConfig = site.getTool( toolID );
            if( toolConfig != null )
            {
                String toolTitle = toolConfig.getTitle();
                String pageTitle = toolConfig.getContainingPage().getTitle();

                String title;
                if( toolTitle.equalsIgnoreCase( pageTitle ) )
                {
                    title = pageTitle;
                }
                else
                {
                    title = pageTitle + " - " + toolTitle;
                }

                return Collections.singletonList( ResolvedRef.newText( "Title", title ) );
            }
        }

        // Failed to retrieve data for the given ref
        LOG.warn( "Unable to retrieve data; ref = " + eventRef );
        return eventDetails;
    }
}
