package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

public class SakaiValueSource implements ValueSource, Observer {

	private static final String OXAM_CONTENT_SITE_ID = "oxam.content.site.id";

	private final static Log LOG = LogFactory.getLog(SakaiValueSource.class);
	
	private ContentHostingService chs;
	private ServerConfigurationService scs;
	private SecurityService ss;
	private EventTrackingService ets;
	private Reloadable consumer;
	private String file;
	
	// Security advisor so we can read the files out of content hosting without worry about permissions.
	private SecurityAdvisor securityAdvisor =  new SecurityAdvisor() {

		public SecurityAdvice isAllowed(String userId, String function,
				String reference) {
			if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)) {
				return SecurityAdvice.ALLOWED;
			}
			return SecurityAdvice.PASS;
		}
	};
	
	public void init() {
		// Watch for all events, this shouldn't be a local observer as we want events from 
		// all nodes.
		ets.addObserver(this);
	}
	
	public void setContentHostingService(ContentHostingService chs) {
		this.chs = chs;
	}
	
	public void setServerConfigurationService(ServerConfigurationService scs) {
		this.scs = scs;
	}
	
	public void setSecurityService(SecurityService ss) {
		this.ss = ss;
	}
	
	public void setEventTrackingService(EventTrackingService ets) {
		this.ets = ets;
	}
	
	public void setReloadable(Reloadable consumer) {
		this.consumer = consumer;
	}

	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Here we want to watch to see if the file we loaded is changed and if so 
	 * call the consumer to reload their values.
	 */
	public void update(Observable o, Object arg) {
		if (arg instanceof Event) {
			Event event = (Event) arg;
			// The first check is fast and should filter out most stuff.
			// As Sakai generates a reasonably number of events.
			if(event.getModify() && event.getEvent().startsWith("content.")) {
				String reference = chs.getReference(getFilePath());
				if (event.getResource().equals(reference)) {
					if (consumer != null) {
						consumer.reload();
					}
				}
			}
		}
		
	}

	
	public InputStream getInputStream() {
		String filePath = getFilePath();
		try {
			// Don't worry about permissions.
			ss.pushAdvisor(securityAdvisor);
			ContentResource resource = chs.getResource(filePath);
			return resource.streamContent();
		} catch (PermissionException pe) {
			LOG.error("This shouldn't happen as we have ability to ready any file.");
		} catch (ServerOverloadException e) {
			LOG.error("Server is dying, we need help.");
		} catch (IdUnusedException e) {
			LOG.error("Couldn't find file: "+ filePath);
		} catch (TypeException e) {
			LOG.error("Resource isn't a file: "+ filePath);
		} finally {
			// Allow it to work in Sakai 2.6, this should change 
			// when we have moved toe 2.8
			ss.popAdvisor();
		}
		return null;
	}

	private String getFilePath() {
		String siteId = scs.getString(OXAM_CONTENT_SITE_ID);
		if (siteId == null) {
			LOG.error("Oxam site hasn't been set to load content from, please set to property "+ OXAM_CONTENT_SITE_ID);
			return null;
		}
		String siteCollection = chs.getSiteCollection(siteId);
		String filePath = siteCollection+ "/"+ file;
		return filePath;
	}

}
