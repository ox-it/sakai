package org.sakaiproject.hierarchy.api.model;

import org.sakaiproject.site.api.Site;

/**
 * TODO: Should switch to using an internal SiteSummary object rather than depending on the site API.
 * @author buckett
 *
 */
public interface PortalNode {

	public String getId();
	
	/**
	 * The site associated with the current node.
	 * @return The site, this will never by <code>null</code>
	 */
	public Site getSite();
	
	public Site getManagementSite();
	
	public String getName();
	
	public String getPath();
	
	public String getTitle();
	
	/**
	 * Can the user access this node.
	 */
	public boolean canView();
	
	public boolean canModify();
}
