package uk.ac.ox.oucs.sirlouie.properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.sakaiproject.component.cover.ServerConfigurationService;

public class SakaiProperties extends SirLouieProperties {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String CONFIG_LOCATION = "sirlouie.properties";
	public final static String WEBRESOURCE_URL = "sirlouie.webresourceURL";
	
	/**
	 * This class allows the location of the sirlouie filter properties to be loaded from a 
	 * file defined in the sakai.properties file.
	 * Set <code>sirlouie.properties</code> to the location of the of the config file for 
	 * SirLouie, if the file isn't absolute it will be Interpreted relative to the sakai home folder.
	 * @author buckett
	 *
	 */

	public SakaiProperties(ServletContext context) throws ServletException {
		
		super();
		setProperty(WEBRESOURCE_URL, ServerConfigurationService.getString(WEBRESOURCE_URL));
	}
	
	public String getWebResourseURL() {
		return getProperty(WEBRESOURCE_URL);
	}
}
