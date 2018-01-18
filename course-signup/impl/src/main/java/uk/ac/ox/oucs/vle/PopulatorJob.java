/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

/**
 * Bean that handles transactional stuff with regard to logging
 * @author buckett
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
	 * The populator.
	 */
	private Populator populator;
	public void setPopulator(Populator populator){
		this.populator = populator;
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
			log.error("Failed to write headers to logfile.", e);
		}

		try {
			populator.update(pContext);

		} catch (PopulatorException e) {
			log.error("Import Failed for: "+ pContext, e);
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
			proxy.prependLog(dWriter.getIdName(), dWriter.getDisplayName(), dOut.toByteArray());

			eWriter.footer();
			eWriter.flush();
			proxy.writeLog(eWriter.getIdName(), eWriter.getDisplayName(), eOut.toByteArray());

			iWriter.footer();
			iWriter.flush();
			proxy.writeLog(iWriter.getIdName(), iWriter.getDisplayName(), iOut.toByteArray());

		} catch (IOException e) {
			log.error("Failed to write logfile to resources [IOException].", e);

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
