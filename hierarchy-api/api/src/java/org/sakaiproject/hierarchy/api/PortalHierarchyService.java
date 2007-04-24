package org.sakaiproject.hierarchy.api;

import org.sakaiproject.hierarchy.api.model.Hierarchy;

public interface PortalHierarchyService extends HierarchyService {

	public static final String PORTAL_SITES = "portal:sites";
	public static final String MANAGEMENT_SITE = "portal:management-site";
	
	String getCurrentPortalPath();

	void setCurrentPortalPath(String portalPath);

	Hierarchy getCurrentPortalNode();

}
