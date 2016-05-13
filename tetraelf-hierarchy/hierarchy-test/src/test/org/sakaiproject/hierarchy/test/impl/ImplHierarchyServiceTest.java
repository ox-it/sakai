package org.sakaiproject.hierarchy.test.impl;

import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.test.HierarchyServiceTest;

public class ImplHierarchyServiceTest extends HierarchyServiceTest {

	public ImplHierarchyServiceTest() {
		hierarchyTest = new ImplServiceProvider();
	}
	
	public void testGetNode() throws HierarchyServiceException {
		super.testGetNode();
		
		// Default implementation has no root node 
		Hierarchy root = service.getNode(null);
		assertNull(root);
		root = service.getNode("");
		assertNull(root);
	}
}
