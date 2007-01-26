package org.sakaiproject.hierarchy.test.contextable;

import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.test.HierarchyServiceTest;

public class ContextableHierarchyServiceTest extends
		HierarchyServiceTest {

	public ContextableHierarchyServiceTest() {
		hierarchyTest = new ContextableServiceProvider();
	}

	public void testGetNode() throws HierarchyServiceException {
		super.testGetNode();
		
		// Contextable has a root node.
		Hierarchy root = service.getNode(null);
		assertNotNull(root);
		root = service.getNode("");
		assertNotNull(root);
	}
	
}
