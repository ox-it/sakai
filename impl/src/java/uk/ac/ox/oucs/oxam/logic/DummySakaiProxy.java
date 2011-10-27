package uk.ac.ox.oucs.oxam.logic;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pom.logic.SakaiProxy;

public class DummySakaiProxy implements SakaiProxy {
	
	private static final Log LOG = LogFactory.getLog(DummySakaiProxy.class);

	public String getCurrentSiteId() {
		return "currentSiteId";
	}

	public String getCurrentUserId() {
		return "currentUserId";
	}

	public String getCurrentUserDisplayName() {
		return "Current User";
	}

	public boolean isSuperUser() {
		return false;
	}

	public void postEvent(String event, String reference, boolean modify) {
		LOG.info("Posted event: "+ event+ " reference: "+ reference+ " modify: "+ modify);
	}

	public String getSkinRepoProperty() {
		return "skin";
	}

	public String getToolSkinCSS(String skinRepo) {
		return skinRepo+"";
	}

	public boolean getConfigParam(String param, boolean dflt) {
		return false;
	}

	public String getConfigParam(String param, String dflt) {
		return "";
	}
	
	public void depositFile(String path, Callback<OutputStream> callback) {
		
	}

}
