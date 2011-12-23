package uk.ac.ox.oucs.oxam.utils;

import javax.activation.MimetypesFileTypeMap;

/**
 * This is just a filetype map that is easier to setup with Spring.
 * It allows extra mappings to be supplied at creation time.
 * @author buckett
 *
 */
public class CustomFileTypeMap extends MimetypesFileTypeMap {

	public CustomFileTypeMap(String[] extras) {
		super();
		for(String extra: extras) {
			addMimeTypes(extra);
		}
	}
}
