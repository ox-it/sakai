package org.sakaiproject.assignment.impl;

import org.sakaiproject.assignment.api.DownloadTrackingService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

/**
 * This just listens to events from the Sakai event tracking service and if they are ones
 * that we are interested in it parses them and asks the download tracking service to
 * persist them.
 */
public class DownloadEventListener implements Observer {

    private final Logger log = LoggerFactory.getLogger(DownloadEventListener.class);

    private EventTrackingService eventTrackingService;
    private DownloadTrackingService downloadTrackingService;
    private AttachmentParser parser;

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setDownloadTrackingService(DownloadTrackingService downloadTrackingService) {
        this.downloadTrackingService = downloadTrackingService;
    }

    public void init() {
        eventTrackingService.addLocalObserver(this);
        parser = new AttachmentParser();
    }

    public void destroy() {
        eventTrackingService.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        // arg is Event
        if (!(arg instanceof Event)){
            return;
        }
        Event event = (Event) arg;
        if (ContentHostingService.EVENT_RESOURCE_READ.equals(event.getEvent())) {
            String resource = event.getResource();
            AttachmentParser.Details detail = parser.parse(resource);
            if (detail != null) {
                String userId = event.getUserId();
                downloadTrackingService.saveDownload(userId, detail.getAttachment(), event.getEventTime());
                log.debug("Tracked event to download {} in Site {} by {}", detail.getFilename(), detail.getSiteId(), userId);
            }
        }
    }
}
