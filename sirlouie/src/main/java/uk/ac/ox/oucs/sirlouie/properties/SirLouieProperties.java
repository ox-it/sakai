package uk.ac.ox.oucs.sirlouie.properties;

import java.util.Properties;

public abstract class SirLouieProperties extends Properties {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SirLouieProperties() {
	}
	
	public String getWebResourseURL(String id) {
		String webResourceURL = getProperty("webresourceURL.aleph");
		id = id.replaceAll("oxfaleph", "BIB01");
		return webResourceURL.replaceAll("<<<OXFALEPHID>>>", id);
	}

}
