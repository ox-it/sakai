package org.sakaiproject.hierarchy.api;

import org.sakaiproject.hierarchy.api.model.Hierarchy;

public interface PortalHierarchyService extends HierarchyService {
	
	String getCurrentPortalPath();

	void setCurrentPortalPath(String portalPath);

	Hierarchy getCurrentPortalNode();

}
