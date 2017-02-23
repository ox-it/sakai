package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;

/**
 * Resolves Poll references into meaningful details.
 *
 * @author bjones86
 */
public class PollReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( PollReferenceResolver.class );

    // Event types handled by this resolver
    private static final String POLL_ADD            = "poll.add";
    private static final String POLL_DELETE         = "poll.delete";
    private static final String POLL_UPDATE         = "poll.update";
    private static final String POLL_VIEW_RESULT    = "poll.viewResult";
    private static final String POLL_VOTE           = "poll.vote";

    // Event types handled by this resolver, as a list
    public static List<String> POLL_RESOLVABLE_EVENTS = Arrays.asList( POLL_ADD, POLL_DELETE, POLL_UPDATE, POLL_VIEW_RESULT, POLL_VOTE );

    /**
     * Resolves a Poll event reference and returns a list of key-value pairs representing meaningful details about the event.
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param pollServ the Polls service to use for retrieving addition information
     * @return
     */
    public static List<ResolvedRef> resolveReference( String eventType, String eventRef, PollListManager pollServ )
    {
        /*
         * Our goal is to return a List with a capacity that matches exactly the amount of data we are returning.
         * But firstly, we'll set our return value - eventDetails - to Collections.emptyList(), this ensures we're returning *something*.
         * Collections.emptyList() is constant (doesn't insantiate anything), and it's immutable.
         */
        List<ResolvedRef> eventDetails = Collections.emptyList();

        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || pollServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return eventDetails;
        }

        // Format will always be /poll/site/<siteID>/poll/<pollID>, except for "poll.vote" which has an extra slash preceeding the site ID: /poll/site//<siteID/...
        eventRef = eventRef.replaceAll( " ", "" );
        String[] tokens = eventRef.split( "/" );
        String pollID;
        if( POLL_VOTE.equals( eventType ) )
        {
            pollID = tokens[6];
        }
        else
        {
            pollID = tokens[4];
        }

        // Short circuit if the poll ID is empty, blank or null
        if( StringUtils.isBlank( pollID ) || "null".equalsIgnoreCase( pollID ) )
        {
            LOG.warn( "Cannot resolve reference. Poll ID is null/empty in event ref: " + eventRef );
            return eventDetails;
        }

        // Parse the ID
        Long id;
        try
        {
            id = Long.parseLong( pollID );
        }
        catch( NumberFormatException ex )
        {
            LOG.warn( "Cannot parse ID = " + pollID + "; ref = " + eventRef, ex );
            return eventDetails;
        }

        // Attempt to resolve the event details
        Poll poll = pollServ.getPollById( id, false );
        if( poll != null )
        {
            return Collections.singletonList( ResolvedRef.newText( "Title", poll.getText() ) );
        }

        // Failed to retrieve data for the given ref
        LOG.warn( "Unable to retrieve data; ref = " + eventRef );
        return eventDetails;
    }
}
