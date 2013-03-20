package org.sakaiproject.hierarchy.api.model;


/**
 * This is the base type which we will store at nodes in the tree.
 */
public interface PortalNode {

	public String getId();

	
	public String getName();
	
	public String getPath();
	
	public String getTitle();
	
	/**
	 * Can the user access this node.
	 */
	public boolean canView();
	
	public boolean canModify();
}
