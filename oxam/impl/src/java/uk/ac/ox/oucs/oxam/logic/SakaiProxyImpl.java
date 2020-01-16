package uk.ac.ox.oucs.oxam.logic;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;


/**
 * Implementation of our SakaiProxy API
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Logger log = Logger.getLogger(SakaiProxyImpl.class);

	/**
 	* {@inheritDoc}
 	*/
	public String getSkinRepoProperty(){
		return serverConfigurationService.getString("skin.repo");
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getToolSkinCSS(String skinRepo){
		
		String skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();
		
		if(skin == null) {
			skin = serverConfigurationService.getString("skin.default");
		}
		
		return skinRepo + "/" + skin + "/tool.css";
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean getConfigParam(String param, boolean dflt) {
		return serverConfigurationService.getBoolean(param, dflt);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getConfigParam(String param, String dflt) {
		return serverConfigurationService.getString(param, dflt);
	}
	
	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}
	
	private SessionManager sessionManager;

	private ServerConfigurationService serverConfigurationService;
	
	private SiteService siteService;


	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
}
