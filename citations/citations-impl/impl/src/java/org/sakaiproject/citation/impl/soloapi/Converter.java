package org.sakaiproject.citation.impl.soloapi;

import org.sakaiproject.citation.api.Citation;

/**
 * A conversion between a ContextObject and a Citation.
 * @author nickwilson
 *
 */
public interface Converter {

	/**
	 * Retrieves the kind of media it is, e.g., book, journal, unknown
	 * @return A constant representing the media type
	 */
	public String getType();
	/**
	 * Converts the json in the Context into a citation
	 * @return A converted citation, or null if an error is thrown
	 */

	public Citation convert(ContextObject context);
}
