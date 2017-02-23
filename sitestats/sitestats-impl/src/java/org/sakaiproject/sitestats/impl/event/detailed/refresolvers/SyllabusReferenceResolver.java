package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;

/**
 * Resolves Syllabus references into meaningful details.
 *
 * @author bjones86
 */
public class SyllabusReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( SyllabusReferenceResolver.class );

    // Event types handled by this resolver
    private static final String SYLLABUS_DELETE         = "syllabus.delete";
    private static final String SYLLABUS_DRAFT_CHANGE   = "syllabus.draft.change";
    private static final String SYLLABUS_DRAFT_NEW      = "syllabus.draft.new";
    private static final String SYLLABUS_POST_CHANGE    = "syllabus.post.change";
    private static final String SYLLABUS_POST_NEW       = "syllabus.post.new";
    private static final String SYLLABUS_READ           = "syllabus.read";

    // Event types handled by this resolver, as a list
    public static final List<String> SYLLABUS_RESOLVABLE_EVENTS = Arrays.asList( SYLLABUS_DELETE, SYLLABUS_DRAFT_CHANGE, SYLLABUS_DRAFT_NEW, SYLLABUS_POST_CHANGE,
                                                                                 SYLLABUS_POST_NEW, SYLLABUS_READ );

    /**
     * Resolves a Syllabus reference and returns a list of key-value pairs representing meaningful details about the event.
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param syllMan
     * @return a list of ResolvedRef objects containing key-value pairs
     */
    public static List<ResolvedRef> resolveReference( String eventType, String eventRef, SyllabusManager syllMan )
    {
        /*
         * Our goal is to return a List with a capacity that matches exactly the amount of data we are returning.
         * But firstly, we'll set our return value - eventDetails - to Collections.emptyList(), this ensures we're returning *something*.
         * Collections.emptyList() is constant (doesn't insantiate anything), and it's immutable.
         */
        List<ResolvedRef> eventDetails = Collections.emptyList();

        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || syllMan == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return eventDetails;
        }

        // Format will always be: /syllabus/<siteID>/<syllabusDataID>
        String[] tokens = eventRef.split( "/" );
        String siteID = tokens[2];
        String syllabusDataID = tokens[3];

        // Attempt to get additonal details
        SyllabusData data = syllMan.getSyllabusData( syllabusDataID );
        if( data != null )
        {
            // If it's a "syllabus.read" event, the data ID is 1, and the item's site ID does note match the site ID from the event,
            // then this isn't a read of a particular item, but rather a user accessing the Syllabus tool in the event's site.
            // For whatever reason, the code in Syllabus always appends the ID of '1' for this type of access, which can be ambiguous
            // becausea SyllabusData item can also have an ID of 1, so we need to make sure that the item with ID of "1" doesn't
            // actually belong to the event's site ID
            if( SYLLABUS_READ.equals( eventType ) )
            {
                Long syllabusID = data.getSyllabusId();
                SyllabusItem item = syllMan.getSyllabusItem( syllabusID );
                String itemsSiteID = item.getContextId();

                // If the IDs dont' match, then it's a read of the entire tool, rather than a specific item
                if( !siteID.equals( itemsSiteID ) )
                {
                    return Collections.singletonList( ResolvedRef.newText( "Action", "Opened Syllabus Tool" ) );
                }
                else
                {
                    return Collections.singletonList( ResolvedRef.newText( "Title", data.getTitle() ) );
                }
            }
            else
            {
                return Collections.singletonList( ResolvedRef.newText( "Title", data.getTitle() ) );
            }
        }

        // Failed to retrieve data for the given ref
        LOG.warn( "Unable to retrieve data; ref = " + eventRef );
        return eventDetails;
    }
}
