package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Arrays;
import java.util.List;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.sitestats.api.event.detailed.forums.ForumData;
import org.sakaiproject.sitestats.api.event.detailed.forums.MessageData;
import org.sakaiproject.sitestats.api.event.detailed.forums.TopicData;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 *
 * @author plukasew
 */
public class MsgForumsReferenceResolver
{
	// Event types handled by this resolver
	private static final String FORUMS_DELETE_MSG = "forums.delete";
	private static final String FORUMS_DELETE_FORUM = "forums.deleteforum";
	private static final String FORUMS_DELETE_TOPIC = "forums.deletetopic";
	private static final String FORUMS_GRADE = "forums.grade";
	private static final String FORUMS_NEW_MSG = "forums.new";
	private static final String FORUMS_NEW_FORUM = "forums.newforum";
	private static final String FORUMS_NEW_TOPIC = "forums.newtopic";
	private static final String FORUMS_READ = "forums.read";
	private static final String FORUMS_RESPONSE = "forums.response";
	private static final String FORUMS_REVISE_FORUM = "forums.reviseforum";
	private static final String FORUMS_REVISE_TOPIC = "forums.revisetopic";
	// Private Messages events will not be resolved due to privacy concerns
	/*private static final String MESSAGES_DELETE_MSG = "messages.delete";
	private static final String MESSAGES_FORWARD = "messages.forward";
	//private static final String MESSAGES_SOFT_DELETE_MSG = "messages.movedtodeletefolder"; <-- currently unrecognized by SiteStats
	private static final String MESSAGES_NEW_MSG = "messages.new";
	private static final String MESSAGES_NEW_FOLDER = "messages.newfolder";
	private static final String MESSAGES_READ = "messages.read";
	private static final String MESSAGES_REPLY = "messages.reply";
	private static final String MESSAGES_MARK_UNREAD = "messages.unread";*/

	public static final List<String> MSG_FORUMS_RESOLVABLE_EVENTS = Arrays.asList(FORUMS_DELETE_MSG,
			FORUMS_DELETE_FORUM, FORUMS_DELETE_TOPIC, FORUMS_GRADE, FORUMS_NEW_MSG, FORUMS_NEW_FORUM, FORUMS_NEW_TOPIC,
			FORUMS_READ, FORUMS_RESPONSE, FORUMS_REVISE_FORUM, FORUMS_REVISE_TOPIC/*, MESSAGES_DELETE_MSG, MESSAGES_FORWARD,
			MESSAGES_NEW_MSG, MESSAGES_NEW_FOLDER, MESSAGES_READ, MESSAGES_REPLY, MESSAGES_MARK_UNREAD*/);

	private static final List<String> RESOLVE_EVENT_TO_FORUM_POST = Arrays.asList(FORUMS_DELETE_MSG,
			FORUMS_NEW_MSG, FORUMS_READ, FORUMS_RESPONSE);
	private static final List<String> RESOLVE_EVENT_TO_TOPIC = Arrays.asList(FORUMS_DELETE_TOPIC,
			FORUMS_NEW_TOPIC, FORUMS_REVISE_TOPIC);
	private static final List<String> RESOLVE_EVENT_TO_FORUM = Arrays.asList(FORUMS_DELETE_FORUM,
			FORUMS_NEW_FORUM, FORUMS_REVISE_FORUM);
	/*private static final List<String> RESOLVE_EVENT_TO_MSG = Arrays.asList(MESSAGES_DELETE_MSG, MESSAGES_FORWARD,
			MESSAGES_NEW_MSG, MESSAGES_READ, MESSAGES_REPLY, MESSAGES_MARK_UNREAD);*/

	private static final String MSG_LEVEL = "Message";
	private static final String TOPIC_LEVEL = "Topic";
	private static final String FORUM_LEVEL = "Forum";

