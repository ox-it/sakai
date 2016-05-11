package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;

/**
 * Simple interface for a class that can supply an inputstream.
 * This is abstracted to an interface to keep sakai dependencies out of the main codebase.
 * @author buckett
 *
 */
public interface ValueSource {

	/**
	 * Get an input stream which data can be read from.
	 * @return The InputStream, or <code>null</code> if there was a problem.
	 */
	public InputStream getInputStream();
	
}
