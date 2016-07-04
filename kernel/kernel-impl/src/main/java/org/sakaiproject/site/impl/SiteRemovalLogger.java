package org.sakaiproject.site.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteRemovalAdvisor;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class just logs out when a site is removed and who by. At the moment this is useful as you can restore
 * a site by re-creating it with the same site ID.
 *
 * @author Matthew Buckett
 */
public class SiteRemovalLogger implements SiteRemovalAdvisor {

	private static final Log LOG = LogFactory.getLog(SiteRemovalLogger.class);

	private UserDirectoryService userDirectoryService;
	private SiteService siteService;
	private DateFormat format;

	public SiteRemovalLogger() {
		format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		// Dateformatter tries to use the current machine's timezone to format the date and this doesn't work
		// historically (eg GMT/BST).
		// This way we get back to UTC.
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void init() {
		siteService.addSiteRemovalAdvisor(this);
	}

	public void destroy() {
		siteService.removeSiteRemovalAdvisor(this);
	}

	public void removed(Site site) {
		LOG.info(buildMessage(site));
	}

	String buildMessage(Site site) {
		StringBuilder message = new StringBuilder();
		message.append("Removing Site ID: ").append(site.getId());
		message.append(" title: ").append(site.getTitle());
		message.append(" type: ").append(site.getType());

		if (site.isSoftlyDeleted()) {
			message.append(" marked for deletion by: ").append(displayUser(site.getModifiedBy()));
			if (site.getModifiedDate()!= null) {
				message.append(" at: ").append(displayDate(site.getModifiedDate()));
			}
		}
		message.append(" removed by: ").append(displayUser(userDirectoryService.getCurrentUser()));
		return message.toString();
	}

	/**
	 * Display details of the user or "unknown" if <code>null</code>.
	 * @param user The user to display, can be <code>null</code>.
	 * @return A string identifying the user.
	 */
	String displayUser(User user) {
		return (user == null)?"unknown":user.getDisplayName()+ "("+ user.getDisplayId()+ ")";

	}

	/**
	 * Formats a date.
	 * @param date The date to format.
	 * @return A date in UTC.
	 */
	String displayDate(Date date) {
		return format.format(date);
	}

}
