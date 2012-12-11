package uk.ac.ox.oucs.vle;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Simple bean that allows the populator to be called from Quartz.
 * @author buckett
 *
 */
public class PopulatorJob implements Job {

	private PopulatorWrapper populator;
	
	public void setPopulatorWrapper(PopulatorWrapper populatorWrapper){
		this.populator = populatorWrapper;
	}
	
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		PopulatorContext pContext = new PopulatorContext("xcri.oxcap.populator", jobDataMap);
		populator.update(pContext);
	}

}
