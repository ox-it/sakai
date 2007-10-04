package org.sakaiproject.hierarchy.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;


/**
 * Test the performance of the implementation. Not really a good use of jUnit but hey.
 * @author buckett
 */
public abstract class PerformanceTest extends ServiceTest {
	
	private static Log log = LogFactory.getLog(PerformanceTest.class);
	

	private long current;
	
	private void logTime() {
		if (current != 0) {
			System.out.println(System.currentTimeMillis() - current);
		}
		current = System.currentTimeMillis();
	}
	
	
	public void testLargeNoRootNodes() throws HierarchyServiceException {
		try {
			logTime();
			service.begin();
			for (int i = 0; i < 2000; i++) {
				Hierarchy node = service.newHierarchy("/rootnode"+i);
				service.save(node);
			}
			logTime();
			Iterator roots = service.getRootNodes().iterator();
			while(roots.hasNext()) {
				assertTrue( ((Hierarchy)roots.next()).getPath().startsWith("/rootnode"));
			}
			logTime();
			assertEquals(2000, service.getRootNodes().size());
			logTime();
		} finally {
			service.end();
		}
	}
	
	public void testLargeNoChildren() throws HierarchyServiceException {
		try {
			logTime();
			service.begin();
			Hierarchy node = service.newHierarchy("/rootnode");
			for (int i = 0; i < 2000; i++) {
				Hierarchy child = service.newHierarchy("/rootnode/child"+i);
				node.addTochildren(child);
			}
			service.save(node);
			logTime();
			Iterator children = service.getNode("/rootnode").getChildren().entrySet().iterator();
			while(children.hasNext()) {
				assertTrue( ((Hierarchy)((Entry)children.next()).getValue()).getPath().startsWith("/rootnode"));
			}
			logTime();
			assertEquals(2000, service.getNode("/rootnode").getChildren().size());
			logTime();
		} finally {
			service.end();
		}
	}
	
	
	public void testDeeplyNestedNodes() throws HierarchyServiceException {
		try {
			service.begin();
			
			StringBuffer path = new StringBuffer("/node");
			Hierarchy parent = service.newHierarchy(path.toString());
			Hierarchy node;
			int created = 1;
			do {
				path.append("/node");
				node = service.newHierarchy(path.toString());
				node.setParent(parent);
				service.save(node);
				parent = node;
				
			} while (++created < 1000);
			
			node = service.getNode(path.toString());
			int found = 1;
			while(node.getParent() != null) {
				node = node.getParent();
				found++;
			}
			assertEquals("/node", node.getPath());
			assertEquals(1000, found);
			
			
		} finally {
			service.end();
		}
	}
	
	public void testLargeNoProperties() throws HierarchyServiceException {
		try {
			service.begin();
			
			Hierarchy node = service.newHierarchy("/node");
			for (int i = 0; i < 1000; i++) {
				HierarchyProperty prop = service.newHierachyProperty();
				prop.setName("name"+i);
				prop.setPropvalue("value");
				node.addToproperties(prop);
			}
			service.save(node);
			
			node = service.getNode("/node");
			Map properties = node.getProperties();
			assertEquals(1000, properties.size());
			Iterator props = properties.keySet().iterator();
			while(props.hasNext()) {
				assertTrue( props.next().toString().startsWith("name"));
			}
				
		} finally {
			service.end();
		}
	}
	
	public void testZBigInjection() throws Exception {
		try
		{
			service.begin();
			String testRoot = "/rootTestNode";
	
			Hierarchy h = service.getNode(testRoot);
			if (h != null)
			{
				service.deleteNode(h);
			}
			h = service.newHierarchy(testRoot);
			addNodes(h, 5);
			service.save(h);
	
			h = service.getNode(testRoot);
			assertEquals("Root node check ", h.getPath(), testRoot);
			checkNodes(h);
			Collection l = service.getRootNodes();
			printList("", l.iterator(), 1);
			printList("", l.iterator(), 2);
			
			service.deleteNode(h);
		}
		finally
		{
			service.end();
		}
	
	}

	private void addNodes(Hierarchy h, int depth) throws HierarchyServiceException {
		if (depth == 0)
		{
			return;
		}
		for (int i = 0; i < 5; i++)
		{
			Hierarchy child1 = service.newHierarchy(h.getPath()
					+ "/annothernodeinthechain" + i);
			h.addTochildren(child1);
	
			HierarchyProperty hp = service.newHierachyProperty();
			hp.setName("propertyA" + i);
			hp.setPropvalue("propertyvalueA" + i);
			child1.addToproperties(hp);
	
			hp = service.newHierachyProperty();
			hp.setName("propertyB" + i);
			hp.setPropvalue("propertyvalueB" + i);
			child1.addToproperties(hp);
			addNodes(child1, depth - 1);
		}
	}

	private void checkNodes(Hierarchy h) throws Exception {
		for (int i = 0; i < 5; i++)
		{
			String testPath = h.getPath() + "/annothernodeinthechain" + i;
			Hierarchy child1 = h.getChild(testPath);
			assertNotNull("Missing node path  " + testPath, child1);
			assertEquals("Path name is not correct ", testPath, child1
					.getPath());
			HierarchyProperty hp = child1.getProperty("propertyA" + i);
			assertNotNull("No property " + testPath + "/propertyA" + i
					+ " node found ", hp);
			assertEquals("Property value of " + testPath + "/propertyA" + i
					+ " is ", "propertyvalueA" + i, hp.getPropvalue());
			hp = child1.getProperty("propertyB" + i);
			assertNotNull("No property " + testPath + "/propertyB" + i
					+ " node found ", hp);
			assertEquals("Property value of " + testPath + "/propertyB" + i
					+ " is ", "propertyvalueB" + i, hp.getPropvalue());
	
		}
	}
	
	protected void printList(String indent, Iterator i, int depth)
	{
		if ( depth == 0 ) {
			return;
		}
		while ( i.hasNext() ) {
			Object o = i.next();
			if ( o instanceof Hierarchy ) {
				print(indent,(Hierarchy)o,depth);
			} else if ( o instanceof HierarchyProperty ) {
				print(indent,(HierarchyProperty)o);
			} else {
				log.info(indent+"Unrecognised Node :"+o);
			}
		}
		
	}

	protected void print(String indent, HierarchyProperty property)
	{
		log.info(indent+"Property "+property.getName()+"(" + property.getVersion() +
				"):"+property.getPropvalue());
		
	}

	protected void print(String indent, Hierarchy hierarchy, int depth)
	{
		
		log.info("Node "+hierarchy.getPath()+"("+hierarchy.getVersion()+")");
		printList("      ",hierarchy.getProperties().values().iterator(),depth);
		printList("",hierarchy.getChildren().values().iterator(),depth-1);		
	}
	
}
