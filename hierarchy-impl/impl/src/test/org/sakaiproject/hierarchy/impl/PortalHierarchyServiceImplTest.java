package org.sakaiproject.hierarchy.impl;


import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.exception.PermissionException;

public class PortalHierarchyServiceImplTest {

	// Check we fail when too many args are null
	@Test(expected = IllegalArgumentException.class)
	public void testNewNodeArgValidationNull() throws PermissionException {
		PortalHierarchyServiceImpl service = new PortalHierarchyServiceImpl();
		service.newNode("parentId", "childName", null, "managementSiteId", null, "title", false, false);
	}

	//Check we fail when too many args are set
	@Test(expected = IllegalArgumentException.class)
	public void testNewNodeArgValidationFull() throws PermissionException {
		PortalHierarchyServiceImpl service = new PortalHierarchyServiceImpl();
		service.newNode("parentId", "childName", "siteId", "managementSiteId", "redirectUrl", "title", false, false);
	}

	@Test
	public void testHash() {
		// Test the old hashing.
		Assert.assertEquals("AA4F6CD1CD5C8E2AADEBEDF0B384C29DEA9A34D4", PortalHierarchyServiceImpl.hash("hello"));
	}

	@Test
	public void testSha1() {
		// Test the new hashing (standard SHA1).
		Assert.assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", PortalHierarchyServiceImpl.sha1("hello"));
	}

}
