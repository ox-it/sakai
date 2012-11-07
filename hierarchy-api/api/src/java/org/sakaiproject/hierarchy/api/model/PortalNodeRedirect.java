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

	public String getUrl();
}