	public static ResolvedEventData resolveEventReference(final String eventType, final String ref,
			DiscussionForumManager dfMan, UIPermissionsManager uiPermMan, EntityBroker broker)
	{
		ParsedMsgForumsRef parsedRef = parse(ref);
		if (!parsedRef.isValid())
		{
			return ResolvedEventData.NO_DATA;
		}

		if ((RESOLVE_EVENT_TO_FORUM.contains(eventType) || FORUMS_GRADE.equals(eventType))
				&& FORUM_LEVEL.equals(parsedRef.getLevel()))
		{
			DiscussionForum forum = dfMan.getForumById(parsedRef.getItemId());
			return new ForumData(forum.getTitle());
		}
		else if ((RESOLVE_EVENT_TO_TOPIC.contains(eventType) || FORUMS_GRADE.equals(eventType))
				&& TOPIC_LEVEL.equals(parsedRef.getLevel()))
		{
			DiscussionTopicData topicData = new DiscussionTopicData(parsedRef.getItemId(), dfMan);
			TopicData td = buildTopicData(topicData, dfMan, broker, uiPermMan);
			if (td != null)
			{
				return td;
			}

			return ResolvedEventData.NO_DATA; // OWLTODO: return optional in java 8
		}
		else if ((RESOLVE_EVENT_TO_FORUM_POST.contains(eventType) || FORUMS_GRADE.equals(eventType))
				&& MSG_LEVEL.equals(parsedRef.getLevel()))
		{
			Message msg = dfMan.getMessageById(parsedRef.getItemId());
			if (msg == null)
			{
				return ResolvedEventData.NO_DATA;
			}

			// we need the topic (and later the thread or "conversation") in order to provide necessary context around this message,
			// and properly support the anonymous forums feature and check forums permissions
			Topic topic = msg.getTopic(); // lightweight topic, has only the id
			if (topic == null)
			{
				return ResolvedEventData.NO_DATA;
			}

			DiscussionTopicData topicData = new DiscussionTopicData(topic.getId(), dfMan);
			if (topicData.exists())
			{
				TopicData td = buildTopicData(topicData, dfMan, broker, uiPermMan);
				if (td == null)
				{
					return ResolvedEventData.NO_DATA;
				}

				if (td.isDeleted())
				{
					// we can't check permissions on the topic because the forum is required
					// so abort here and return topicdata only instead of messagedata and let the UI sort it out
					return td;
				}
				if (!td.isUserPermittedToRead())
				{
					// current user does not have permissions to read this topic (or the topic/forum is in draft, according to the implementation)
					// just return the topicdata instead of messagedata and let the UI sort it out
					return td;
				}

				// get the thread (conversation), if different from message
				MessageData thread = null;
				Long threadId = msg.getThreadId();
				if (threadId != null && !msg.getId().equals(threadId))
				{
					Message threadMsg = dfMan.getMessageById(threadId);
					if (threadMsg != null)
					{
						thread = new MessageData(td, null, threadMsg.getTitle(), threadMsg.getAuthor(), threadMsg.getCreated());
					}
				}

				return new MessageData(td, thread, msg.getTitle(), msg.getAuthor(), msg.getCreated());
			}
		}

		return ResolvedEventData.NO_DATA;
	}

	private static ParsedMsgForumsRef parse(String ref)
	{
			/* Format and examples:
			/<toolId>/site/<site_id>/<level>/<item_id>/<user_id>
			/forums/site/<siteID>/Message/<messageID>/<userID>
			/forums/site/4a4be716-9414-4423-a3eb-863e70952d6e/Message/3333128/01584302-9de1-4a0d-801b-c4da83192ba1
			*/
			String[] tokens = ref.split("/");
			if (tokens.length != 7)
			{
				return ParsedMsgForumsRef.INVALID;
			}

			try
			{
				long itemId = Long.valueOf(tokens[5]);
				String siteId = tokens[3];
				String toolId = tokens[1];
				String level = tokens[4];
				String userId = tokens[6];
				return new ParsedMsgForumsRef(itemId, siteId, toolId, level, userId);
			}
			catch (NumberFormatException nfe)
			{
				return ParsedMsgForumsRef.INVALID;
			}
	}

