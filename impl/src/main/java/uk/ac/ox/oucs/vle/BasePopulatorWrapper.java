package uk.ac.ox.oucs.vle;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class BasePopulatorWrapper implements PopulatorWrapper {

	private static final Log log = LogFactory.getLog(BasePopulatorWrapper.class);

	abstract void runPopulator(PopulatorContext context) throws IOException;
	
	/**
	 * 
	 */
	public void update(PopulatorContext context) 
	throws PopulatorException{

		try {
			runPopulator(context);

		} catch (IllegalStateException e) {
			log.error("IllegalStateException ["+context.getURI()+"]", e);

		} catch (IOException e) {
			log.error("IOException ["+context.getURI()+"]", e);

		}

	}

}
