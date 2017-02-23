package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.sitestats.api.UserModel;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.EmbeddedItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.GenericItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.TextItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.ContentLinkItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentsSectionItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.PageData;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.FormattedText;

/**
 * Resolves LessonBuilder references into meaningful details.
 *
 * @author bjones86
 */
public class LessonsReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( LessonsReferenceResolver.class );

    // Event types handled by this resolver
    private static final String LSNBLDR_CREATE  = "lessonbuilder.create";
    private static final String LSNBLDR_DELETE  = "lessonbuilder.delete";
    private static final String LSNBLDR_READ    = "lessonbuilder.read";
    private static final String LSNBLDR_REMOVE  = "lessonbuilder.remove";
    private static final String LSNBLDR_UPDATE  = "lessonbuilder.update";

    // Event ref distinguishers
    private static final String PAGE_REF_IDENTIFIER     = "/page/";
    private static final String ITEM_REF_IDENTIFIER     = "/item/";
    private static final String COMMENT_REF_IDENTIFIER  = "/comment/";

    // Sakai properties
    public static final String SAK_PROP_LSNBLDR_READ_CUTOVER_DATE = "sitestats.refResolver.lessonbuilder.read.cutoverDate";

    // Event types handled by this resolver, as a list
    public static final List<String> LESSONS_RESOLVABLE_EVENTS = Arrays.asList( LSNBLDR_CREATE, LSNBLDR_DELETE, LSNBLDR_READ, LSNBLDR_REMOVE, LSNBLDR_UPDATE );

    // Lessons permissions that we're requiring to resolve references
    private static final String READ_PERM = "lessonbuilder.read";
    public static final List<String> REQUIRED_PERMS = Arrays.asList( READ_PERM );

    /**
     * Resolves a LessonBuilder event reference and returns a list of key-value pairs representing meaningful details about the event.
     * In the case of LessonBuilder event refs, they are always the same format, so we don't have to worry about which event type the caller is asking to resolve;
     * we resolve them all the same way.
     *
     * @param eventType the type of event to be processed
     * @param eventRef the event ref string to be processed
     * @param eventDate the date the event occurred on
     * @param lsnServ the LessonBuilder service to use for retrieving addition information
     * @param userDirServ the UserDirectoryService object to use for performing user lookups
     * @param lessonsReadEventCutoverDate the cutover date to use when resolving lessonbuilder.read events
     * @return a list of key-value pairs representing meaningful information about the event given
     */
    public static ResolvedEventData resolveReference( String eventType, String eventRef, Date eventDate, SimplePageToolDao lsnServ,
                                                      UserDirectoryService userDirServ, Date lessonsReadEventCutoverDate )
    {
        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || lsnServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.NO_DATA;
        }

        // If the event type is "lessonbuilder.read", and the event date is NOT after the cutover date, abort resolving this ref (OWL-2407)
        if( LSNBLDR_READ.equals( eventType ) && eventDate != null && lessonsReadEventCutoverDate != null )
        {
            if( !eventDate.after( lessonsReadEventCutoverDate ) )
            {
                LOG.warn( LSNBLDR_READ + " event date (" + eventDate + ") is not after cutover date (" + lessonsReadEventCutoverDate + "); aborting ref resolver." );
                return ResolvedEventData.NO_DATA;
            }
        }

        // If the event type is "lessonbuilder.read", and the event date or the cutover date is null, abort resolving this ref (OWL-2407)
        else if( LSNBLDR_READ.equals( eventType ) && (eventDate == null || lessonsReadEventCutoverDate == null) )
        {
            LOG.warn( "sakai.property " + SAK_PROP_LSNBLDR_READ_CUTOVER_DATE + " not set or has invalid format; aborting ref resolver" );
            return ResolvedEventData.NO_DATA;
        }

        // Format will always be: /lessonbuilder/<page|item|comment>/<ID>
        String[] tokens = eventRef.split( "/" );
        long id;
        try
        {
            id = Long.parseLong( tokens[3] );
        }
        catch( NumberFormatException ex )
        {
            LOG.warn( "Cannot parse ID = " + tokens[3] + "; ref = " + eventRef, ex );
            return ResolvedEventData.NO_DATA;
        }

        // Determine what type of object the event reference refers to, and the get details appropriately
        if( eventRef.contains( PAGE_REF_IDENTIFIER ) )
        {
            SimplePage page = lsnServ.getPage( id );
            if( page != null )
            {
                return collectPageEventDetails( page, lsnServ );
            }
            else if( id > 0 )
            {
                return PageData.DELETED_PAGE;
            }
        }
        else if( eventRef.contains( ITEM_REF_IDENTIFIER ) )
        {
            SimplePageItem item = lsnServ.findItem( id );
            if( item != null )
            {
               return collectItemEventDetails( item, lsnServ );
            }
            else if( id > 0 )
            {
                if( LSNBLDR_READ.equals( eventType ) )
                {
                    // We know that read events are never fired for the items on a page, so this must have been
                    // an item of type PAGE. Therefore, treat it like a deleted page.
                    return PageData.DELETED_PAGE;
                }

                return GenericItemData.DELETED_ITEM;
            }
        }
        else if( eventRef.contains( COMMENT_REF_IDENTIFIER ) )
        {
            SimplePageComment comment = lsnServ.findCommentById( id );
            if( comment != null )
            {
                return collectCommentEventDetails( comment, lsnServ, userDirServ );
            }
        }

        // Failed to retrieve data for the given ref
        LOG.warn( "Unable to retrieve data; ref = " + eventRef );
        return ResolvedEventData.NO_DATA;
    }

    /**
     * Aggregate all appropriate details of the page.
     * @param page the page object to retrieve data from
     * @param lsnServ the LessonBuilder service to use for additional data retrieval
     * @return a list of ResolvedRef objects containing details about the event
     */
    private static PageData collectPageEventDetails( SimplePage page, SimplePageToolDao lsnServ )
    {
        // Add the necessary details for the page
        return new PageData( page.getTitle(), getPageHierarchyString( page, lsnServ ) );
    }

    /**
     * Aggregate all appropriate details of the item.
     * @param item the item object to retrieve data from
     * @param lsnServ the LessonBuilder service to use for additional data retrieval
     * @return a list of ResolvedRef objects containing details about the event
     */
    private static ResolvedEventData collectItemEventDetails( SimplePageItem item, SimplePageToolDao lsnServ )
    {
        // Attempt to get the parent page and hierarchy
        SimplePage parentPage = lsnServ.getPage( item.getPageId() );
        String hierarchy = getPageHierarchyString( parentPage, lsnServ );

        // Build the return object conditionally based on the 'type' of item
        switch( item.getType() )
        {
            case SimplePageItem.TEXT:
                return new TextItemData( FormattedText.stripHtmlFromText( item.getHtml(), false, true ),  new PageData( parentPage.getTitle(), hierarchy ) );

            case SimplePageItem.MULTIMEDIA:
                String desc = StringUtils.trimToEmpty( item.getDescription() );
                return new EmbeddedItemData( desc, new PageData( parentPage.getTitle(), hierarchy ) );

            // Chuck has notes in PROGRAMMER.NOTES indicating that for whatever reason he creates 'content' items as of the resource type,
            // despite the fact that they are in reality URLs. He goes on to say that 'this may be a mistake', however he has done nothing to address this.
            // So for the time being, we need to create 'URL' data objects when the item object is of the type 'resource'...
            case SimplePageItem.RESOURCE:
                return new ContentLinkItemData( item.getName(), new PageData( parentPage.getTitle(), hierarchy ) );

            case SimplePageItem.PAGE:
                String hier = item.getPageId() > 0  ? hierarchy : PageData.TOP_LEVEL;
                return new PageData( item.getName(), hier );  // technically an item, treat as a page

            case SimplePageItem.COMMENTS:
                if( item.getPageId() == -1 )
                {
                    // "forced" comments section on a student page, we can't determine which page
                    return new CommentsSectionItemData( null, true );
                }

                return new CommentsSectionItemData( new PageData( parentPage.getTitle(), hierarchy ), false );

            /* Unimplemeted cases:
            case SimplePageItem.ASSESSMENT:
            case SimplePageItem.ASSIGNMENT:
            case SimplePageItem.BLTI:
            case SimplePageItem.FORUM:
            case SimplePageItem.PEEREVAL:
            case SimplePageItem.QUESTION:
            case SimplePageItem.URL:
            case SimplePageItem.STUDENT_CONTENT:
            */

            // Default behaviour for all above unimplemented and not defined cases:
            default:
                return new GenericItemData( item.getName(), new PageData( parentPage.getTitle(), hierarchy ) );
        }
    }

    /**
     * Aggregate all appropriate details of the comment.
     * @param comment the comment object to retrieve data from
     * @param lsnServ the LessonBuilder service to use for additional data retrieval
     * @param userDirServ the UserDirectoryService object used to perform user lookups
     * @return a list of ResolvedRef objects containing details about the event
     */
    private static CommentData collectCommentEventDetails( SimplePageComment comment, SimplePageToolDao lsnServ, UserDirectoryService userDirServ )
    {
        // Get the top parent
        SimplePage parentPage = lsnServ.getPage( comment.getPageId() );

        // Get the author
        String author;
        try
        {
            UserModel user = new UserModel( userDirServ.getUser( comment.getAuthor() ) );
            author = user.getDisplayValue();
        }
        catch( UserNotDefinedException ex )
        {
            author = "User no longer exists";
            LOG.debug( "Can't get user by ID = " + comment.getAuthor(), ex );
        }

        return new CommentData( author, FormattedText.stripHtmlFromText( comment.getComment(), false, true ),
                new PageData( parentPage.getTitle(), getPageHierarchyString( parentPage, lsnServ ) ), comment.getTimePosted() );
    }

    /**
     * Get the page hierarchy string from the top level to the given page.
     * @param page the page we want to traverse the hierarchy from
     * @param lsnServ the LessonBuilder service to use for accessing the hierarchy information
     * @return a hierarchy string in the format of "Top Level Page > Sub Page [repeated as necessary] > Given Page"
     */
    private static String getPageHierarchyString( SimplePage page, SimplePageToolDao lsnServ )
    {
        // OWLTODO: this is presentation logic that should be moved to the view layer
        List<String> hierarchy = getPageHierarchy( page, lsnServ );
        Collections.reverse( hierarchy );
        return StringUtils.join( hierarchy, " > " );
    }

    /**
     * Accumulates a list of string representing the hierarchy of pages starting from the given page and traversing upwards.
     * @param page the page we want to traverse the hierarchy from
     * @param lsnServ the LessonBuilder service to use for accessing the hierarchy information
     * @return a List of strings representing the hierarchy in reverse order (bottom to top)
     */
    private static List<String> getPageHierarchy( SimplePage page, SimplePageToolDao lsnServ )
    {
        List<String> hierarchy = new ArrayList<>();

        if( page != null )
        {
            hierarchy.add( page.getTitle() );
            Long parentPageID = page.getParent();

            if( parentPageID != null )
            {
                SimplePage parent = lsnServ.getPage( parentPageID );
                hierarchy.addAll( getPageHierarchy( parent, lsnServ ) );
            }
        }
        else
        {
            hierarchy.add( "Page no longer exists" );
        }

        return hierarchy;
    }
}
