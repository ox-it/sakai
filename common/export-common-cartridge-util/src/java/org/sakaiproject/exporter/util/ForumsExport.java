package org.sakaiproject.exporter.util;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.util.FormattedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

public class ForumsExport {
	private static Logger log = LoggerFactory.getLogger(CCExport.class);
	public final static String FORUM_TOPIC = "forum_topic";

	private Map <String, Resource> forumsMap = new HashMap<>();
	private Map<String, Resource> attachmentMap = new HashMap<>();
	private ExportUtil exportUtil;
	private MessageForumsForumManager forumManager;
	private MessageForumsMessageManager messageManager;

	public ForumsExport(ExportUtil exportUtil, MessageForumsForumManager messageForumsForumManager, MessageForumsMessageManager messageForumsMessageManager) {
		this.exportUtil = exportUtil;
		forumManager = messageForumsForumManager;
		messageManager = messageForumsMessageManager;
	}

	public boolean getForumsInSite(String siteId) {
		Resource res;

		// Get all the forums for this site.
		for (DiscussionForum forum: forumManager.getForumsForMainPage()) {
			if (!forum.getDraft()) {
				// For each forum get the topics.
				for (DiscussionTopic topic: (Set<DiscussionTopic>)forum.getTopicsSet()) {
					if (topic.getDraft().equals(Boolean.FALSE)) {
						res = new Resource();
						res.setResourceId(ExportUtil.getResourceId());
						res.setLocation("cc-objects/" + res.getResourceId() + ".xml");
						res.setSakaiId(topic.getId().toString());
						res.setTitle(topic.getTitle());
						res.setMetaId(ExportUtil.getResourceId());
						res.setIslink(false);
						res.setIsbank(false);

						// Find any attachments on the topic and set as dependancies for the topic.
						List<Attachment> attachments = topic.getAttachments();
						Set<String> dependencies = new HashSet<>();
						for (Attachment attachment: attachments) {
							String sakaiId = attachment.getAttachmentId();

							String lastAtom = sakaiId.substring(sakaiId.lastIndexOf("/") + 1);
							Resource attachmentResource = exportUtil.getResource(sakaiId, "attachments/" + FORUM_TOPIC + "/" + topic.getId() + "/" + lastAtom, null);
							dependencies.add(attachmentResource.getResourceId());
							attachmentMap.put(attachmentResource.getResourceId(), attachmentResource);
						}
						res.setDependencies(dependencies);
						forumsMap.put(res.getSakaiId(), res);
					}
				}
			}
		}
		return true;
	}

	public boolean outputAllForums(ZipPrintStream out) throws IOException, TypeException, PermissionException, IdUnusedException, ServerOverloadException {
		// Output forum topics into the cc-objects directory.
		for (Map.Entry<String, Resource> entry: forumsMap.entrySet()) {
			Topic topic = forumManager.getTopicByIdWithAttachments(Long.parseLong(entry.getValue().getSakaiId()));
			String text = topic.getExtendedDescription();  // html
			if (text == null || text.trim().equals("")) {
				text = topic.getShortDescription();
				if (text != null) {
					text = FormattedText.convertPlaintextToFormattedText(text);
				}
			}

			// Create the xml file with the forum details.
			ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

			out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3\"");
			out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imsdt_v1p3.xsd\">");

			out.println("  <title>" + entry.getValue().getTitle() + "</title>");
			out.println("  <text texttype=\"text/html\"><div>" + text + "</div></text>");
			if (!entry.getValue().getDependencies().isEmpty()) {
				out.println("  <attachments>");
				for (String dependancy: entry.getValue().getDependencies()) {
					out.println("    <attachment href=\"" + attachmentMap.get(dependancy).getLocation() + "\"/>");
				}
				out.println("  </attachments>");
			}
			out.println("</topic>");
			out.closeEntry();

			// Create the xml metadata file.
			zipEntry = new ZipEntry("cc-objects/" + entry.getValue().getMetaId() + ".xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<topicMeta identifier=\"" + entry.getValue().getMetaId() + "\" xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("  <topic_id>" + entry.getValue().getResourceId() + "</topic_id>");
			out.println("  <title>" + entry.getValue().getTitle() + "</title>");
			out.println("  <position/>");
			out.println("  <type>topic</type>");
			out.println("  <discussion_type>threaded</discussion_type>");
			out.println("  <has_group_category>false</has_group_category>");
			out.println("  <workflow_state>unpublished</workflow_state>");
			out.println("  <module_locked>false</module_locked>");
			out.println("  <allow_rating>false</allow_rating>");
			out.println("  <only_graders_can_rate>false</only_graders_can_rate>");
			out.println("  <sort_by_rating>false</sort_by_rating>");
			out.println("  <todo_date/>");
			out.println("</topicMeta>");
			out.closeEntry();
		}

		// Output any forum attachments into the attachments directory.
		for (Map.Entry<String, Resource> entry : attachmentMap.entrySet()) {
			ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());

			String s = entry.getValue().getSakaiId();
			if (!exportUtil.isLink(s)) {
				// Attachment is a file.
				ContentResource contentResource = exportUtil.getContentResource(s);
				try (InputStream contentStream = contentResource.streamContent()) {
					out.putNextEntry(zipEntry);
					IOUtils.copy(contentStream, out);
					out.closeEntry();
				}
			}
		}
		return true;

	}

	public void ouputForumsToManifest(ZipPrintStream out) {
		// Output forum attachments and forum items (if they exist) to the manifest.
		for (Map.Entry<String, Resource> forumAttachment: attachmentMap.entrySet()) {
			out.println("    <resource href=\"" + forumAttachment.getValue().getLocation() + "\" identifier=\"" + forumAttachment.getValue().getResourceId() + "\" type=\"webcontent\">");
			out.println("      <file href=\"" + forumAttachment.getValue().getLocation() + "\"/>");
			out.println("    </resource>");
		}

		for (Map.Entry<String, Resource> forumTopic: forumsMap.entrySet()) {
			// Note from the IMSCC 1.3 spec the type should be imsdt_xmlv1p3 to reflect the CC version that is being used (1.3) but there seems
			// to be a bug in Canvas so this must be imsdt_xmlv1p1, otherwise one cannot import both discussions and get HTML pages into the Canvas Pages area.
			out.println("    <resource identifier=\"" + forumTopic.getValue().getResourceId() + "\" type=\"imsdt_xmlv1p1\">");
			out.println("      <file href=\"" + forumTopic.getValue().getLocation() + "\"/>");
			// Add meta data dependency.
			out.println("      <dependency identifierref=\"" + forumTopic.getValue().getMetaId() + "\"/>");
			// Add file attachment dependencies.
			for (String dependency: forumTopic.getValue().getDependencies()) {
				out.println("      <dependency identifierref=\"" + dependency + "\"/>");
			}
			out.println("    </resource>");

			out.println("    <resource identifier=\"" + forumTopic.getValue().getMetaId() + "\" type=\"associatedcontent/imscc_xmlv1p3/learning-application-resource\" href=\"cc-objects/" + forumTopic.getValue().getMetaId() + ".xml\">");
			out.println("      <file href=\"cc-objects/" + forumTopic.getValue().getMetaId() + ".xml\"/>");
			out.println("    </resource>");
		}
	}
}
