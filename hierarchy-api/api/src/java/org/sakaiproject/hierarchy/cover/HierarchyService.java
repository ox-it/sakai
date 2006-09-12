/**
 * 
 */
package org.sakaiproject.hierarchy.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

/**
 * @author ieb
 *
 */
public class HierarchyService 
{

	private static org.sakaiproject.hierarchy.api.HierarchyService m_instance = null;
	public static org.sakaiproject.hierarchy.api.HierarchyService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.hierarchy.api.HierarchyService) ComponentManager
						.get(org.sakaiproject.hierarchy.api.HierarchyService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.hierarchy.api.HierarchyService) ComponentManager
					.get(org.sakaiproject.hierarchy.api.HierarchyService.class);
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.HierarchyService#getRootNodes()
	 */
	public static List getRootNodes()
	{
		org.sakaiproject.hierarchy.api.HierarchyService service = getInstance();
		if (service == null) return null;
		return service.getRootNodes();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.HierarchyService#getNode(java.lang.String)
	 */
	public static Hierarchy getNode(String nodePath)
	{
		org.sakaiproject.hierarchy.api.HierarchyService service = getInstance();
		if (service == null) return null;
		return service.getNode(nodePath);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.HierarchyService#deleteNode(org.sakaiproject.hierarchy.api.model.Hierarchy)
	 */
	public void deleteNode(Hierarchy node)
	{
		org.sakaiproject.hierarchy.api.HierarchyService service = getInstance();
		if (service == null) return;
		service.deleteNode(node);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.HierarchyService#save(org.sakaiproject.hierarchy.api.model.Hierarchy)
	 */
	public void save(Hierarchy node)
	{
		org.sakaiproject.hierarchy.api.HierarchyService service = getInstance();
		if (service == null) return;
		service.save(node);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.HierarchyService#newHierarchy(java.lang.String)
	 */
	public Hierarchy newHierarchy(String nodePath)
	{
		org.sakaiproject.hierarchy.api.HierarchyService service = getInstance();
		if (service == null) return null;
		return service.newHierarchy(nodePath);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.hierarchy.api.HierarchyService#newHierachyProperty()
	 */
	public HierarchyProperty newHierachyProperty()
	{
		org.sakaiproject.hierarchy.api.HierarchyService service = getInstance();
		if (service == null) return null;
		return service.newHierachyProperty();
	}

}
