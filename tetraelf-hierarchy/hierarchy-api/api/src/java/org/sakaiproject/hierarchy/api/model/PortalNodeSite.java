package org.sakaiproject.hierarchy.api.model;

import org.sakaiproject.site.api.Site;

/**
 * This is a node that we store in the tree which indicates there 
 * is a site here.
 * 
 * @author buckett
 *
 */
public interface PortalNodeSite extends PortalNode {
	
	/**
	 * The site associated with the current node.
	 * @return The site, this will never be <code>null</code>.
	 */
	public Site getSite();
	
	/**
	 * The management site is the site which contains all the management tools for
	 * the portal. Typically it has a site ID of !hierarchy.
	 * @return The management site, this will never be <code>null</code>.
	 */
	public Site getManagementSite();
}
