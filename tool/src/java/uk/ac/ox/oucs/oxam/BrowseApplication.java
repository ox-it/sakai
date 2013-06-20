package uk.ac.ox.oucs.oxam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;

import uk.ac.ox.oucs.oxam.pages.AdvancedSearchPage;
import uk.ac.ox.oucs.oxam.pages.SimpleSearchPage;

/**
 * Main application class for our app
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class BrowseApplication extends SakaiApplication {
   
	/**
	 * Configure your app here
	 */
	@Override
	protected void init() {
		super.init();
		//to put this app into deployment mode, see web.xml
		// We don't use mountBookmarkablePage as it's URL coding strategy doesn't cope
		// with space in the URL.
		mount(new QueryStringUrlCodingStrategy("/search", SimpleSearchPage.class));
		mount(new QueryStringUrlCodingStrategy("/advanced", AdvancedSearchPage.class));
	}

	/**
	 * The main page for our app
	 * 
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<? extends Page> getHomePage() {
		return SimpleSearchPage.class;
	}

	@Override
	public boolean isToolbarEnabled() {
		return true;
	}

	@Override
	public RequestCycle newRequestCycle(Request request, Response response) {
		return new SolrWebRequestCycle(this, request, response);
	}


}
