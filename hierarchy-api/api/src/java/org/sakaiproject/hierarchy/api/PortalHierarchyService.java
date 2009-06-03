package org.sakaiproject.hierarchy.api;

import java.util.List;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.model.PortalNode;

public interface PortalHierarchyService {

	String EVENT_NEW = "portal_hierarchy.new";
	String EVENT_DELETE = "portal_hierarchy.delete";
	String EVENT_MODIFY = "portal_hierarchy.modify";

	String getCurrentPortalPath();

	void setCurrentPortalNode(PortalNode node);

	PortalNode getCurrentPortalNode();
	
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
	
	PortalNode getDefaultNode(String siteId);
	
	/**
	 * Find all the nodes in the hierarchy with the selected site attached.
	 * @param siteId The ID of the site to search for.
	 * @return
	 */
	List<PortalNode> getNodesWithSite(String siteId);
	
	List<PortalNode> getNodesFromRoot(String nodeId);
	
	List<PortalNode> getNodeChildren(String nodeId);
	
	/**
	 * delete nodes also removes the properties
	 * 
	 * @param nodePath
	 * @throws IllegalStateException When there are still children of this node.
	 */
	void deleteNode(String id) throws PermissionException, IllegalStateException;

	PortalNode newNode(String parentId, String childName, String siteId, String managementSiteId) throws PermissionException;
	
	void renameNode(String id, String newPath) throws PermissionException;

	void moveNode(String id, String newParent) throws PermissionException;
	
	void changeSite(String id, String newSiteId) throws PermissionException;
	
	boolean canDeleteNode(String id);
	
	boolean canNewNode(String parentId);
	
	boolean canRenameNode(String id);
	
	boolean canMoveNode(String id);
	
	boolean canChangeSite(String id);
	
	
}
