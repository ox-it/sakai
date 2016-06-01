package org.sakaiproject.hierarchy.test;

import org.sakaiproject.hierarchy.api.HierarchyService;

import junit.framework.TestCase;

public abstract class ServiceTest extends TestCase {

	protected HierarchyService service;
	protected ServiceProvider hierarchyTest;

	public ServiceTest() {
		super();
	}

	public void setUp() throws Exception {
		hierarchyTest.setUp();
		service = hierarchyTest.getService();
	}

	public void tearDown() throws Exception {
		hierarchyTest.tearDown();
	}

}