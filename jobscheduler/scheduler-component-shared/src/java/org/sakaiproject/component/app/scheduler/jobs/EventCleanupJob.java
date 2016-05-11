package org.sakaiproject.component.app.scheduler.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.event.api.EventTrackingService;

/**
 * Job to cleanup old events from the database.
 * Outside of the event service as when merging into K1 jobscheduler won't be available.
 * @author buckett
 *
 */
@DisallowConcurrentExecution
public class EventCleanupJob implements Job{

	private EventTrackingService eventTrackingService;
	
	public void setEventTrackingService(EventTrackingService eventService) {
		this.eventTrackingService = eventService;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		eventTrackingService.cleanupEvents();
	}

}
