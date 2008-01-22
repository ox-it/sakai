package org.sakaiproject.hierarchy.api.model;

import org.sakaiproject.site.api.Site;

/**
 * TODO: Should switch to using an internal SiteSummary object rather than depending on the site API.
 * @author buckett
 *
 */
public interface PortalNode {

	public String getId();
		
	public Site getSite();
	
	public Site getManagementSite();
	
	public String getName();
	
	public String getPath();
	
	public boolean canView();
	
	public boolean canModify();
}
