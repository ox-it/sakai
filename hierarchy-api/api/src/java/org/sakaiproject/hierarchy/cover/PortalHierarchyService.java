package org.sakaiproject.hierarchy.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.hierarchy.api.model.PortalNode;

public class PortalHierarchyService {
	
	private static org.sakaiproject.hierarchy.api.PortalHierarchyService m_instance = null;
	public static org.sakaiproject.hierarchy.api.PortalHierarchyService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.hierarchy.api.PortalHierarchyService) ComponentManager
						.get(org.sakaiproject.hierarchy.api.PortalHierarchyService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.hierarchy.api.PortalHierarchyService) ComponentManager
					.get(org.sakaiproject.hierarchy.api.PortalHierarchyService.class);
		}
	}
	public static void changeSite(String id, String newSiteId) {
		getInstance();
		if (m_instance == null) return;
		m_instance.changeSite(id, newSiteId);
	}
	public static void deleteNode(String id) {
		getInstance();
		if (m_instance == null) return;
		m_instance.deleteNode(id);
	}
	public static PortalNode getCurrentPortalNode() {
		getInstance();
		if (m_instance == null) return null;
		return m_instance.getCurrentPortalNode();
	}
	public static String getCurrentPortalPath() {
		getInstance();
		if (m_instance == null) return null;
		return m_instance.getCurrentPortalPath();
	}
	public static PortalNode getNode(String portalPath) {
		getInstance();
		if (m_instance == null) return null;
		return m_instance.getNode(portalPath);
	}
	public static PortalNode getNodeById(String id) {
		getInstance();
		if (m_instance == null) return null;
		return m_instance.getNodeById(id);
	}
	public static List<PortalNode> getNodeChildren(String siteid) {
		getInstance();
		if (m_instance == null) return null;
		return m_instance.getNodeChildren(siteid);
	}
	public static List<PortalNode> getNodesToRoot(String siteId) {
		getInstance();
		if (m_instance == null) return null;
		return m_instance.getNodesFromRoot(siteId);
	}
	public static List<PortalNode> getNodesWithSite(String siteId) {
		getInstance();
		if (m_instance == null) return null;
		return m_instance.getNodesWithSite(siteId);
	}
	public static void moveNode(String id, String newParent) {
		getInstance();
		if (m_instance == null) return;
		m_instance.moveNode(id, newParent);
	}
	public static PortalNode newNode(String parentId, String childName, String siteId,
			String managementSiteId) {
		getInstance();
		if (m_instance == null) return null;
		return m_instance
				.newNode(parentId, childName, siteId, managementSiteId);
	}
	public static void renameNode(String id, String newPath) {
		getInstance();
		if (m_instance == null) return;
		m_instance.renameNode(id, newPath);
	}
	public static void setCurrentPortalPath(String portalPath) {
		getInstance();
		if (m_instance == null) return;
		m_instance.setCurrentPortalPath(portalPath);
	}

}

