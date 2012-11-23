package uk.ac.ox.oucs.vle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DaisyPopulatorWrapper implements PopulatorWrapper {
	
	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}

	/**
	 * 
	 */
	private Populator populator;
	public void setPopulator(Populator populator) {
		this.populator = populator;
	}
	
	private static final Log log = LogFactory.getLog(DaisyPopulatorWrapper.class);

	/**
	 * 
	 */
	public void update(PopulatorContext context) {
		
		try {
			dao.flagSelectedDaisyCourseGroups(context.getName());
			dao.flagSelectedDaisyCourseComponents(context.getName());
			
            populator.update(context);
            
            dao.deleteSelectedCourseGroups(context.getName());
            dao.deleteSelectedCourseComponents(context.getName());
			
        } catch (IllegalStateException e) {
        	log.warn("IllegalStateException ["+context.getURI()+"]", e);
        }
       
	}
	
}
