package org.sakaiproject.hierarchy.api;

import java.util.List;

import org.sakaiproject.hierarchy.api.model.PortalNode;

public interface PortalHierarchyService {
	
	String getCurrentPortalPath();

	void setCurrentPortalPath(String portalPath);

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
	
	/**
	 * Find all the nodes in the hierarchy with the selected site attached.
	 * @param siteId The ID of the site to search for.
	 * @return
	 */
	List<PortalNode> getNodesWithSite(String siteId);
	
	List<PortalNode> getNodesFromRoot(String siteId);
	
	List<PortalNode> getNodeChildren(String siteid);
	
	/**
	 * delete nodes also removes the properties
	 * 
	 * @param nodePath
	 */
	void deleteNode(String id);

	PortalNode newNode(String parentId, String childName, String siteId, String managementSiteId);
	
	void renameNode(String id, String newPath);

	void moveNode(String id, String newParent);
	
	void changeSite(String id, String newSiteId);
	
}
