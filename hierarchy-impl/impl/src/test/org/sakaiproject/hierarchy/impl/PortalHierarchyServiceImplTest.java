package org.sakaiproject.hierarchy.impl;


import org.junit.Test;
import org.sakaiproject.exception.PermissionException;

public class PortalHierarchyServiceImplTest {

	// Check we fail when too many args are null
	@Test(expected = IllegalArgumentException.class)
	public void testNewNodeArgValidationNull() throws PermissionException {
		PortalHierarchyServiceImpl service = new PortalHierarchyServiceImpl();
		service.newNode("parentId", "childName", null, "managementSiteId", null, "title", false);
	}

	//Check we fail when too many args are set
	@Test(expected = IllegalArgumentException.class)
	public void testNewNodeArgValidationFull() throws PermissionException {
		PortalHierarchyServiceImpl service = new PortalHierarchyServiceImpl();
		service.newNode("parentId", "childName", "siteId", "managementSiteId", "redirectUrl", "title", false);
	}

}
