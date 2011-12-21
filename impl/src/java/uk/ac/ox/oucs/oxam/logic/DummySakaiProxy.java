package uk.ac.ox.oucs.oxam.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DummySakaiProxy implements SakaiProxy {
	
	private static final Log LOG = LogFactory.getLog(DummySakaiProxy.class);
	
	private Properties props;
	
	public DummySakaiProxy() throws IOException {
		String name = getClass().getSimpleName()+".properties";
		InputStream stream = getClass().getResourceAsStream(name);
		props = new Properties();
		props.load(stream);
	}

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
		return Boolean.parseBoolean(props.getProperty(param, Boolean.toString(dflt)));
	}

	public String getConfigParam(String param, String dflt) {
		return props.getProperty(param, dflt);
	}
	
	public void depositFile(String path, Callback<OutputStream> callback) {
		
	}

}
