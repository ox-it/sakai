package org.sakaiproject.hierarchy.test;

import org.sakaiproject.hierarchy.api.HierarchyService;

public interface ServiceProvider {
	
	public void setUp() throws Exception;
	public void tearDown() throws Exception;
	public HierarchyService getService();

}
