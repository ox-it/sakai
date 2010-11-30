package uk.ac.ox.oucs.sirlouie.properties;

import java.util.Properties;

public abstract class SirLouieProperties extends Properties {
	
	public SirLouieProperties() {
	}
	
	public String getWebResourseURL() {
		return getProperty("webresourceURL");
	}

}
