package org.sakaiproject.hierarchy.cover;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

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
	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.PortalHierarchyService#getRootNodes()
	 */
	public static Collection getRootNodes()
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return null;
		return service.getRootNodes();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.PortalHierarchyService#getNode(java.lang.String)
	 */
	public static Hierarchy getNode(String nodePath)
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return null;
		return service.getNode(nodePath);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.HierarchyService#getNode(java.lang.String)
	 */
	public static Hierarchy getNodeById(String id)
	{
		org.sakaiproject.hierarchy.api.HierarchyService service = getInstance();
		if (service == null) return null;
		return service.getNodeById(id);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.PortalHierarchyService#deleteNode(org.sakaiproject.hierarchy.api.model.Hierarchy)
	 */
	public static void deleteNode(Hierarchy node)
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return;
		service.deleteNode(node);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.PortalHierarchyService#save(org.sakaiproject.hierarchy.api.model.Hierarchy)
	 */
	public static void save(Hierarchy node) throws HierarchyServiceException
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return;
		service.save(node);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.PortalHierarchyService#newHierarchy(java.lang.String)
	 */
	public static Hierarchy newHierarchy(String nodePath) throws HierarchyServiceException
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return null;
		return service.newHierarchy(nodePath);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.PortalHierarchyService#newHierachyProperty()
	 */
	public static HierarchyProperty newHierachyProperty()
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return null;
		return service.newHierachyProperty();
	}
	public static String getCurrentPortalPath()
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return null;
		return service.getCurrentPortalPath();
	}
	public static void setCurrentPortalPath(String portalPath)
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return;
		service.setCurrentPortalPath(portalPath);
		
	}
	public static Hierarchy getCurrentPortalNode()
	{
		org.sakaiproject.hierarchy.api.PortalHierarchyService service = getInstance();
		if (service == null) return null;
		return service.getCurrentPortalNode();
	}
}

