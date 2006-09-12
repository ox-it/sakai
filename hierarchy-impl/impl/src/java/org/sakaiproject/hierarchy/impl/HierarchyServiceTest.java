package org.sakaiproject.hierarchy.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

public class HierarchyServiceTest
{
	private static final Log log = LogFactory.getLog(HierarchyServiceTest.class);
	private HierarchyService hierarchyService = null;

	public void init()
	{
		try
		{
			String testRoot = "/rootTestNode";

			Hierarchy h = hierarchyService.getNode(testRoot);
			if (h != null)
			{
				hierarchyService.deleteNode(h);
			}
			h = hierarchyService.newHierarchy(testRoot);
			addNodes(h);
			hierarchyService.save(h);

			h = hierarchyService.getNode(testRoot);
			assertEquals("Root node check ", h.getPath(), testRoot);
			checkNodes(h);
			//hierarchyService.deleteNode(h);
			log.info("Testing Navigation ");
			List l = hierarchyService.getRootNodes();
			printList("",l.iterator());
			log.warn("Spring Injected Test Sucessfull..... but plesae remove in production ");
		}
		catch (Exception ex)
		{
			log.warn("Spring Injected Test Failed..... but plesae remove in production ",ex);
			log.error("Spring Injected Test Failed..... but plesae remove in production ");
			System.exit(-1);
		}
	}

	private void printList(String indent, Iterator i)
	{
		while ( i.hasNext() ) {
			Object o = i.next();
			if ( o instanceof Hierarchy ) {
				print(indent,(Hierarchy)o);
			} else if ( o instanceof HierarchyProperty ) {
				print(indent,(HierarchyProperty)o);
			} else {
				log.info(indent+"Unrecognised Node :"+o);
			}
		}
		
	}

	private void print(String indent, HierarchyProperty property)
	{
		log.info(indent+"Property "+property.getName()+"(" + property.getVersion() +
				"):"+property.getPropvalue());
		
	}

	private void print(String indent, Hierarchy hierarchy)
	{
		log.info("Node "+hierarchy.getPath()+"("+hierarchy.getVersion()+")");
		printList("      ",hierarchy.getProperties().values().iterator());
		printList("",hierarchy.getChildren().values().iterator());		
	}

	private void checkNodes(Hierarchy h) throws Exception
	{
		for (int i = 0; i < 10; i++)
		{
			String testPath = h.getPath() + "/" + i;
			Hierarchy child1 = h.getChild(testPath);
			assertNotNull("Missing node path  " + testPath, child1);
			assertEquals("Path name is not correct ", testPath, child1
					.getPath());
			HierarchyProperty hp = child1.getProperty("propertyA" + i);
			assertNotNull("No property "+testPath+"/propertyA"+i+" node found ", hp);
			assertEquals("Property value of "+testPath+"/propertyA"+i+" is ", "propertyvalueA" + i,
					hp.getPropvalue());
			hp = child1.getProperty("propertyB" + i);
			assertNotNull("No property "+testPath+"/propertyB"+i+" node found ", hp);
			assertEquals("Property value of "+testPath+"/propertyB"+i+" is ", "propertyvalueB" + i,
					hp.getPropvalue());
			
		}
	}

	private void assertEquals(String message, String expected, String actual)
			throws Exception
	{
		if (!expected.equals(actual))
		{
			throw new Exception(message + ":" + expected + "!=" + actual);
		}

	}

	private void assertNotNull(String message, Object o) throws Exception
	{
		if (o == null)
		{
			throw new Exception(message + ": is null ");
		}
	}

	private void addNodes(Hierarchy h)
	{
		for (int i = 0; i < 10; i++)
		{
			Hierarchy child1 = hierarchyService.newHierarchy(h.getPath() + "/"
					+ i);
			h.addTochildren(child1);

			HierarchyProperty hp = hierarchyService.newHierachyProperty();
			hp.setName("propertyA" + i);
			hp.setPropvalue("propertyvalueA" + i);
			child1.addToproperties(hp);
			
			hp = hierarchyService.newHierachyProperty();
			hp.setName("propertyB" + i);
			hp.setPropvalue("propertyvalueB" + i);
			child1.addToproperties(hp);
		}
	}

	public HierarchyService getHierarchyService()
	{
		return hierarchyService;
	}

	public void setHierarchyService(HierarchyService hierarchyService)
	{
		this.hierarchyService = hierarchyService;
	}

}

