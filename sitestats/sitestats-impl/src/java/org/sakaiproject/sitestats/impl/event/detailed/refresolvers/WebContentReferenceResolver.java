package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.ArrayList;
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
 * Resolves WebContent references into meaningful details.
 *
 * @author bjones86
 */
public class WebContentReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( WebContentReferenceResolver.class );

    // Event types handled by this resolver
    private static final String WEB_CONTENT_READ    = "webcontent.read";
    private static final String WEB_CONTENT_REVISE  = "webcontent.revise";
    private static final String WEB_CONTENT_SERV_READ    = "webcontent.service.read";
    private static final String WEB_CONTENT_SERV_REVISE  = "webcontent.service.revise";
    private static final String WEB_CONTENT_SITE_READ    = "webcontent.site.read";
    private static final String WEB_CONTENT_SITE_REVISE  = "webcontent.site.revise";
    private static final String WEB_CONTENT_WORK_READ    = "webcontent.myworkspace.read";
    private static final String WEB_CONTENT_WORK_REVISE  = "webcontent.myworkspace.revise";

    // Event types handled by this resolver, as a list
    public static final List<String> WEB_CONTENT_RESOLVABLE_EVENTS = Arrays.asList( WEB_CONTENT_READ, WEB_CONTENT_REVISE, WEB_CONTENT_SERV_READ, WEB_CONTENT_SERV_REVISE,
                                                                                    WEB_CONTENT_SITE_READ, WEB_CONTENT_SITE_REVISE, WEB_CONTENT_WORK_READ, WEB_CONTENT_WORK_REVISE );

    /**
     * Resolves a WebContent reference and returns a list of key-value pairs representing meaningful details about the event.
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param siteServ
     * @return a list of ResolvedRef objects containing key-value pairs
     */
    public static List<ResolvedRef> resolveReference( String eventType, String eventRef, SiteService siteServ )
    {
        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || siteServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return Collections.emptyList();
        }

        // Format will always be: /web/<siteID>/id/<toolID>/url/<urlOrFile>
        String[] tokens = eventRef.split( "/" );
        String siteID = tokens[2];
        String toolID = tokens[4];
        String urlOrFile = tokens[6];
        String decodedUrlOrFile = RefResolverUtils.urlDecode( urlOrFile, LOG );

        // Attempt to get additonal details
        List<ResolvedRef> eventDetails = new ArrayList<>( 1 );
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

                RefResolverUtils.addEventDetailsLink( eventDetails, "Page", title, decodedUrlOrFile );
            }
        }

        // Failed to retrieve data for the given ref
        if( eventDetails.isEmpty() )
        {
            LOG.warn( "Unable to retrieve data; ref = " + eventRef );
            return Collections.emptyList();
        }

        // Otherwise, we must have gotten some data, return the list
        else
        {
            return RefResolverUtils.sortEventDetails( eventDetails );
        }
    }
}
