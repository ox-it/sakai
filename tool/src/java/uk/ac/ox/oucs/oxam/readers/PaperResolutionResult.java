package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;

/**
 * This class exists so that the importer can lookup a paper without having to 
 * open streams, but also allows the lookup to be done once and a good error
 * message to be passed back.
 * 
 * @author buckett
 *
 */
public interface PaperResolutionResult {

	public boolean isFound();
	public InputStream getStream();
	public String[] getPaths();
}
