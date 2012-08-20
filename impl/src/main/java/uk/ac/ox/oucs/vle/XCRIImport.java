package uk.ac.ox.oucs.vle;

import java.io.InputStream;

import org.sakaiproject.component.api.ServerConfigurationService;

public class XCRIImport {
		
	/**
	 * 
	 */
	protected ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	protected String getXcriURL() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.url", 
					"http://daisy-feed.socsci.ox.ac.uk/XCRI_SES.php");
		}
		return "http://daisy-feed.socsci.ox.ac.uk/XCRI_SES.php";
	}
	
	protected String getXcriAuthUser() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.auth.user", "sesuser");
		}
		return "sesuser";
	}
	
	protected String getXcriAuthPassword() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.auth.password", "blu3D0lph1n");
		}
		return "blu3D0lph1n";
	}
	
}
