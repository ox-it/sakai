package org.sakaiproject.component.app.scheduler.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.event.api.UsageSessionService;

/**
 * Job to cleanup old sessions from the database.
 * Outside of the session service as when merging into K1 jobscheduler won't be available.
 * @author buckett
 *
 */
@DisallowConcurrentExecution
public class SessionCleanupJob implements Job {

	private UsageSessionService usageSessionService;

	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		usageSessionService.cleanupSessions();
	}

}
