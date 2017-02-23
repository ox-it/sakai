package org.sakaiproject.sitestats.tool.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.StatsDates;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.api.event.detailed.forums.ForumData;
import org.sakaiproject.sitestats.api.event.detailed.forums.MessageData;
import org.sakaiproject.sitestats.api.event.detailed.forums.TopicData;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Class to temporarily convert a ResolvedEventData object into a List<ResolvedRef> so that we
 * don't need to change the presentation code or any of the other ref resolvers at this time.
 *
 * View-layer logic for presenting the data contained in the ResolvedEventData object lives here and will probably remain,
 * even if we change the mechanism of presentation from a simple K/V list
 *
 * @author plukasew
 */
public class ForumsResolvedRefTransformer
{
	public static List<ResolvedRef> transform(ResolvedEventData resolved, String eventType)
	{
		// OWLTODO: localization should be done in this part of the process

		// switch on the instance and cast to what we need
		// this requires knowledge here in the view layer about the meaning of the data the service layer provides
		if (resolved instanceof ForumData)
		{
			// even with a deleted forum, this is always just the forum title and can be presented as-is
			ForumData forum = (ForumData) resolved;
			return Collections.singletonList(ResolvedRef.newText("Title", forum.getTitle()));
		}
		else if (resolved instanceof TopicData)
		{
			TopicData topic = (TopicData) resolved;

			// this one is tricky, could be for a topic event, or could be for a message event that could not be fully resolved
			// depending on the event we are dealing with, we need to handle this differently
			// OWLTODO: don't hardcode these events (will mean we may have to expose the MsgForumsReferenceResolver to the API? Or at least move the event defs into the API?)
			final List<String> RESOLVE_EVENT_TO_FORUM_POST = Arrays.asList("forums.delete",
			"forums.new", "forums.read", "forums.response");
			if (RESOLVE_EVENT_TO_FORUM_POST.contains(eventType))  // we expected a MessageData but we got a TopicData instead, handle these edge cases
			{
				List<ResolvedRef> list = new ArrayList<>(2);
				list.add(topicToRef(topic));

				if (topic.isDeleted())  // message info cannot be retrieved for deleted topics (not permitted will always be true for deleted topics, so the order of these checks is important)
				{
					list.add(ResolvedRef.newText("Error", "Topic has been deleted, message details are unavailable."));
				}
				else if (!topic.isUserPermittedToRead())
				{
					list.add(ResolvedRef.newText("Error", "You don't have permission to read messages in this topic, or the topic/forum has been saved in draft mode."));
				}

				return list;
			}

			return Collections.singletonList(topicToRef(topic));
		}
		else if (resolved instanceof MessageData)
		{
			MessageData msg = (MessageData) resolved;
			List<ResolvedRef> list = new ArrayList<>(3); // should have at most 3 entries

			// first get the topic and check some details
			TopicData topic = msg.getTopic();
			if (topic.getEntityUrl().isEmpty()) // OWLTODO: is this possible when we have MessageData? should it be?
			{
				list.add(ResolvedRef.newText("Topic", topic.getForum().getTitle() + " > " + topic.getTitle()));
			}
			else
			{
				list.add(ResolvedRef.newLink("Topic", topic.getForum().getTitle() + " > " + topic.getTitle(), topic.getEntityUrl()));
			}

			// Note: deleted topics and not permitted topics result in TopicData being returned, not MessageData,
			// so we don't need to deal with this possibilities here
			if (topic.isAnon())
			{
				// OWLTODO: possibly replace this with code that returns TopicData instead of MessageData for this scenario,
				//since we can't present the message
				list.add(ResolvedRef.newText("Conversation", "Anonymous conversation"));
				list.add(ResolvedRef.newText("Message", "Anonymous message"));
				return list;
			}

			// now on to the actual message details
			final String MSG_TEMPLATE = "\"%s\" posted by %s on %s";
			TimeService timeServ = Locator.getFacade().getTimeService();

			// first, see if we have a conversation
			MessageData convo = msg.getConversation();
			if (convo != null)
			{
				String date = StatsDates.formatForDisplay(convo.getCreationDate(), timeServ);
				list.add(ResolvedRef.newText("Conversation", String.format(MSG_TEMPLATE, convo.getTitle(), convo.getAuthor(), date)));
			}

			String date = StatsDates.formatForDisplay(msg.getCreationDate(), timeServ);
			// for forums.response, make it clear by the key that this is the message responsed to, not the response
			String key = "forums.response".equals(eventType) ? "Responded to" : "Message";
			list.add(ResolvedRef.newText(key, String.format(MSG_TEMPLATE, msg.getTitle(), msg.getAuthor(), date)));

			return list;
		}

		return Collections.emptyList();
	}

	private static ResolvedRef topicToRef(TopicData topic)
	{
		if (topic.isDeleted())
		{
			return ResolvedRef.newText("Topic", topic.getTitle() + " [deleted]");
		}
		else if (topic.getEntityUrl().isEmpty())
		{
			return ResolvedRef.newText("Topic", topic.getForum().getTitle() + " > " + topic.getTitle());
		}

		return ResolvedRef.newLink("Topic", topic.getForum().getTitle() + " > " + topic.getTitle(), topic.getEntityUrl());
	}
}
