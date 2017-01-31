package org.sakaiproject.component.app.scheduler.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

/**
 * Simple job to fire a site update on all sites of a particular type.
 */
public class EventTriggerJob implements Job {

    private final Log log = LogFactory.getLog(EventTriggerJob.class);

    private EventTrackingService eventTrackingService;
    private SiteService siteService;

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String siteType = context.getMergedJobDataMap().getString("type");
        if (siteType == null || siteType.isEmpty()) {
            log.warn("You need to set a site type");
            return;
        }
        String eventType = context.getMergedJobDataMap().getString("event");
        if (eventType == null || eventType.isEmpty()) {
            log.warn("You need to set a event type");
            return;
        }

        for (Site site: siteService.getSites(SiteService.SelectionType.ANY, siteType, null, null,SiteService.SortType.NONE, null)) {
            Event event = eventTrackingService.newEvent(eventType, site.getReference(), true);
            eventTrackingService.post(event);
        }
    }
}
