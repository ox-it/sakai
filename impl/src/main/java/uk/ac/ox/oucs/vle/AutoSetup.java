package uk.ac.ox.oucs.vle;

import java.util.Arrays;
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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class performs automatic setup of the SES tool.
 * The main purpose of this is to automatically run the importers on startup.
 * @author buckett
 *
 */
public class AutoSetup implements ApplicationContextAware {

	private static final Log log = LogFactory.getLog(AutoSetup.class);

	private SchedulerManager schedulerManager;
	public void setSchedulerManager(SchedulerManager schedulerManager) {
		this.schedulerManager = schedulerManager;
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private ApplicationContext applicationContext;
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}

	public void init() {
		if (serverConfigurationService.getBoolean("ses.autosetup", false)) {
			log.info ("SES AutoSetup running");
			Scheduler scheduler = schedulerManager.getScheduler();
			
			String[] startups = serverConfigurationService.getStrings("ses.startup");
			for (int i = 0; i < startups.length; i++) {
				String startup = startups[i];
				try {
					String jobBean = serverConfigurationService.getString("ses.import."+startup+".jobbean");
					SpringJobBeanWrapper job = (SpringJobBeanWrapper)applicationContext.getBean(jobBean);
					JobDataMap jobData = new JobDataMap();
					jobData.put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
					jobData.put(JobBeanWrapper.JOB_TYPE, job.getJobType());
					
					String param;
					param = serverConfigurationService.getString("ses.import."+startup+".uri");
					if (null != param) {
						jobData.put("xcri.oxcap.populator.uri", param);
					}
					
					param = serverConfigurationService.getString("ses.import."+startup+".user");
					if (null != param) {
						jobData.put("xcri.oxcap.populator.username", param);
					}
					
					param = serverConfigurationService.getString("ses.import."+startup+".password");
					if (null != param) {
						jobData.put("xcri.oxcap.populator.password", param);
					}
					
					param = serverConfigurationService.getString("ses.import."+startup+".name");
					if (null != param) {
						jobData.put("xcri.oxcap.populator.name", param);
					}
					
					JobDetail jobDetail = new JobDetail(job.getJobType(), null, job.getJobClass());
					jobDetail.setJobDataMap(jobData);
					scheduler.addJob(jobDetail, true);
					scheduler.triggerJobWithVolatileTrigger(job.getJobType(), null);
					log.info("Triggered job: "+ job.getJobType());
				} catch (SchedulerException se) {
					log.warn("Failed to run job: "+ startup, se);
				}
				
			}
		}
	}

	
}
