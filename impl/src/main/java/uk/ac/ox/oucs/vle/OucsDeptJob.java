package uk.ac.ox.oucs.vle;

import java.net.MalformedURLException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * Simple bean that allows the populator to be called from Quartz.
 * @author buckett
 *
 */
public class OucsDeptJob implements Job {

	private Module module;
	
	public void setModule(Module module){
		this.module = module;
	}
	
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		module.update();
	}
	
	/*
	public static void main(String [] args) {
		
		OucsDeptJob job = new OucsDeptJob();
		CourseDAO dao = new CourseDAOImpl();
		
		
		try {
			oucsDept.setCourseDao(dao);
			
			job.execute(null);
			
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

}
