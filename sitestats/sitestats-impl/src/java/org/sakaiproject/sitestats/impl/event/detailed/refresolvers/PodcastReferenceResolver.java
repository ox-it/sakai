package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;
import org.sakaiproject.time.cover.TimeService;

/**
 * Resolves Podcast references into meaningful details.
 *
 * @author bjones86
 */
public class PodcastReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( PodcastReferenceResolver.class );

    // Event types handled by this resolver
    private static final String PODCAST_ADD         = "podcast.add";
    private static final String PODCAST_DELETE      = "podcast.delete";
    private static final String PODCAST_READ        = "podcast.read";
    private static final String PODCAST_READ_PUBLIC = "podcast.read.public";
    private static final String PODCAST_READ_SITE   = "podcast.read.site";
    private static final String PODCAST_REVISE      = "podcast.revise";

    // Event types handled by this resolver, as a list
    public static final List<String> PODCAST_RESOLVABLE_EVENTS = Arrays.asList( PODCAST_ADD, PODCAST_DELETE, PODCAST_READ, PODCAST_READ_PUBLIC, PODCAST_READ_SITE, PODCAST_REVISE );

    private static final SimpleDateFormat PODCAST_DATE_FORMATTER = new SimpleDateFormat( "EEEEEE',' dd MMMMM yyyy hh:mm a z" );
    static { PODCAST_DATE_FORMATTER.setTimeZone( TimeService.getLocalTimeZone() ); }

    /**
     * Resolves a Podcast event reference and returns a list of key-value pairs representing meaningful details about the event.
     *
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param podServ
     * @return a list of ResolvedRef objects containing key-value pairs
     */
    public static List<ResolvedRef> resolveReference( String eventType, String eventRef, PodcastService podServ )
    {
        /*
         * Our goal is to return a List with a capacity that matches exactly the amount of data we are returning.
         * But firstly, we'll set our return value - eventDetails - to Collections.emptyList(), this ensures we're returning *something*.
         * Collections.emptyList() is constant (doesn't insantiate anything), and it's immutable.
         */
        List<ResolvedRef> eventDetails = Collections.emptyList();

        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || podServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return eventDetails;
        }

        // The 'read' refs contain nothing other than the site ID, making it useless; short circuit
        if( PODCAST_READ_PUBLIC.equals( eventType ) || PODCAST_READ_SITE.equals( eventType ) || PODCAST_READ.equals( eventType ) )
        {
            return eventDetails;
        }

        // Format of ref string is dependant on the event type, however the site ID is always in the same place:
        // delete and revise format: /content/group/<siteID>/Podcasts/<fileName>
        // add format (notice the double slash): /content/group/<siteID>/Podcasts/<userID>//content/group/<siteID>/Podcasts/<fileName>
        String tokens[] = eventRef.split( "/" );
        String siteID = tokens[3];

        // Attempt to get additional details
        if( StringUtils.isNotBlank( siteID ) )
        {
            List<ContentResource> podcasts = getPodcastsBySiteID( siteID, podServ );
            for( ContentResource resource : podcasts )
            {
                String resourceID = resource.getId();
                String resourceRef;
                if( PODCAST_ADD.equals( eventType ) )
                {
                    String userID = tokens[5];
                    resourceRef = "/content/group/" + siteID + "/Podcasts/" + userID + "//content" + resourceID;
                }
                else
                {
                    resourceRef = "/content" + resourceID;
                }

                if( resourceRef.equals( eventRef ) )
                {
                    // Get the necessary info
                    ContentCollection parent = resource.getContainingCollection();
                    String podcastTitle = RefResolverUtils.getResourceName( resource );
                    String publishDateTime = getPodcastPublishDateTime( resource, podServ );

                    // Dump the details into the return list
                    eventDetails = new ArrayList<>( 2 );
                    RefResolverUtils.addEventDetailsLink( eventDetails, "Title", podcastTitle, parent.getUrl() );
                    RefResolverUtils.addEventDetailsText( eventDetails, "Publish Date/Time", publishDateTime );
                }
            }
        }

        // Failed to retrieve data for the given ref
        if( eventDetails == null || eventDetails.isEmpty() )
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

    /**
     * Convenience method to retrieve a list of ContentResource objects which are podcasts for the given site ID.
     * @param siteID the ID of the site to retrieve all podcasts
     * @param podServ the PodcastService used to perform the retrieval
     * @return All podcasts for the given site ID, or an empty list
     */
    private static List<ContentResource> getPodcastsBySiteID( String siteID, PodcastService podServ )
    {
        try
        {
            return podServ.getPodcasts( siteID );
        }
        catch( PermissionException ex )
        {
            LOG.warn( "Permission exception retrieving podcasts for site: " + siteID, ex );
        }
        catch( InUseException | IdInvalidException | InconsistentException | IdUsedException ex )
        {
            LOG.warn( "Could not retrieve podcasts for site: " + siteID, ex );
        }

        return Collections.emptyList();
    }

    /**
     * Utility method to retrieve the 'published' date of a given Podcast.
     * @param podcastResource The podcast resource
     * @param podServ The Podcast service object
     * @return A string representation of the podcast's publish date/time, matching the format from the Podcast tool
     */
    private static String getPodcastPublishDateTime( ContentResource podcastResource, PodcastService podServ )
    {
        if( podcastResource == null )
        {
            return null;
        }

        Date releaseDateTime;
        if( podcastResource.getReleaseDate() == null )
        {
            try
            {
                ResourceProperties podcastProperties = podcastResource.getProperties();
                releaseDateTime = podServ.getGMTdate( podcastProperties.getTimeProperty( PodcastService.DISPLAY_DATE ).getTime() );
            }
            catch( EntityPropertyNotDefinedException | EntityPropertyTypeException ex )
            {
                LOG.warn( "Unable to retrieve release date for resource with ID = " + podcastResource.getId(), ex );
                return null;
            }
        }
        else
        {
            releaseDateTime = new Date( podcastResource.getReleaseDate().getTime() );
        }

        // Return the formatted date/time
        return PODCAST_DATE_FORMATTER.format( releaseDateTime );
    }
}
