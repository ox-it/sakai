package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

/**
 * Simple bean that allows the populator to be called from Quartz.
 * @author buckett
 *
 */
public class PopulatorJob implements Job {

	private static final Log log = LogFactory.getLog(PopulatorJob.class);

	/**
	 * The proxy for adding logfile to resources
	 */
	protected SakaiProxy proxy;
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * The populator jov wrapper
	 */
	private PopulatorWrapper populator;
	public void setPopulatorWrapper(PopulatorWrapper populatorWrapper){
		this.populator = populatorWrapper;
	}

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		JobDataMap jobDataMap = context.getMergedJobDataMap();
		PopulatorContext pContext = new PopulatorContext("xcri.oxcap.populator", jobDataMap);

		PopulatorLogWriter dWriter = null;
		PopulatorLogWriter eWriter = null;
		PopulatorLogWriter iWriter = null;

		ByteArrayOutputStream dOut = new ByteArrayOutputStream();
		ByteArrayOutputStream eOut = new ByteArrayOutputStream();
		ByteArrayOutputStream iOut = new ByteArrayOutputStream();

		try {
			dWriter = new XcriLogWriter(dOut, pContext.getName()+"ImportDeleted");
			dWriter.header("Deleted Groups and Components from SES Import");
			pContext.setDeletedLogWriter(dWriter);

			eWriter = new XcriLogWriter(eOut, pContext.getName()+"ImportError");
			eWriter.header("Errors and Warnings from SES Import");
			pContext.setErrorLogWriter(eWriter);

			iWriter = new XcriLogWriter(iOut, pContext.getName()+"ImportInfo");
			iWriter.header("Info and Warnings from SES Import");
			pContext.setInfoLogWriter(iWriter);

		} catch (IOException e) {
			log.error("Failed to write content to logfile.", e);
		}

		try {
			populator.update(pContext);

		} catch (PopulatorException e) {

			try {
				eWriter.write(getStackTrace(e));
				eWriter.flush();
			} catch (IOException ex) {
				log.error("Failed to write content to logfile.", ex);
			}

		}

		try {
			dWriter.footer();
			dWriter.flush();
			proxy.writeLog(dWriter.getIdName(), dWriter.getDisplayName(), dOut.toByteArray());

			eWriter.footer();
			eWriter.flush();
			proxy.writeLog(eWriter.getIdName(), eWriter.getDisplayName(), eOut.toByteArray());

			iWriter.footer();
			iWriter.flush();
			proxy.writeLog(iWriter.getIdName(), iWriter.getDisplayName(), iOut.toByteArray());

		} catch (IOException e) {
			log.error("Failed to write logfile to resources [IOException].", e);

		} catch (InUseException e) {
			log.error("Failed to write logfile to resources [InUseException].", e);

		} catch (TypeException e) {
			log.error("Failed to write logfile to resources [TypeException].", e);

		} catch (PermissionException e) {
			log.error("Failed to write logfile to resources [PermissionException].", e);

		} catch (ServerOverloadException e) {
			log.error("Failed to write logfile to resources [ServerOverloadException].", e);

		} catch (OverQuotaException e) {
			log.error("Failed to write logfile to resources [OverQuotaException].", e);

		} finally {

			if (null != dWriter) {
				try {
					dWriter.close();

				} catch (IOException e) {
					log.error("IOException ["+pContext.getURI()+"]", e);
				}
			}

			if (null != eWriter) {
				try {
					eWriter.close();

				} catch (IOException e) {
					log.error("IOException ["+pContext.getURI()+"]", e);
				}
			}

			if (null != iWriter) {
				try {
					iWriter.close();

				} catch (IOException e) {
					log.error("IOException ["+pContext.getURI()+"]", e);
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
