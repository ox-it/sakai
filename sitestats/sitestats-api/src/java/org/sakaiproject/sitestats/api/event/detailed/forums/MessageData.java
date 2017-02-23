package org.sakaiproject.sitestats.api.event.detailed.forums;

import java.util.Date;
import java.util.Objects;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 *
 * @author plukasew
 */
public final class MessageData implements ResolvedEventData
{
	private final TopicData topic;
	private final MessageData conversation;
	private final String title;
	private final String author;
	private final Date creationDate;

	public MessageData(TopicData topic, MessageData conversation, String title, String author, Date creationDate)
	{
		this.topic = topic;
		this.conversation = conversation;  // OWLTODO: Optional<MessageData>?
		this.title = title;
		this.author = author;  // this should be uuid, but forums Message objects only store the display id string
		this.creationDate = new Date(Objects.requireNonNull(creationDate).getTime());
	}

	public TopicData getTopic()
	{
		return topic;
	}

	public MessageData getConversation()
	{
		return conversation;
	}

	public String getTitle()
	{
		return title;
	}

	public String getAuthor()
	{
		return author;
	}

	public Date getCreationDate()
	{
		return creationDate;
	}
}
