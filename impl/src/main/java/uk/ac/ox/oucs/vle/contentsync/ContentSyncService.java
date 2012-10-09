package uk.ac.ox.oucs.vle.contentsync;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public class ContentSyncService {
	
	public static final String CONTENT_SYNC_ENABLED = "content.sync.enabled";

	private static final String TRACK_CONTENT = "trackContent";
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public boolean isContentSyncEnabled() {
		return serverConfigurationService.getBoolean(CONTENT_SYNC_ENABLED, true);
	}
	
	public boolean isSiteTracked(String siteId) {
		if (siteId != null) {
			try {
				// The site should be cached so the performance should be pretty good.
				Site site = siteService.getSite(siteId);
				return site.getProperties().getProperty(TRACK_CONTENT) != null;
			} catch (IdUnusedException e) {
				// Just ignore and fall through.
			}
		}
		return false;
	}
	
}
