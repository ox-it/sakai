package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

/**
 * Resolves Wiki references into meaningful details.
 *
 * @author bjones86
 */
public class WikiReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( WikiReferenceResolver.class );

    // Event types handled by this resolver
    private static final String WIKI_NEW    = "wiki.new";
    private static final String WIKI_READ   = "wiki.read";
    private static final String WIKI_REVISE = "wiki.revise";

    private static final String WIKI_TOOL_ID = "sakai.rwiki";

    // Event types handled by this resolver, as a list
    public static final List<String> WIKI_RESOLVABLE_EVENTS = Arrays.asList( WIKI_NEW, WIKI_READ, WIKI_REVISE );

    // Wiki permissions that we're requiring to resolve references
    private static final String READ_PERM = "rwiki.read";
    public static final List<String> REQUIRED_PERMS = Arrays.asList( READ_PERM );

    /**
     * Resolves a Wiki event reference and returns a list of key-value pairs representing meaningful details about the event.
     *
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param devHlprServ
     * @param siteServ
     * @return a list of ResolvedRef objects containing key-value pairs
     */
    public static List<ResolvedRef> resolveReference( String eventType, String eventRef, DeveloperHelperService devHlprServ, SiteService siteServ )
    {
        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || devHlprServ == null || siteServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return Collections.emptyList();
        }

        // If the event type is "wiki.new" or "wiki.revise", strip off the trailing period in the event ref
        if( WIKI_NEW.equals( eventType ) || WIKI_REVISE.equals( eventType ) )
        {
            eventRef = eventRef.substring( 0, eventRef.length() - 1 );
        }

        // Format will be /wiki/site/<siteID>/<pageName> OR site/<siteID>/<pageName> (for read events)
        String[] tokens = eventRef.split( "/" );
        String siteID;
        String pageName;
        if( WIKI_READ.equals( eventType ) )
        {
            siteID = tokens[2];
            pageName = tokens[3];
        }
        else
        {
            siteID = tokens[3];
            pageName = tokens[4];
        }

        // Build the URL to the page
        List<ResolvedRef> eventDetails = new ArrayList<>( 1 );
        Site site = RefResolverUtils.getSiteByID( siteID, siteServ, LOG );
        if( site != null )
        {
            ToolConfiguration toolConfig = site.getToolForCommonId( WIKI_TOOL_ID );
            if( toolConfig != null )
            {
                String pageNameEncoded = RefResolverUtils.urlEncode( pageName, LOG );
                if( StringUtils.isNotBlank( pageNameEncoded ) )
                {
                    String url = devHlprServ.getPortalURL() + "/tool/" + toolConfig.getId() + "?pageName=/site/" + siteID + "/" + pageNameEncoded
                            + "&action=view&panel=Main&realm=/site/" + siteID;
                    RefResolverUtils.addEventDetailsLink( eventDetails, "Title", pageName, url );
                }
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
