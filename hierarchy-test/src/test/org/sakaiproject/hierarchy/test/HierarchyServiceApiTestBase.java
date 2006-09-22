package org.sakaiproject.hierarchy.test;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

/**
 * Implementations of the Heararchy API should pass this test. To use it
 * subclass it and provide a HierarchyService implementation.
 * 
 * @author Matthew Buckett matthew.buckett at oucs dot ox dot ac dot uk
 */
public abstract class HierarchyServiceApiTestBase extends TestCase
{

	private static final Log log = LogFactory
			.getLog(HierarchyServiceApiTestBase.class);

	/**
	 * Set the service to something useful in your subclass setup.
	 */
	protected HierarchyService service;

	public void testGetRoots() throws HierarchyServiceException
	{
		try
		{
			service.begin();
			Hierarchy node = service.newHierarchy("/rootnode");
			service.save(node);
			List roots = service.getRootNodes();
			assertEquals(1, roots.size());
			assertEquals("/rootnode", ((Hierarchy) roots.get(0)).getPath());
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
			List roots = service.getRootNodes();
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
			List roots = service.getRootNodes();
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

	public void testPathEnforcement() throws HierarchyServiceException
	{
		try
		{
			service.begin();
			Hierarchy parent = service.newHierarchy("/parent/child");
			log.info("Check Parent");
			print(" ", parent, 100);
			Hierarchy child = service.newHierarchy("/parent");
			log.info("Check Child");
			print(" ", child, 100);
			parent.addTochildren(child);
			log.info("Added Parent");
			print(" ", parent, 100);
			log.info("Added Child");
			print(" ", child, 100);
			service.save(parent);
			log.info("Saved Parent");
			print(" ", parent, 100);

			parent = service.getNode("/parent/child");
			log.info("Found Parent ");
			print(" ", parent, 100);
			assertNull(parent.getChild("/parent"));
			assertNotNull(parent.getChild("/parent/child/parent"));
		}
		finally
		{
			service.end();
		}
	}

	protected void printList(String indent, Iterator i, int depth)
	{
		if (depth == 0)
		{
			return;
		}
		while (i.hasNext())
		{
			Object o = i.next();
			if (o instanceof Hierarchy)
			{
				print(indent, (Hierarchy) o, depth);
			}
			else if (o instanceof HierarchyProperty)
			{
				print(indent, (HierarchyProperty) o);
			}
			else
			{
				log.info(indent + "Unrecognised Node :" + o);
			}
		}

	}

	protected void print(String indent, HierarchyProperty property)
	{
		log.info(indent + "Property " + property.getName() + "("
				+ property.getVersion() + "):" + property.getPropvalue());

	}

	protected void print(String indent, Hierarchy hierarchy, int depth)
	{
		log.info("Node " + hierarchy.getPath() + "(" + hierarchy.getVersion()
				+ ")");
		printList("      ", hierarchy.getProperties().values().iterator(),
				depth);
		printList("", hierarchy.getChildren().values().iterator(), depth - 1);
	}

}