package uk.ac.ox.oucs.vle;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Simple bean that allows the populator to be called from Quartz.
 * @author buckett
 *
 */
public class ModuleJob implements Job {

	private Module module;
	
	public void setModule(Module module){
		this.module = module;
	}
	
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		module.update();

	}
	
	public static void main(String [] args) {
		
		ModuleJob job = new ModuleJob();
		
		try {
			job.setModule(new ModuleImpl());
			job.execute(null);
			
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
