package org.sakaiproject.calendaring;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendaring.api.*;
import org.sakaiproject.calendaring.mocks.MockCalendarEventEdit;
import org.sakaiproject.calendaring.mocks.MockSakaiProxy;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the changes to implement REQUEST and CANCEL methods
 *
 * @author Ben Holmes
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-components.xml"})
public class CalendarWithMethodTest {

    private final String EVENT_NAME = "A new event";
    private final String LOCATION = "Building 1";
    private final String DESCRIPTION = "This is a sample event.";
    private final String CREATOR = "steve";
    private final long START_TIME = 1336136400; // 4/May/2012 13:00 GMT
    private final long END_TIME = 1336140000; // 4/May/2012 14:00 GMT
    
    private Set<User> users;
    //for the test classes we can still use annotation based injection
    @Resource(name = "org.sakaiproject.calendaring.api.ExternalCalendaringService")
    private ExternalCalendaringService service;
    @Autowired
    private ApplicationContext applicationContext;
    @Before
    public void setupData() {
        users = generateUsers();
    }

    @Test
    public void testContext() {
        Assert.assertNotNull(applicationContext.getBean("org.sakaiproject.calendaring.logic.SakaiProxy"));
        Assert.assertNotNull(applicationContext.getBean("org.sakaiproject.calendaring.api.ExternalCalendaringService"));
    }

    /**
     * A calendar with REQUEST method is not valid if it has no attendees
     */
    @Test
    public void testRequestCalendarWithoutAttendees() {
        VEvent vevent = generateVEvent();
        Calendar calendar = service.createCalendar(Collections.singletonList(vevent), "REQUEST", false);
        assertNull(calendar);
    }

    @Test
    public void testRequestCalendarWithAttendees() {
        VEvent vevent = generateVEvent();
        service.addChairAttendeesToEvent(vevent, users);
        Calendar calendar = service.createCalendar(Collections.singletonList(vevent), "REQUEST", false);
        assertNotNull(calendar);
    }

    @Test
    public void testRequestCalendarWithAttendeesWithoutEmails() {
        VEvent vevent = generateVEvent();
        User noEmailUser = mock(User.class);
        when(noEmailUser.getEmail()).thenReturn("");
        service.addChairAttendeesToEvent(vevent, new HashSet<>(Arrays.asList(noEmailUser)));
        Calendar calendar = service.createCalendar(Collections.singletonList(vevent), "REQUEST", false);
        assertNotNull(calendar);
    }

    @Test
    public void testCanDefineEventSequence() {
        VEvent vevent = generateExtEventWithSequence("101");
        assertEquals("101", vevent.getProperty(Property.SEQUENCE).getValue());
    }

    /**
     * For a cancel calendar to be valid all events must have sequence defined.
     * I expect the service to adjust for this in #cancelEvent().
     */
    @Test
         public void testCancelCalendarStillWorksWithoutSequence() {
        VEvent vevent = generateVEvent();
        service.cancelEvent(vevent);
        Calendar calendar = service.createCalendar(Collections.singletonList(vevent), "CANCEL", false);
        assertNotNull(calendar);
    }

    private VEvent generateVEvent() {
        CalendarEvent event = generateEvent(CREATOR);
        return service.createEvent(event);
    }

    private VEvent generateExtEventWithSequence(String sequenceNumber) {
        CalendarEventEdit event = generateEvent(CREATOR);
        event.setField("vevent_sequence", sequenceNumber);
        return service.createEvent(event);
    }

    @Test
    public void testEventWithOrganizer() {
        CalendarEvent event = generateEvent("fred");
        VEvent vEvent = getVEvent(event);
        String creatorEmail = new MockSakaiProxy().getUserEmail("fred");
        assertEquals(vEvent.getOrganizer().getValue(), "mailto:" + creatorEmail);
    }

    @Test
    public void testEventWithOrganizerWithoutEmailAddress() {
        CalendarEvent event = generateEvent(MockSakaiProxy.NO_EMAIL_ID);
        VEvent vEvent = getVEvent(event);
        assertNotNull(vEvent.getOrganizer());
    }

    private VEvent getVEvent(CalendarEvent event) {
        return service.createEvent(event);
    }
    private CalendarEventEdit generateEvent(String creatorId) {

        MockCalendarEventEdit edit = new MockCalendarEventEdit();

        edit.setDisplayName(EVENT_NAME);
        edit.setLocation(LOCATION);
        edit.setDescription(DESCRIPTION);
        edit.setId(UUID.randomUUID().toString());
        edit.setCreator(creatorId);

        Time start = Mockito.mock(Time.class);
        Mockito.when(start.getTime()).thenReturn(START_TIME);
        Time end = Mockito.mock(Time.class);
        Mockito.when(end.getTime()).thenReturn(END_TIME);
        TimeRange timeRange = Mockito.mock(TimeRange.class);
        when(timeRange.firstTime()).thenReturn(start);
        when(timeRange.lastTime()).thenReturn(end);

        edit.setRange(timeRange);

        return edit;

    }

    /**
     * Helper to generate a list of users. NOT A TEST METHOD
     * @return
     * @throws UserPermissionException
     * @throws UserAlreadyDefinedException
     * @throws UserIdInvalidException
     */
    private Set<User> generateUsers(){
        Set<User> users = new HashSet<User>();

        for(int i=0;i<5;i++) {
            User u = mock(User.class);
            when(u.getId()).thenReturn("user"+i);
            when(u.getEid()).thenReturn("user"+i);
            when(u.getDisplayId()).thenReturn("user"+i);
            when(u.getEmail()).thenReturn("user"+ i+ "@email.com");
            when(u.getFirstName()).thenReturn("User");
            when(u.getLastName()).thenReturn(String.valueOf(i));
            users.add(u);
        }
        return users;
    }
}
