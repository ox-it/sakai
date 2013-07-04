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
