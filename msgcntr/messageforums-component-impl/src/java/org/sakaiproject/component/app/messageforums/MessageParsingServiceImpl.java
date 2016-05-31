package org.sakaiproject.component.app.messageforums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.api.app.messageforums.MessageParsingService;
import org.sakaiproject.util.api.FormattedText;

public class MessageParsingServiceImpl implements MessageParsingService {

	private Pattern brCleanup = Pattern.compile("<\\s*br\\s*[^<>]?>", Pattern.CASE_INSENSITIVE);
	// Key is to look for a domain name which ends in 2 or 4 characters.
	private Pattern findURLs = Pattern.compile("(?<=\\s|>|;|\\(|^)((?:http|ftp)s?://)?+(?:[\\w-]+\\.)+([a-z]{2,4})(:\\d+)?(/[-#;&=?+%/\\.\\w\\(\\)]*)?(?=\\s|\\.|\\)|<|,|;|$)");
	// Looks for emails.
	// This is a simple scan for emails as it's bettern to ignore valid emails than to markup stuff that isn't.
	private Pattern findEmails = Pattern.compile("[\\w-\\+.=]+@[\\w-\\+]+(?:\\.[\\w-\\+]+)+");

	private FormattedText formattedText;

	public void setFormattedText(FormattedText formattedText) {
		this.formattedText = formattedText;
	}

	
	public String parse(String message) {
		// Get rid of form feeds.
		String withMarkup = message.replaceAll("\\r", "");
		
		// We don't replace &quot; as it doesn't get decoded.
		withMarkup = withMarkup.replaceAll("&", "&amp;");
		withMarkup = withMarkup.replaceAll("<", "&lt;");
		withMarkup = withMarkup.replaceAll(">", "&gt;");
		
		
		withMarkup = withMarkup.replaceAll("\\n", "<br />");
		withMarkup = markupURLs(withMarkup);
		withMarkup = markupEmails(withMarkup);
		return withMarkup;
	}
	
	public String markupEmails(String message) {
		String withMarkup = findEmails.matcher(message).replaceAll("<a href='mailto:$0'>$0</a>");
		return withMarkup;
	}

	public String markupURLs(String message) {
		Matcher urls = findURLs.matcher(message);
		StringBuffer out = new StringBuffer();
		while (urls.find()) {
			String url = urls.group();
			String trimmed = "";

			if (url.endsWith(",")) {
				url = url.substring(0, url.length() - 1);
				trimmed = ','+trimmed;
			}
			
			if (url.endsWith(".")) {
				url = url.substring(0, url.length() - 1);
				trimmed = '.'+trimmed;
			}
			// Find the right part of the URL
			if (message.charAt(Math.max(0, urls.start()-1)) == '(' && url.endsWith(")")) {
				url = url.substring(0, url.length() - 1);
				trimmed = ')'+trimmed;
				
			}
			// Keep the body of the tag.
			String text=url;
			// Now we know we're in a URL, fix any &amp; in the URL.
			url = url.replaceAll("&amp;", "&");
			// If there isn't a protocol, assume http://
			if(urls.group(1) == null) {
				url = "http://"+url;
			}
			urls.appendReplacement(out, "<a href='"+ url+ "' target='_blank'>"+text+"</a>"+ trimmed);
		}
		urls.appendTail(out);
		return out.toString();
	}
	
	public String format(String message) {
		String noMarkup = message.replaceAll("</p>", "<br /><br />");
		// convertFormattedText doesn't deal with <br> only <br >, using a pattern as it's case insensitive (<BR>)
		noMarkup = brCleanup.matcher(noMarkup).replaceAll("<br />");
		
		noMarkup = formattedText.convertFormattedTextToPlaintext(noMarkup);
		return noMarkup;
	}

}
