package org.sakaiproject.exporter.util;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ForumsExport {
	public final static String FORUM_TOPIC = "forum_topic";

	private Map<String, Resource> attachmentMap = new HashMap<>();
	private ExportUtil exportUtil;
	private MessageForumsForumManager forumManager;
	private MessageForumsMessageManager messageManager;

	public ForumsExport(ExportUtil exportUtil, MessageForumsForumManager messageForumsForumManager, MessageForumsMessageManager messageForumsMessageManager) {
		this.exportUtil = exportUtil;
		forumManager = messageForumsForumManager;
		messageManager = messageForumsMessageManager;
	}

	public Map<String, Resource> getForumsInSite(String siteId) {
		Map<String, Resource> forumsMap = new HashMap<>();
		Resource res;

		// Get all the forums for this site.
		for (DiscussionForum forum: forumManager.getForumsForMainPage()) {
			if (!forum.getDraft()) {
				// For each forum get the topics.
				for (DiscussionTopic topic: (Set<DiscussionTopic>)forum.getTopicsSet()) {
					if (topic.getDraft().equals(Boolean.FALSE)) {
						res = new Resource();
						res.resourceId = exportUtil.getResourceId();
						res.location = "cc-objects/" + res.resourceId + ".xml";
						res.sakaiId = topic.getId().toString();
						res.title = topic.getTitle();
						res.dependencies = new HashSet<>();
						res.use = null;
						res.islink = false;
						res.isbank = false;

						List<Attachment> attachments = topic.getAttachments();
						for (Attachment attachment: attachments) {
							String sakaiId = attachment.getAttachmentId();

							String lastAtom = sakaiId.substring(sakaiId.lastIndexOf("/") + 1);
							Resource attachmentResource = exportUtil.getResource(sakaiId, "attachments/" + FORUM_TOPIC + "/" + topic.getId() + "/" + lastAtom, null);
							res.dependencies.add(attachmentResource.resourceId);
							attachmentMap.put(attachmentResource.resourceId, attachmentResource);
						}
						forumsMap.put(res.sakaiId, res);
					}
				}
			}
		}
		return forumsMap;
	}

	public Map<String, Resource> getAttachmentsInSite() {
			return attachmentMap;
	}

}
