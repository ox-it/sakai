package org.sakaiproject.hierarchy.api.model;


/**
 * This is the base type which we will store at nodes in the tree.
 */
public interface PortalNode {

	/**
	 * The ID of the node. This should not be exposed to the user.
	 * @return A String ID.
	 */
	public String getId();

	/**
	 * The name of this node at this point in the hierarchy.
	 * @return A String name for this part of the hierarchy.
	 */
	public String getName();
	
	/**
	 * The full path to this node in the hierarchy.
	 * The path should end in the name of this node.
	 * @see #getName()
	 * @return A String path.
	 */
	public String getPath();
	
	/**
	 * The title of this node that is displayed to the end user.
	 * @return A String title.
	 */
	public String getTitle();
	
	/**
	 * Can the user access this node.
	 * @return <code>true</code> if the user is allowed to see this node.
	 */
	public boolean canView();
	
	/**
	 * Can the user modify this node.
	 * @return <code>true</code> if the user is allowed to modify this node.
	 */
	public boolean canModify();
}
