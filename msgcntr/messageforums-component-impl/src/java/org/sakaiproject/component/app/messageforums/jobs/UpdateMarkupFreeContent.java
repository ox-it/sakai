package org.sakaiproject.component.app.messageforums.jobs;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageParsingService;

/**
 * This is a job to find all the existing messages in a forum and to filter them so
 * that they are plain text.
 * @author buckett
 *
 */
public class UpdateMarkupFreeContent implements Job {

	private static final Log LOG = LogFactory.getLog(UpdateMarkupFreeContent.class);
	
	// These are escaped tags which should have been stripped.
	// Use non-greedy matcher.
	private static final Pattern anchorTags = Pattern.compile("<a[^>]*?href=('|\")(.*?)\\1.*?>(.*?)</a>");
	
	private MessageForumsMessageManager messageForumsMessageManager;
	
	private MessageParsingService messageParsingService;

	public void setMessageForumsMessageManager(
			MessageForumsMessageManager messageForumMessageManager) {
		this.messageForumsMessageManager = messageForumMessageManager;
	}

	public void setMessageParsingService(MessageParsingService messageParsingService) {
		this.messageParsingService = messageParsingService;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		// You can't find all sites with a tool through the API  so you have to tell this job which site
		// to look in.
		String siteId = context.getMergedJobDataMap().getString("site.id");
		if (siteId == null) {
			LOG.warn("Job started without site.id property.");
			return;
		}

		int messages = 0;
		int filteredMessages = 0;

		List<Message> allMessagesInSite = messageForumsMessageManager.getAllMessagesInSite(siteId);
		for(Message message: allMessagesInSite) {
			messages++;
			BaseForum baseForum = message.getTopic().getBaseForum();
			if (baseForum instanceof DiscussionForum) {
				DiscussionForum discussionForum = (DiscussionForum)baseForum;
				if (discussionForum.getMarkupFree()) {
					// Filter the message.
					// Need to strip all existing HTML 
					String oldBody = message.getBody();
					String newBody = updateBody(oldBody);
					message.setBody(newBody);
					messageForumsMessageManager.saveMessage(message);
					filteredMessages++;
				}
			}
		}
		LOG.info("Updating complete for site: "+ siteId+ ", messages: "+ messages+ " filtered: "+ filteredMessages);
	}
	
	/**
	 * Update the body of a message removing most of the HTML and then adding it back in where we need to.
	 */
	public String updateBody(String body) {
		String cleanBody = body.replaceAll("&lt;", "<");
		cleanBody = cleanBody.replaceAll("&gt;", ">");
		
		
		Matcher anchorMatcher = anchorTags.matcher(cleanBody); //.replaceAll("$3 ($2)")
		StringBuffer anchorBuilder = new StringBuffer();
		while(anchorMatcher.find()) {
			String url = anchorMatcher.group(2);
			String name = anchorMatcher.group(3);
			// drop any mailto links and we'll pick them up again.
			if (url.startsWith("mailto:")) {
				anchorMatcher.appendReplacement(anchorBuilder, name);
			// use contains so if there are existing elements in the name we still match
			} else if (name.contains(url)) {
				anchorMatcher.appendReplacement(anchorBuilder, url);
			} else {
				anchorMatcher.appendReplacement(anchorBuilder, name+ " "+ "("+ url+ ")");
			}
		}
		anchorMatcher.appendTail(anchorBuilder);
		
		String newBody = anchorBuilder.toString().replaceAll("\n", "<br />");
		newBody = newBody.replaceAll("</div>", "<br /><br />");

		String clean = messageParsingService.parse(messageParsingService.format(newBody));
		// Trim multiple <br />
		clean = clean.replaceAll("<br />(:?\\s*<br />)+", "<br /><br />");
		return clean;
	}

}
