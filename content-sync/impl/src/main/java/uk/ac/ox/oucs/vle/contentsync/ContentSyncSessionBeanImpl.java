package uk.ac.ox.oucs.vle.contentsync;

import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;

/**
 * This bean is wrapped by a Spring Hibernate transaction wrapper so we can 
 * perform further operations on returned objects.
 */
public class ContentSyncSessionBeanImpl implements ContentSyncSessionBean {
	
	private DiscussionForumManager forumManager;
	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}
	
	public long getTopicId(long messageId) {

		Message message = forumManager.getMessageById(messageId);
		if (null != message) {
			Topic topic = message.getTopic();
			return topic.getId();
		}
		return 0;
	}
}
