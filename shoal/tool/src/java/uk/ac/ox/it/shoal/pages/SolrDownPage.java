package uk.ac.ox.it.shoal.pages;

import org.apache.solr.client.solrj.SolrServerException;

/**
 * Custom error page to display.
 *
 * @author Matthew Buckett
 */
public class SolrDownPage extends SakaiPage {
	private final SolrServerException cause;

	public SolrDownPage(SolrServerException cause) {
		this.cause = cause;
	}

}
