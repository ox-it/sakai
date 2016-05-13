package org.sakaiproject.hierarchy.test.impl;

import org.sakaiproject.hierarchy.test.PerformanceTest;

public class ImplPerformanceTest extends PerformanceTest {

	public ImplPerformanceTest() {
		hierarchyTest = new ImplServiceProvider();
	}
}
