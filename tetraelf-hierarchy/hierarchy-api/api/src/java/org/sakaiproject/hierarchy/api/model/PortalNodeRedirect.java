package org.sakaiproject.hierarchy.api.model;

/**
 * This is a node which indicates we should redirect to somewhere else.
 * That other location could be another node in the hierarchy or it could
 * be a external site.
 * 
 * @author buckett
 *
 */
public interface PortalNodeRedirect extends PortalNode {

	/**
	 * The URL to redirect to. This can be absolute or relative to the host.
	 * @return A String to redirect the user's browser to.
	 */
	public String getUrl();
	
	/**
	 * Should extra path parameters be appended to the redirect URL?
	 * This allows a hierarchy of sites and pages to be moved and a redirect setup which 
	 * preserves requests to individual pages in a site.
	 * 
	 * @return <code>true</code> if extra path parameters should be appended to the redirect URL.
	 */
	public boolean isAppendPath();

	/**
	 * Should this node be hidden?
	 * This is to allow redirects to sites to exist but them not to be displayed in most
	 * places throughout the interface. This is useful for example when you want to
	 * rename a node (keep it in the same place in the hierarchy), but want an invisible
	 * redirect from the old name.
	 *
	 * @return <code>true</code> if the node should be hidden,
	 */
	public boolean isHidden();
}