	private static TopicData buildTopicData(DiscussionTopicData topicData, DiscussionForumManager dfMan, EntityBroker broker, UIPermissionsManager uiPermMan)
	{
		if (!topicData.exists())
		{
			return null;  // OWLTODO: java 8 optional
		}

		DiscussionTopic fullTopic = topicData.getTopic();
		String topicTitle = fullTopic.getTitle();
		if (topicData.isDeleted())
		{
			String linkUrl = null;   // we have a deleted topic that cannot be linked to
			ForumData forum = null;  // and we cannot find the forum it belonged to
			return new TopicData(forum, topicTitle, linkUrl, topicData.isDeleted(), fullTopic.getPostAnonymous(), false); // pass permission = false since we can't check it
		}

		// we have an active open topic, try to get the discussion topic and forum and build an entity link
		DiscussionForum forum = dfMan.getForumById(fullTopic.getOpenForum().getId());
		if (forum != null)
		{
			String linkUrl = buildTopicUrl(fullTopic, broker);
			return new TopicData(new ForumData(forum.getTitle()), topicTitle, linkUrl, topicData.isDeleted(), fullTopic.getPostAnonymous(),
					uiPermMan.isRead(fullTopic, forum));  // uiPermMan.isRead() is slow but that's the best we have right now since the service permissions impl appears incomplete
		}

		return null; // OWLTODO: java 8 optional
	}

	private static String buildTopicUrl(Topic t, EntityBroker broker)
	{
		// At appears, at least in Sakai 10, that we can entity link only to forum topic through some hackery
		// we don't even really need entity broker for this, we can hardcode the format like ckeditor does,
		// but it is probably safer to go through the broker
		EntityData edata = broker.getEntity("/forum_topic/" + t.getId());
		return edata != null ? edata.getEntityURL() : "";
	}

	private static class ParsedMsgForumsRef
	{
		private final long itemId;
		private final String siteId;
		private final String toolId; // OWLTODO: enum (FORUMS | MESSAGES) ???
		private final String level;  // OWLTODO: enum (FORUM | TOPIC | MESSAGE) (hierarchy level) ???
		private final String userId;

		private static final ParsedMsgForumsRef INVALID = new ParsedMsgForumsRef(-1, "", "", "", "");

		public ParsedMsgForumsRef(long itemId, String siteId, String toolId, String level, String userId)
		{
			this.itemId = itemId;
			this.siteId = siteId;
			this.toolId = toolId;
			this.level = level;
			this.userId = userId;
		}

		public long getItemId()
		{
			return itemId;
		}

		public boolean isValid()
		{
			return itemId > 0;
		}

		public String getSiteId()
		{
			return siteId;
		}

		public String getToolId()
		{
			return toolId;
		}

		public String getLevel()
		{
			return level;
		}

		public String getUserId()
		{
			return userId;
		}
	}

	private static class DiscussionTopicData
	{
		private final DiscussionTopic topic;
		private final boolean exists;
		private final boolean isDeleted;

		public DiscussionTopicData(Long topicId, DiscussionForumManager dfMan)
		{
			// this call gets a fully-populated discussiontopic object
			// topic = dfMan.getTopicById(topicId); // does not retrieve deleted topics
			Topic t = dfMan.getTopicByIdWithAttachments(topicId); // retrieves deleted topics with a small performance cost (an extra join)
			topic = (t instanceof DiscussionTopic) ? (DiscussionTopic) t : null;

			if (topic == null)
			{
				exists = false;
				isDeleted = false;
			}
			else
			{
				exists = true;
				isDeleted = topic.getOpenForum() == null;
			}
		}

		public DiscussionTopic getTopic()
		{
			return topic;
		}

		public boolean exists()
		{
			return exists;
		}

		public boolean isDeleted()
		{
			return isDeleted;
		}
	}
}
