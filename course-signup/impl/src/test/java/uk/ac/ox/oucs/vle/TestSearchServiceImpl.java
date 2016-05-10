/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Just quickly test my logic.
 * @author Matthew Buckett
 */
public class TestSearchServiceImpl {

	private SearchServiceImpl searchService;

	@Before
	public void setUp() {
		searchService = new SearchServiceImpl();
	}

	@Test
	public void testSolrUrlNoSlash() {
		searchService.setSolrUrl("http://localhost:8983/solr");
		assertEquals("http://localhost:8983/solr/", searchService.getSolrUrl());
	}

	@Test
	public void testSolrUrlWithSlash() {
		searchService.setSolrUrl("http://localhost:8983/solr/");
		assertEquals("http://localhost:8983/solr/", searchService.getSolrUrl());
	}

	@Test(expected = IllegalStateException.class)
	public void testSolrUrlUnSet() {
		searchService.getSolrUrl();
	}
}
