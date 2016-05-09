package org.sakaiproject.site.tool;

/**
 * Small helper class to stop adding lines to SiteAction.
 * @author buckett
 *
 */
public class SiteActionUtils {

	/**
	 * This just gets the HTML that is used to add the podcast picker to the Site Info tools config page.
	 * @param rss The URL of the RSS.
	 * @return The string to be added to the head of the HTML.
	 */
	public static String getPodcastPicker(String rss) {
		String html =
			"        <script>var sourceUrl = '"+ rss + "';</script>\n"+
			"        <script src=\"/library/newspicker/js/jquery.defer.lib.js\"></script>\n" + 
			"        <script src=\"/library/newspicker/js/jqmodal.lib.js\"></script>\n" + 
			"        <script src=\"/library/newspicker/js/podcaster.js\"></script>\n" +
			"        <script src=\"/library/newspicker/js/site-setup.js\"></script>\n" +
			"        <link href=\"/library/newspicker/css/jqModal.css\" rel=\"stylesheet\" type=\"text/css\" />\n" + 
			"        <link href=\"/library/newspicker/css/test.css\" rel=\"stylesheet\" type=\"text/css\" />\n" + 
			"        <link href=\"/library/newspicker/css/picker.css\" rel=\"stylesheet\" type=\"text/css\" />\n";
		return html;
	}
}
