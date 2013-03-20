package org.sakaiproject.hierarchy.cover;

import org.sakaiproject.component.cover.ComponentManager;

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

}

