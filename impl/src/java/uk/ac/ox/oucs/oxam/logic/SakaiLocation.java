package uk.ac.ox.oucs.oxam.logic;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;

/**
 * This is a Sakai implementation for locating uploaded paper files. 
 * @author buckett
 *
 */
public class SakaiLocation implements Location {
	
	private ServerConfigurationService serverConfigurationService;
	private ContentHostingService contentHostingService;
	private String sitePath;
	
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}


	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}


	public void init() {
		String siteId = serverConfigurationService.getString(SakaiValueSource.OXAM_CONTENT_SITE_ID);
		if (siteId == null) {
			// Oh poo.
		}
		sitePath = contentHostingService.getSiteCollection(siteId);
		
		
	}

	public String getPrefix() {
		return contentHostingService.getUrl(sitePath);
	}

	public String getPath(String path) {
		// TODO do we need to worry about pathSeperator?
		String fullPath = sitePath + path;
		return fullPath;
	}

}
