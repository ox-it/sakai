package org.sakaiproject.hierarchy.test;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

/**
 * Implementations of the Heararchy API should pass this test. To use it
 * subclass it and set contextable in a constructor.
 * 
 * @author Matthew Buckett matthew.buckett at oucs dot ox dot ac dot uk
 */
public abstract class HierarchyServiceTest extends ServiceTest
{

	private static final Log log = LogFactory
			.getLog(HierarchyServiceTest.class);


	public void testGetRoots() throws HierarchyServiceException
	{
		try
		{
			service.begin();
			Hierarchy node = service.newHierarchy("/rootnode");
			service.save(node);
			Collection roots = service.getRootNodes();
			assertEquals(1, roots.size());
			assertEquals("/rootnode", ((Hierarchy) roots.iterator().next()).getPath());
		}
		finally
		{
			service.end();
		}
	}

	public void testGetRootsEmpty()
	{
		try
		{
			service.begin();
			Collection roots = service.getRootNodes();
			assertEquals(0, roots.size());
		}
		finally
		{
			service.end();
		}
	}

	public void testGetRootsMultiple() throws HierarchyServiceException
	{
		try
		{
			service.begin();
			Hierarchy node = service.newHierarchy("/1");
			service.save(node);
			node = service.newHierarchy("/2");
			service.save(node);
			Collection roots = service.getRootNodes();
			assertEquals(2, roots.size());
		}
		finally
		{
			service.end();
		}
	}

	public void testSaveEmpty() throws HierarchyServiceException
	{
		try
		{
			service.begin();
			Hierarchy node = service.newHierarchy("/rootnode");
			service.save(node);
			assertNotNull(service.getNode("/rootnode"));
		}
		finally
		{
			service.end();
		}
	}

	public void testSaveWithProperty() throws HierarchyServiceException
	{
		try
		{
			service.begin();
			Hierarchy node = service.newHierarchy("/rootnode");

			node.setRealm("realm");
			node.addToproperties("name", "value");
			service.save(node);

			node = service.getNode("/rootnode");
			assertEquals("realm", node.getRealm());
			assertEquals("value", node.getProperty("name").getPropvalue());
		}
		finally
		{
			service.end();
		}
	}

	public void testSaveTree() throws HierarchyServiceException
	{
		try
		{
			service.begin();
			Hierarchy node, parent, root;

			parent = root = node = service.newHierarchy("/node");
			for (int count = 0; count < 20; count++)
			{
				node = service.newHierarchy(parent.getPath() + "/node");
				parent.addTochildren(node);
				parent = node;
			}
			service.save(root);
			assertNotNull(service.getNode("/node/node/node"));
		}
		finally
		{
			service.end();
		}
	}
	
	public void testNewHierarchy() throws HierarchyServiceException {
		assertNotNull(service.newHierarchy("/testnode"));
	}
	
	public void testNewHierarchyRootCreate() {
		try {
			service.newHierarchy("/");
			fail("Shouldn't be able to create root node (/)");
		} catch (HierarchyServiceException hse)
		{} // We should catch this.
	}
	
	public void testNewHierarchyDeepCreate() {
		try {
			Hierarchy node = service.newHierarchy("/path/does not/exist");
			service.save(node);
			fail("Shouldn't be able to create hierarchy with missing parents.");
		} catch (HierarchyServiceException hse)
		{} // Is should fall through.
	}
	
	public void testDeleteNode() throws HierarchyServiceException {
		Hierarchy node = service.newHierarchy("/roottest");
		service.save(node);
		node = service.getNode("/roottest");
		assertNotNull(node);
		service.deleteNode(node);
		assertNull(service.getNode("/roottest"));
	}
	
	public void testDeleteNodeMissing() {
		try {
			Hierarchy node = service.newHierarchy("/roottest");
			service.deleteNode(node);
		} catch (HierarchyServiceException hse) {
			fail("Deleting a node that doesn't exist shouldn't throw an Exception");
		} 
	}
	
	public void testGetNodeWithNull() {
		service.getNode(null);
	}
	
	public void testGetNodeWithSlash() {
		assertNull(service.getNode("/"));
	}
	
	public void testGetNodeMissing() {
		assertNull(service.getNode("/doesnotexist"));
	}
	
	public void testGetNode() throws HierarchyServiceException {
		Hierarchy node = service.newHierarchy("/roottest");
		service.save(node);
		node = service.getNode("/roottest");
		assertNotNull(node);
		assertEquals("/roottest", node.getPath());
	}
	
	public void testDeleteNodeTree() throws HierarchyServiceException {
		Hierarchy node = service.newHierarchy("/roottest");
		node.addTochildren(service.newHierarchy("/roottest/1"));
		node.addTochildren(service.newHierarchy("/roottest/2"));
		service.save(node);
		service.deleteNode(service.getNode("/roottest"));
		assertNull(service.getNode("/roottest"));
		assertNull(service.getNode("/roottest/1"));
	}
	
	public void testAddTochildren() throws HierarchyServiceException {
		Hierarchy root = service.newHierarchy("/roottest");
		Hierarchy child = service.newHierarchy("/roottest/child");
		root.addTochildren(child);
		service.save(root);
		assertNotNull(service.getNode("/roottest"));
		assertNotNull(service.getNode("/roottest/child"));
	}
	
	public void testAddTochildrenReversed() throws HierarchyServiceException {
		try {
			Hierarchy child = service.newHierarchy("/roottest");
			Hierarchy root = service.newHierarchy("/roottest/child");
			root.addTochildren(child);
			service.save(root);
			assertNotNull("/child");
			fail("Creating reverse paths should throw an exception.");
		} catch (HierarchyServiceException hse)
		{}
	}
	
	public void testPathEnforcement() throws HierarchyServiceException {
		Hierarchy parent = service.newHierarchy("/parent/child");
		Hierarchy child = service.newHierarchy("/parent");
		parent.addTochildren(child);
		service.save(parent);
		
		parent = service.getNode("/parent/child");
		assertNull(parent.getChild("/parent"));
		assertNotNull(parent.getChild("/parent/child/parent"));
	}
	




	
}