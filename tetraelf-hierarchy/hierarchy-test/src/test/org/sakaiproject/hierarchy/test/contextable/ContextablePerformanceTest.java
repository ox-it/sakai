package org.sakaiproject.hierarchy.test.contextable;

import org.sakaiproject.hierarchy.test.PerformanceTest;

public class ContextablePerformanceTest extends
		PerformanceTest {

	public ContextablePerformanceTest() {
		hierarchyTest = new ContextableServiceProvider();
	}
	
}
