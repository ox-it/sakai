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

	private Populator populator;
	
	public void setPopulator(Populator populator){
		this.populator = populator;
	}
	
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		
		JobDetail jobDetail = context.getJobDetail();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		PopulatorContext pContext = new PopulatorContext("xcri.oxcap.populator", jobDataMap);
		populator.update(pContext);
	}

}
