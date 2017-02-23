package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;

/**
 * Resolves Announcement references into meaningful details.
 *
 * @author bjones86
 */
public class AnnouncementReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( AnnouncementReferenceResolver.class );

    // Event types handled by this resolver
    private static final String ANNC_DELETE_ANY = "annc.delete.any";
    private static final String ANNC_DELETE_OWN = "annc.delete.own";
    private static final String ANNC_REVISE_ANY = "annc.revise.any";
    private static final String ANNC_REVISE_OWN = "annc.revise.own";
    private static final String ANNC_NEW        = "annc.new";

    // Event types handled by this resolver, as a list
    public static List<String> ANNC_RESOLVABLE_EVENTS = Arrays.asList( ANNC_DELETE_ANY, ANNC_DELETE_OWN, ANNC_NEW, ANNC_REVISE_ANY, ANNC_REVISE_OWN );

    /**
     * Resolves an Announcement reference and returns a list of key-value pairs representing meaningful details about the event.
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param anncServ the Announcements service object to use for retrieving additional information
     * @return a list of ResolvedRef objects containing key-value pairs
     */
    public static List<ResolvedRef> resolveReference( String eventType, String eventRef, AnnouncementService anncServ )
    {
        /*
         * Our goal is to return a List with a capacity that matches exactly the amount of data we are returning.
         * But firstly, we'll set our return value - eventDetails - to Collections.emptyList(), this ensures we're returning *something*.
         * Collections.emptyList() is constant (doesn't insantiate anything), and it's immutable.
         */
        List<ResolvedRef> eventDetails = Collections.emptyList();

        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || anncServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return eventDetails;
        }

        // Format will always be: /announcment/msg/<siteID>/<channelID>/<messageID>
        String[] tokens = eventRef.split( "/" );
        String siteID = tokens[3];
        String channelID = tokens[4];
        String messageID = tokens[5];
        String channelRef = "/announcement/channel/" + siteID + "/" + channelID;

        // Attempt to get additional details
        AnnouncementChannel channel = getChannelByRef( channelRef, anncServ );
        if( channel != null )
        {
            AnnouncementMessage message = getMessageByID( channel, messageID );
            if( message != null )
            {
                return Collections.singletonList( ResolvedRef.newText( "Title", message.getAnnouncementHeader().getSubject() ) );
            }
        }

        // Failed to retrieve data for the given ref
        LOG.warn( "Unable to retrieve data; ref = " + eventRef );
        return eventDetails;
    }

    /**
     * Get the AnnouncementMessage object for the given channel by it's ID.
     * @param channel the channel to retrieve the message from
     * @param messageID the ID of the message to retrieve from the channel
     * @return the AnnouncementMessage asked for or null if it can't be found
     */
    private static AnnouncementMessage getMessageByID( AnnouncementChannel channel, String messageID )
    {
        AnnouncementMessage message = null;
        try
        {
            message = channel.getAnnouncementMessage( messageID );
        }
        catch( IdUnusedException ex )
        {
            LOG.warn( "Unable to retrieve AnnouncementMessage by ID: " + messageID, ex );
        }
        catch( PermissionException ex )
        {
            LOG.warn( "Permission exception trying to retrieve AnnouncementMessage by ID: " + messageID, ex );
        }

        return message;
    }

    /**
     * Get the AnnouncementChannel object by it's reference string.
     * @param ref the reference string to use for retrieval
     * @param anncServ the Announcements service object used to perform the retrieval
     * @return the AnnouncementChannel object asked for, or null if it can't be found
     */
    private static AnnouncementChannel getChannelByRef( String ref, AnnouncementService anncServ )
    {
        AnnouncementChannel channel = null;
        try
        {
            channel = anncServ.getAnnouncementChannel( ref );
        }
        catch( IdUnusedException ex )
        {
            LOG.warn( "Unable to retrieve AnnouncementChannel by ref: " + ref, ex );
        }
        catch( PermissionException ex )
        {
            LOG.warn( "Permission exception trying to retrieve AnnouncementChannel by ref: " + ref, ex );
        }

        return channel;
    }
}
