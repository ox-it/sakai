package org.sakaiproject.sitestats.api.event.detailed;

import java.io.Serializable;

/**
 * Represents public facing information about a reference.
 * Can take the form of simple textual information, or a link to, say, an entity
 *
 * @author bbailla2, bjones86
 */
public class ResolvedRef implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * enum indicating how a ResolvedRef is to be displayed
	 */
	public enum ResolutionType
	{
		TEXT, LINK
	}

	// how this ResolveRef is to be displayed
	private final ResolutionType type;

	// the key for the key-value pair
	private final String key;

	// text to display to user
	private final String displayValue;

	// if type=LINK, this will store the href
	private String url;

	private ResolvedRef(String key, String displayValue)
	{
		type = ResolutionType.TEXT;
		this.key = key;
		this.displayValue = displayValue;
	}

	private ResolvedRef(ResolutionType type, String key, String displayValue)
	{
		this.type = type;
		this.key = key;
		this.displayValue = displayValue;
	}

	/**
	 * @param key they key for this key-value pair
	 * @param displayValue the user facing text
	 * @return a ResolvedRef instance with type=TEXT
	 */
	public static ResolvedRef newText(String key, String displayValue)
	{
		return new ResolvedRef(key, displayValue);
	}

	/**
	 * @param key the key for this key-value pair
	 * @param displayValue the user facing text in the link
	 * @param url the href of the link
	 * @return a ResolvedRef instance with type=LINK
	 */
	public static ResolvedRef newLink(String key, String displayValue, String url)
	{
		ResolvedRef instance = new ResolvedRef(ResolutionType.LINK, key, displayValue);
		instance.url = url;
		return instance;
	}

	public ResolutionType getType()
	{
		return type;
	}

	public String getKey()
	{
		return key;
	}

	public String getDisplayValue()
	{
		return displayValue;
	}

	public String getUrl()
	{
		return url;
	}
}
