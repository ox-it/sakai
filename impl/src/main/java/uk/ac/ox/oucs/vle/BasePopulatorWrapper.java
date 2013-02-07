package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

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
