package org.sakaiproject.sitestats.api.event.detailed.forums;

import java.util.Objects;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 *
 * @author plukasew
 */
public final class TopicData implements ResolvedEventData
{
	private final ForumData forum;  // OWLTODO: this can be null (deleted topics have no forum), wrap in Java 8 optional
	private final String title;
	private final String entityUrl;  // OWLTODO: this can be null (deleted topics have no url), wrap in Java 8 optional
	private final boolean deleted;
	private final boolean anon;
	private final boolean userPermittedToRead;

	public TopicData(ForumData forum, String title, String entityUrl, boolean deleted, boolean anon, boolean userPermittedToRead)
	{
		this.forum = forum;
		this.title = Objects.requireNonNull(title);
		this.entityUrl = entityUrl;
		this.deleted = deleted;
		this.anon = anon;
		this.userPermittedToRead = userPermittedToRead;
	}

	/**
	 * This could be null if the topic has been deleted. Best to call isDeleted on the topic first before calling this
	 * method. Will be replaced with Java 8 Optional in the future.
	 * @return the forum data, or null
	 */
	public ForumData getForum()
	{
		return forum;
	}

	public String getTitle()
	{
		return title;
	}

	/**
	 * This could be null if the topic has been deleted. Best to call isDeleted on the topic first before calling this
	 * method. Will be replaced with Java 8 Optional in the future.
	 * @return the forum data, or null
	 */
	public String getEntityUrl()
	{
		return entityUrl;
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public boolean isAnon()
	{
		return anon;
	}

	public boolean isUserPermittedToRead()
	{
		return userPermittedToRead;
	}
}
