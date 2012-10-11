package uk.ac.ox.oucs.vle;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.SpringJobBeanWrapper;

/**
 * This class performs automatic setup of the SES tool.
 * The main purpose of this is to automatically run the importers on startup.
 * @author buckett
 *
 */
public class AutoSetup {

	private static final Log log = LogFactory.getLog(AutoSetup.class);

	private SchedulerManager schedulerManager;
	private ServerConfigurationService serverConfigurationService;
	private List<SpringJobBeanWrapper> jobs;

	public void setSchedulerManager(SchedulerManager schedulerManager) {
		this.schedulerManager = schedulerManager;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setJobs(List<SpringJobBeanWrapper> jobs) {
		this.jobs = jobs;
	}

	public void init() {
		if (serverConfigurationService.getBoolean("ses.autosetup", false)) {
			log.info ("SES AutoSetup running");
			Scheduler scheduler = schedulerManager.getScheduler();
			for (SpringJobBeanWrapper job: jobs) {
				try {
					JobDataMap jobData = new JobDataMap();
					jobData.put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
					jobData.put(JobBeanWrapper.JOB_TYPE, job.getJobType());
					JobDetail jobDetail = new JobDetail(job.getJobType(), null, job.getJobClass());
					jobDetail.setJobDataMap(jobData);
					scheduler.addJob(jobDetail, true);
					scheduler.triggerJobWithVolatileTrigger(job.getJobType(), null);
					log.info("Triggered job: "+ job.getJobType());
				} catch (SchedulerException se) {
					log.warn("Failed to run job: "+ job, se);
				}
			}
		}
	}
}
