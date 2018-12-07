/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
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
 *
 * @author buckett
 */
public class AutoImport implements ApplicationContextAware {

	private static final Log log = LogFactory.getLog(AutoImport.class);

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
		if (serverConfigurationService.getBoolean("ses.autoimport", false)) {
			log.info ("SES AutoImport running");
			Scheduler scheduler = schedulerManager.getScheduler();
			
			String[] startups = serverConfigurationService.getStrings("ses.startup");
			for (String startup : startups) {
				try {
					String jobBean = serverConfigurationService.getString("ses.import."+startup+".jobbean");
					SpringJobBeanWrapper job = (SpringJobBeanWrapper)applicationContext.getBean(jobBean);
					JobDataMap jobData = new JobDataMap();
					jobData.put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
					jobData.put(JobBeanWrapper.JOB_TYPE, job.getJobType());
					
					JobDataMap triggerData = new JobDataMap();
					
					String param;
					param = serverConfigurationService.getString("ses.import."+startup+".uri");
					if (null != param) {
						triggerData.put("xcri.oxcap.populator.uri", param);
					}
					
					param = serverConfigurationService.getString("ses.import."+startup+".user");
					if (null != param) {
						triggerData.put("xcri.oxcap.populator.username", param);
					}
					
					param = serverConfigurationService.getString("ses.import."+startup+".password");
					if (null != param) {
						triggerData.put("xcri.oxcap.populator.password", param);
					}
					
					param = serverConfigurationService.getString("ses.import."+startup+".name");
					if (null != param) {
						triggerData.put("xcri.oxcap.populator.name", param);
					}

					JobBuilder builder = JobBuilder.newJob(job.getJobClass())
							.withIdentity(job.getJobType(), null)
                            .setJobData(jobData);
					JobDetail jobDetail = builder.build();
					scheduler.addJob(jobDetail, true, true);
					scheduler.triggerJob(jobDetail.getKey(), triggerData);
					log.info("Triggered job: "+ job.getJobType());
				} catch (SchedulerException se) {
					log.warn("Failed to run job: "+ startup, se);
				}
				
			}
		}
	}

}
