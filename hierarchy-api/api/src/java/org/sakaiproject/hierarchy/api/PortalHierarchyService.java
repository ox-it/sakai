package org.sakaiproject.hierarchy.api;

import org.sakaiproject.hierarchy.api.model.Hierarchy;

public interface PortalHierarchyService extends HierarchyService {

	public static final String PORTAL_SITES = "portal:sites";
	public static final String MANAGEMENT_SITE = "portal:management-site";
	public static final String CONTENT = "portal:content";
	public static final String TITLE = "portal:title";
	
	String getCurrentPortalPath();

	void setCurrentPortalPath(String portalPath);

	Hierarchy getCurrentPortalNode();

}
