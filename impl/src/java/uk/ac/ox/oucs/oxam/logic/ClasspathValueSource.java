package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;

/**
 * A very simple value source that loads the files from the classpath.
 * @author buckett
 *
 */
public class ClasspathValueSource implements ValueSource {
	
	private String resource;
	

	public void setResource(String resource) {
		this.resource = resource;
	}

	public InputStream getInputStream() {
		return getClass().getResourceAsStream(resource);
	}

}
