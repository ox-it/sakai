/*
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.ox.oucs.vle;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.sakaiproject.api.app.scheduler.JobDetailWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.api.app.scheduler.TriggerWrapper;
import org.sakaiproject.component.app.scheduler.JobDetailWrapperImpl;
import org.sakaiproject.component.app.scheduler.TriggerWrapperImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Tool;

public class SchedulerTool extends HttpServlet {

  private static final Log LOG = LogFactory.getLog(SchedulerTool.class);
  
  private Scheduler scheduler;

  public SchedulerTool()  {
  }

  /**
   * 	
   * @param scheduler
   */
  public List<JobDetailWrapper> processRefreshJobs() {
  		
	  List<JobDetailWrapper> jobDetailWrapperList = 
			  new ArrayList<JobDetailWrapper>();
  		
  		try	{
  			String[] jobNames = scheduler.getJobNames(Scheduler.DEFAULT_GROUP);
  		
  			for (int i = 0; i < jobNames.length; i++) {
  				JobDetailWrapper jobDetailWrapper = new JobDetailWrapperImpl();
  				jobDetailWrapper.setJobDetail(
  						scheduler.getJobDetail(jobNames[i],	Scheduler.DEFAULT_GROUP));
  				Trigger[] triggerArr = scheduler.getTriggersOfJob(jobNames[i],
  						Scheduler.DEFAULT_GROUP);
  				List<TriggerWrapper> triggerWrapperList = new ArrayList<TriggerWrapper>();
  				TriggerWrapper tw;
  				
  				for (int j = 0; j < triggerArr.length; j++) {
  					tw = new TriggerWrapperImpl();
  					tw.setTrigger(triggerArr[j]);
  					triggerWrapperList.add(tw);
  				}

  				jobDetailWrapper.setTriggerWrapperList(triggerWrapperList);
  				jobDetailWrapperList.add(jobDetailWrapper);
  			}
  			
  		} catch (SchedulerException e) {
  			LOG.error("scheduler error while getting job detail");
  		}
  		
  		return jobDetailWrapperList;
  	}
  
  	/**
  	 * This method runs the current job only once, right now
  	 * @return String
  	 */
  	public String processRunJobNow(String jobName) {
  		
  		try	{
  			List<JobDetailWrapper> jobDetailWrapperList =
  					processRefreshJobs();
  			
  			JobDetailWrapper selectedJobDetailWrapper = null;
  			
  			for (JobDetailWrapper jobDetailWrapper : jobDetailWrapperList) {
  	  			if (jobDetailWrapper.getJobDetail().getName().equals(jobName)) {
  	  				selectedJobDetailWrapper = jobDetailWrapper;
  	  			}
  	  		}
  			
  			scheduler.triggerJob(
  					selectedJobDetailWrapper.getJobDetail().getName(), 
  					selectedJobDetailWrapper.getJobDetail().getGroup());

  			return "success";
  			
  		} catch (Exception e) {
  			LOG.error("Failed to trigger job now");
  			return "error";
  		}
  	}
  	
  	private void setScheduler() {
  		SchedulerManager schedulerManager = 
  				(SchedulerManager) ComponentManager.getInstance().get(
  						org.sakaiproject.api.app.scheduler.SchedulerManager.class);
  		
  		scheduler = schedulerManager.getScheduler();
  		if (scheduler == null) {
  			LOG.error("Scheduler is down!");
  		}
  	}
  
  	public void doGet(HttpServletRequest request, HttpServletResponse response)
  			throws ServletException, IOException {
  		
  		ServletContext context = getServletContext();
  		setScheduler();
  		List<JobDetailWrapper> jobDetailWrapperList =
					processRefreshJobs();
  		
  		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
  		request.setAttribute("jobDetailList", jobDetailWrapperList);
  		
  		RequestDispatcher dispatcher = 
  				context.getRequestDispatcher("/static/quartz.jsp");
  		dispatcher.forward( request, response );
  	}
  	
  	public void doPost(HttpServletRequest request, HttpServletResponse response)
  			throws ServletException, IOException {
  		
  		Map<String, String[]> requestParams = request.getParameterMap();
  		String[] vals = (String[])requestParams.get("jobName");  
  		String jobName = vals[0];
  		setScheduler();
  		processRunJobNow(jobName);
  	}
}

