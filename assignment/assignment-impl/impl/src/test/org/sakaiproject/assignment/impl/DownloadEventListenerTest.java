package org.sakaiproject.assignment.impl;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.assignment.api.DownloadTrackingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;

import java.util.Date;
import java.util.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DownloadEventListenerTest {

    private DownloadTrackingService downloadTrackingService;

    private DownloadEventListener listener;
    private Event event;
    private Observable observable;

    @Before
    public void setUp() {
        EventTrackingService eventTrackingService = mock(EventTrackingService.class);
        downloadTrackingService = mock(DownloadTrackingService.class);

        listener = new DownloadEventListener();
        listener.setEventTrackingService(eventTrackingService);
        listener.setDownloadTrackingService(downloadTrackingService);
        listener.init();

        event = mock(Event.class);
        observable = new Observable();
    }

    @Test
    public void testIgnored() {
        when(event.getEvent()).thenReturn("some.event");
        listener.update(observable, event);
        verify(downloadTrackingService, never()).saveDownload(any(), any(), any());
    }

    @Test
    public void testWrongPath() {
        when(event.getEvent()).thenReturn("content.read");
        when(event.getResource()). thenReturn("/content/public/file.txt");
        listener.update(observable, event);
        verify(downloadTrackingService, never()).saveDownload(any(), any(), any());
    }

    @Test
    public void testActuallySave() {
        when(event.getEvent()).thenReturn("content.read");
        when(event.getResource()).thenReturn("/content/attachment/1234/Assignments/5678/file.txt");
        when(event.getUserId()).thenReturn("userId");
        Date date = new Date();
        when(event.getEventTime()).thenReturn(date);
        listener.update(observable, event);
        verify(downloadTrackingService).saveDownload("userId", "/attachment/1234/Assignments/5678/file.txt", date);
    }

}