package uk.ac.ox.oucs.oxam.pages;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Fragment;

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
