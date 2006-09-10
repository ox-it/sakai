package org.sakaiproject.hierarchy.impl;

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
			assertEquals("Root node check ", h.getName(), testRoot);
			checkNodes(h);
			hierarchyService.deleteNode(h);
			log.warn("Spring Injected Test Sucessfull..... but plesae remove in production ");
		}
		catch (Exception ex)
		{
			log.warn("Spring Injected Test Failed..... but plesae remove in production ",ex);
			log.error("Spring Injected Test Failed..... but plesae remove in production ");
			System.exit(-1);
		}
	}

	private void checkNodes(Hierarchy h) throws Exception
	{
		for (int i = 0; i < 10; i++)
		{
			String testPath = h.getName() + "/" + i;
			Hierarchy child1 = h.getChild(testPath);
			assertNotNull("Missing node path " + testPath, child1);
			assertEquals("Path name is not correct ", testPath, child1
					.getName());
			HierarchyProperty hp = child1.getProperty("propertyA" + i);
			assertNotNull("No property A node found ", hp);
			assertEquals("Property value of Prop A is ", "propertyvalueA" + i,
					hp.getPropvalue());
			hp = child1.getProperty("propertyB" + i);
			assertNotNull("No property B node found ", hp);
			assertEquals("Property value of Prop B is ", "propertyvalueB" + i,
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
			Hierarchy child1 = hierarchyService.newHierarchy(h.getName() + "/"
					+ i);
			HierarchyProperty hp = hierarchyService.newHierachyProperty();
			hp.setName("propertyA" + i);
			hp.setPropvalue("propertyvalueA" + i);
			h.addTochildren(child1);
			h.addToproperties(hp);
			hp = hierarchyService.newHierachyProperty();
			hp.setName("propertyB" + i);
			hp.setPropvalue("propertyvalueB" + i);
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
