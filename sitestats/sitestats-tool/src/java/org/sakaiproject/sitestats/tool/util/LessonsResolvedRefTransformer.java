package org.sakaiproject.sitestats.tool.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.StatsDates;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentsSectionItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.EmbeddedItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.GenericItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.PageData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.TextItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.ContentLinkItemData;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.time.api.TimeService;

/**
 * Class to temporarily convert a ResolvedEventData object into a List<ResolvedRef> so that we
 * don't need to change the presentation code or any of the other ref resolvers at this time.
 *
 * View-layer logic for presenting the data contained in the ResolvedEventData object lives here and will probably remain,
 * even if we change the mechanism of presentation from a simple K/V list.
 *
 * @author bjones86
 */
public class LessonsResolvedRefTransformer
{
    private static final String MSG_TEMPLATE    = "\"%s\" by %s on %s";
    private static final int    TRUNCATE_LENGTH = 50;  // OWLTODO: sakai.property?

    /**
     *
     * @param resolved
     * @param eventType
     * @return
     */
    public static List<ResolvedRef> transform( ResolvedEventData resolved, String eventType )
    {
        // OWLTODO: localization would go in this area

        List<ResolvedRef> eventDetails = null;
        if( resolved instanceof ResolvedEventData.PermissionError )
        {
            eventDetails = new ArrayList<>( 1 );
            addEventDetailsText( eventDetails, "Error", "You do not have the required permissions to view details of this event.");
        }
        else if( resolved instanceof TextItemData )
        {
            TextItemData data = (TextItemData) resolved;
            eventDetails = new ArrayList<>( 2 );
            addEventDetailsText( eventDetails, "Text Item", StringUtils.abbreviate( data.getHTML(), TRUNCATE_LENGTH ) );
            addEventDetailsText( eventDetails, "Page", getPageDisplay(data.getParentPage()) );
        }
        else if( resolved instanceof EmbeddedItemData )
        {
            EmbeddedItemData data = (EmbeddedItemData) resolved;
            eventDetails = new ArrayList<>( 3 );
            addEventDetailsText( eventDetails, "Item", "Embedded Content" );
            if( !data.getDescription().isEmpty() )
            {
                addEventDetailsText( eventDetails, "Description", StringUtils.abbreviate( data.getDescription(), TRUNCATE_LENGTH ) );
            }

            addEventDetailsText( eventDetails, "Page", getPageDisplay(data.getParentPage()) );
        }
        else if( resolved instanceof ContentLinkItemData )
        {
            ContentLinkItemData data = (ContentLinkItemData) resolved;
            eventDetails = new ArrayList<>( 2 );
            addEventDetailsText( eventDetails, "Content Link", data.getName());
            addEventDetailsText( eventDetails, "Page", getPageDisplay(data.getParentPage()));
        }
        else if( resolved instanceof GenericItemData )
        {
            GenericItemData data = (GenericItemData) resolved;
            if (GenericItemData.DELETED_ITEM.equals(data))
            {
                // we don't know if this was actually a top-level PAGE item or a regular item
                eventDetails = Collections.singletonList(ResolvedRef.newText("Item", "[Deleted Page or Item]")); // OWLTODO: localize
            }
            else
            {
                eventDetails = new ArrayList<>( 2 );
                addEventDetailsText( eventDetails, "Item", data.getTitle() );
                addEventDetailsText( eventDetails, "Page", getPageDisplay(data.getParentPage()));
            }
        }
        else if( resolved instanceof PageData )
        {
            PageData page = (PageData) resolved;
            if( PageData.DELETED_PAGE.equals( page ) )
            {
                eventDetails = Collections.singletonList( ResolvedRef.newText( "Page", "[Deleted Page]" ) ); // OWLTODO: localize
            }
            else
            {
                ResolvedRef pageRef = ResolvedRef.newText( "Page", page.getTitle() );
                if( PageData.TOP_LEVEL.equals( page.getHierarchy() ) )
                {
                    eventDetails = Collections.singletonList( pageRef );
                }
                else
                {
                    eventDetails = new ArrayList<>( 2 );
                    eventDetails.add( pageRef );
                    eventDetails.add( ResolvedRef.newText( "Hierarchy", page.getHierarchy() ) );
                }
            }
        }
        else if( resolved instanceof CommentData )
        {
            CommentData data = (CommentData) resolved;
            eventDetails = new ArrayList<>( 2 );

            TimeService timeServ = Locator.getFacade().getTimeService();
            String comment = StringUtils.trimToEmpty(StringUtils.abbreviate( data.getComment(), TRUNCATE_LENGTH ));
            if( comment.isEmpty() )
            {
                comment = "[Comment was deleted or contains no text]";  // OWLTODO: localize
            }

            String timePosted = StatsDates.formatForDisplay( data.getTimePosted(), timeServ );
            String message = String.format( MSG_TEMPLATE, comment, data.getAuthor(), timePosted );
            addEventDetailsText( eventDetails, "Comment", message );
            addEventDetailsText( eventDetails, "Page", getPageDisplay(data.getParentPage()) );
        }
        else if( resolved instanceof CommentsSectionItemData )
        {
            eventDetails = new ArrayList<>( 2 );
            eventDetails.add( ResolvedRef.newText( "Item", "Comments Section" ) );

            CommentsSectionItemData comments = (CommentsSectionItemData) resolved;
            if( comments.isStudentPageComments() )
            {
                String note = "This is an automatically generated comments section placeholder. All student pages have such a placeholder, even if comments are not enabled on the page.";
                ResolvedRef ref = ResolvedRef.newText( "Note", note );
                eventDetails.add( ref );
            }
            else
            {
                eventDetails.add( ResolvedRef.newText( "Page", comments.getParentPage().getHierarchy() ) );
            }
        }

        // No data found, return empty list
        if( eventDetails == null || eventDetails.isEmpty() )
        {
            return Collections.emptyList();
        }
        else
        {
            return eventDetails;
        }
    }

    /**
     * Add a new text ResolvedRef to the list, provided that both the key and value are not empty/blank/null
     * @param eventDetails the list to add the ResolvedRef to
     * @param key the key to be added
     * @param displayValue the display value to be used for the link
     * @param url the URL to be added
     */
    public static void addEventDetailsLink( List<ResolvedRef> eventDetails, String key, String displayValue, String url )
    {
        if( StringUtils.isNotBlank( key ) && StringUtils.isNotBlank( displayValue ) && StringUtils.isNotBlank( url ) )
        {
            eventDetails.add( ResolvedRef.newLink( key, displayValue, url ) );
        }
    }

    /**
     * Add a new text ResolvedRef to the list, provided that both the key and value are not empty/blank/null
     * @param eventDetails the list to add the ResolvedRef to
     * @param key the key to be added
     * @param value the value to be added
     */
    private static void addEventDetailsText( List<ResolvedRef> eventDetails, String key, String value )
    {
        if( StringUtils.isNotBlank( key ) && StringUtils.isNotBlank( value ) )
        {
            eventDetails.add( ResolvedRef.newText( key, value ) );
        }
    }

    /**
     * Returns the page title for a top level page, or the full page hierarchy for sub-pages
     * @param page
     * @return the page title, or full page hierarchy for sub-pages
     */
    private static String getPageDisplay( PageData page )
    {
        return page.isTopLevel() ? page.getTitle() : page.getHierarchy();
    }
}
