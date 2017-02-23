package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.calendar.CalendarEntryData;

/**
 * Resolves Calendar references into meaningful details.
 *
 * @author bjones86
 */
public class CalendarReferenceResolver
{
    private static final Log LOG = LogFactory.getLog( CalendarReferenceResolver.class );

    // Event types handled by this resolver
    private static final String CAL_NEW     = "calendar.new";
    private static final String CAL_REVISE  = "calendar.revise";

    // Event types handled by this resolver, as a list
    public static List<String> CAL_RESOLVABLE_EVENTS = Arrays.asList( CAL_NEW, CAL_REVISE );

    public static ResolvedEventData resolveEventReference( final String eventType, final String eventRef, CalendarService calServ )
    {
        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || calServ == null )
        {
            LOG.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.NO_DATA;
        }

        // Format will always be: /calendar/event/<siteID>/<calendarID>/<eventID>
        String[] tokens = eventRef.split( "/" );
        String siteID = tokens[3];
        String calendarID = tokens[4];
        String eventID = tokens[5];
        String calendarRef = "/calendar/calendar/" + siteID + "/" + calendarID;

        // Attempt to get additional details
        Calendar cal = getCalendarByRef( calendarRef, calServ );
        if( cal != null )
        {
            CalendarEvent calEvent = getEventByID( cal, eventID );
            if( calEvent != null )
            {
                int interval = 1;
                String frequencyUnit = CalendarEntryData.FREQ_ONCE;
                RecurrenceRule recRule = calEvent.getRecurrenceRule();
                if( recRule != null )
                {
                    interval = recRule.getInterval();
                    frequencyUnit = recRule.getFrequencyDescription();
                }

                CalendarEntryData calEntryData = new CalendarEntryData( calEvent.getDisplayName(), calEvent.getRange(), interval, frequencyUnit );
                return calEntryData;
            }
        }

        // Failed to retrieve data for the given ref
        LOG.warn( "Unable to retrieve data; ref = " + eventRef );
        return ResolvedEventData.NO_DATA;
    }

    /**
     * Retrieve the Calendar object by its reference string
     * @param ref the reference string (ID) of the Calendar being asked for
     * @param calServ the CalendarService object to use for retrieval
     * @return the Calendar object asked for, or null if it can't be retrieved
     */
    private static Calendar getCalendarByRef( String ref, CalendarService calServ )
    {
        Calendar cal = null;
        try
        {
            cal = calServ.getCalendar( ref );
        }
        catch( IdUnusedException ex )
        {
            LOG.warn( "Unable to retrieve Calendar by ref: " + ref, ex );
        }
        catch( PermissionException ex )
        {
            LOG.warn( "Permission exception trying to retrieve Calendar by ref: " + ref, ex );
        }

        return cal;
    }

    /**
     * Retrieve an Event object for the given Calendar by it's ID.
     * @param cal the Calendar object to retrieve the Event from
     * @param eventID the ID of the event being asked for
     * @return The CalendarEvent object being asked for, or null if it can't be retrieved
     */
    private static CalendarEvent getEventByID( Calendar cal, String eventID )
    {
        CalendarEvent event = null;
        try
        {
            event = cal.getEvent( eventID );
        }
        catch( IdUnusedException ex )
        {
            LOG.warn( "Unable to retrieve CalendarEvent by ID: " + eventID, ex );
        }
        catch( PermissionException ex )
        {
            LOG.warn( "Permission exception trying to retrieve CalendarEvent by ID: " + eventID, ex );
        }

        return event;
    }
}
