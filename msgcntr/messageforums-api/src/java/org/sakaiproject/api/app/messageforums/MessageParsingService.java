package org.sakaiproject.api.app.messageforums;

/**
 * This service is used to convert from plain text to HTML and back again.
 * It's used for the plain text forums.
 *
 */
public interface MessageParsingService {
	
	/**
	 * Take a plain test string and convert it to HTML.
	 * @param message The plain text message.
	 * @return A HTML version of the message.
	 */
	public String parse(String message);
	
	/**
	 * Take a HTML message and convert it to plain text.
	 * @param message A HTML message.
	 * @return A plain text version of the message.
	 */
	public String format(String message);

}
