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

	/**
	 * 
	 */
	public SakaiProxy proxy;
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	private static final Log log = LogFactory.getLog(BasePopulatorWrapper.class);

	abstract void runPopulator(PopulatorContext context) throws IOException;
	
	/**
	 * 
	 */
	public void update(PopulatorContext context) {

		PopulatorLogWriter dWriter = null;
		PopulatorLogWriter eWriter = null;
		PopulatorLogWriter iWriter = null;

		try {
			ByteArrayOutputStream dOut = new ByteArrayOutputStream();
			ByteArrayOutputStream eOut = new ByteArrayOutputStream();
			ByteArrayOutputStream iOut = new ByteArrayOutputStream();

			dWriter = new XcriLogWriter(dOut, context.getName()+"ImportDeleted");
			context.setDeletedLogWriter(dWriter);

			eWriter = new XcriLogWriter(eOut, context.getName()+"ImportError");
			context.setErrorLogWriter(eWriter);

			iWriter = new XcriLogWriter(iOut, context.getName()+"ImportInfo");
			context.setInfoLogWriter(iWriter);

			runPopulator(context);

			dWriter.footer();
			dWriter.flush();
			proxy.writeLog(dWriter.getIdName(), dWriter.getDisplayName(), dOut.toByteArray());

			eWriter.footer();
			eWriter.flush();
			proxy.writeLog(eWriter.getIdName(), eWriter.getDisplayName(), eOut.toByteArray());

			iWriter.footer();
			iWriter.flush();
			proxy.writeLog(iWriter.getIdName(), iWriter.getDisplayName(), iOut.toByteArray());

		} catch (PopulatorException e) {
			try {
				eWriter.write("PopulatorException caught ["+e.getLocalizedMessage()+"]");
				eWriter.write(getStackTrace(e));
			} catch (IOException e1) {
				log.error("Failed to write content to logfile.", e);
			}

		} catch (IllegalStateException e) {
			log.error("IllegalStateException ["+context.getURI()+"]", e);

		} catch (IOException e) {
			log.error("IOException ["+context.getURI()+"]", e);

		} catch (VirusFoundException e) {
			log.error("VirusFoundException ["+context.getURI()+"]", e);

		} catch (OverQuotaException e) {
			log.error("OverQuotaException ["+context.getURI()+"]", e);

		} catch (ServerOverloadException e) {
			log.error("ServerOverloadException ["+context.getURI()+"]", e);

		} catch (PermissionException e) {
			log.error("PermissionException ["+context.getURI()+"]", e);

		} catch (TypeException e) {
			log.error("TypeException ["+context.getURI()+"]", e);

		} catch (InUseException e) {
			log.error("InUseException ["+context.getURI()+"]", e);

		} finally {
			if (null != dWriter) {
				try {
					dWriter.close();

				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}

			if (null != eWriter) {
				try {
					eWriter.close();

				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}

			if (null != iWriter) {
				try {
					iWriter.close();

				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}
		}

	}
	
	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	  }

}
