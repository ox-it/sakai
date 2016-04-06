package org.sakaiproject.util;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

public class LocaleLogger {

	private static Log log = LogFactory.getLog(LocaleLogger.class);
	
	public void init() {
		Locale jvmLocale = Locale.getDefault();
		Locale sakaiLocale = new ResourceLoader().getLocale();
		log.info("JVM Locale: "+ jvmLocale+ " Sakai Locale: "+ sakaiLocale);
	}
}
