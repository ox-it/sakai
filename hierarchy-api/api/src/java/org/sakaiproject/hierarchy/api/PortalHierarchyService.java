package org.sakaiproject.hierarchy.api;

import java.util.List;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;

public interface PortalHierarchyService {

	/**
	 * The event generated when a new node is created.
	 */
	String EVENT_NEW = "portal_hierarchy.new";
	/**
	 * The event generated when a node is deleted.
	 */
	String EVENT_DELETE = "portal_hierarchy.delete";
	/**
	 * The event generated when a node is modified.
	 */
	String EVENT_MODIFY = "portal_hierarchy.modify";
	
	/**
	 * The permission required to create a new node in the hierarchy.
	 */
	String SECURE_NEW = "portal.hierarchy.new";
	/**
	 * The permission required to delete a node in the hierarchy.
	 */
	String SECURE_DELETE = "portal.hierarchy.delete";
	/**
	 * The permission required to modify an node in the hierarchy.
	 */
	String SECURE_MODIFY = "portal.hierarchy.modify";

	/**
	 * Gets the current portal path associated with the request.
	 * @deprecated
	 * @see #getCurrentPortalNode()
	 * @return The path of the current portal node.
	 */
	String getCurrentPortalPath();

	/**
	 * Set the current PortalNode for this request in a thread local.
	 * This only accepts PortalNodeSite as PortalNodeRedirect doesn't make
	 * any sense to be the current node.
	 * @param node The node to set the current request to or <code>null</code> to clear it.
	 * @see #getCurrentPortalNode()
	 */
	void setCurrentPortalNode(PortalNodeSite node);

	/**
	 * Get the current PortalNode for this request.
	 * @return The current portal node or <code>null</code> if there isn't one.
	 * @see #setCurrentPortalNode(PortalNodeSite)
	 */
	PortalNodeSite getCurrentPortalNode();
	
	/**
	 * Get the node based on its nodePath.
	 * 
	 * @param nodePath The path of the node. If null or an empty string then get the node
	 * at the base of the service.
	 * @see #getRootNodes() 
	 * @return The found node or null if it couldn't be found.
	 */
	PortalNode getNode(String portalPath);

	PortalNode getNodeById(String id);
	
	/**
	 * This returns the primary portal node for a site.
	 * This is useful when you need to build a link to a site, but it might be placed in
	 * multiple locations in the hierarchy and you need to know which one you should 
	 * build a link to.
	 * @param siteId The site ID of the node.
	 * @return The default portal node for this site or <code>null</code> if there isn't a node
	 * with the supplied site ID.
	 */
	PortalNode getDefaultNode(String siteId);
	
	/**
	 * Find all the nodes in the hierarchy with the selected site attached.
	 * @param siteId The ID of the site to search for.
	 * @return A list of PortalNodes with this site ID.
	 */
	List<PortalNode> getNodesWithSite(String siteId);
	
	/**
	 * Get back the parent nodes for a Node. The supplied node is
	 * not contained in the list. The API enforces that PortalNodeSites can only be parents.
	 */
	List<PortalNodeSite> getNodesFromRoot(String nodeId);
	
	/**
	 * Get the children of a node.
	 * @param nodeId The node ID to find the children of.
	 * @return A list of portal nodes or an empty list if there are none.
	 */
	List<PortalNode> getNodeChildren(String nodeId);
	
	/**
	 * Delete a node.
	 * @param id The node to remove from the hierarchy.
	 * @throws PermissionException If the current user doesn't have permission to remove the node.
	 */
	void deleteNode(String id) throws PermissionException;

	/**
	 * Create a new Site Node in the hierarchy.
	 * @param parentId The ID of the node that will be it's parent. Not <code>null</code>.
	 * @param childName The name of the child node. Not <code>null</code>.
	 * @param siteId The Site ID of the site that will be linked to this node. Not <code>null</code>.
	 * @param managementSiteId The Site ID of the management site. Not <code>null</code>.
	 * @return The newly created PortalNodeSite.
	 * @throws PermissionException If the current user in't allowed to create new site nodes in the parent node.
	 */
	PortalNodeSite newSiteNode(String parentId, String childName, String siteId, String managementSiteId) throws PermissionException;
	
	/**
	 * Create a new redirect node in the hierarchy.
	 * @param parentId The ID of the node that will be it's parent. Not <code>null</code>.
	 * @param childName The name of the redirect node. Not <code>null</code>.
	 * @param redirectUrl The URL to send the user to when accessing this node. Not <code>null</code>.
	 * @param title The title of the node. Not <code>null</code>.
	 * @param appendPath Should the parts of the request after the node be appended to the redirect URL.
	 * @return The newly created PortalNodeRedirect.
	 * @throws PermissionException If the current user isn't allowed toe create redirect nodes in the parent node.
	 */
	PortalNodeRedirect newRedirectNode(String parentId, String childName, String redirectUrl, String title, boolean appendPath) throws PermissionException;
	
	/**
	 * Attempt to rename this node.
	 * @deprecated Renameing paths should not be done and moving is a better solution.
	 * @param id The ID of the node to rename.
	 * @param newPath The new name of the node.
	 * @throws PermissionException If the current user doesn't have permission to rename this node.
	 */
	void renameNode(String id, String newPath) throws PermissionException;

	/**
	 * Move this node and all it's children to a new location.
	 * @param id The ID of the node to move.
	 * @param newParent The ID of the node's new parent.
	 * @throws PermissionException If you don't have permission to move the node.
	 */
	void moveNode(String id, String newParent) throws PermissionException;
	
	/**
	 * Change the located at a node.
	 * @param id The ID of the node to update.
	 * @param newSiteId The Site ID of the new site.
	 * @throws PermissionException If the user can't update the current node or can't use the new site.
	 */
	void changeSite(String id, String newSiteId) throws PermissionException;
	
	/**
	 * Can the current user delete the node.
	 * @param id The ID of the node to check.
	 * @return <code>true</code> if the current user can delete the supplied node.
	 */
	boolean canDeleteNode(String id);
	
	/**
	 * Can the current user add a new node to the node?
	 * @param parentId The nodeId of the parent.
	 * @return <code>true</code> if the node can be added.
	 */
	boolean canNewNode(String parentId);
	
	/**
	 * Can the current user rename the current node.
	 * @deprecated Renaming paths should not be done and moving is a better solution.
	 * @param id The ID of the node to check.
	 * @return <code>true</code> if the node can be moved.
	 */
	boolean canRenameNode(String id);
	
	/**
	 * Can the current user move the node to another location?
	 * @param id The node ID of the node to move.
	 * @return <code>true</code> if the node can be moved.
	 */
	boolean canMoveNode(String id);
	
	/**
	 * Can the current user change the site at the current node.
	 * @param id The node ID of the node to update.
	 * @return <code>true</code> if the node can have it's site changed.
	 */
	boolean canChangeSite(String id);
	
	
}
