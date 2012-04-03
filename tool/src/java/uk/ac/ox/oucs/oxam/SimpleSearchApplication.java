package uk.ac.ox.oucs.oxam;

import org.apache.wicket.Page;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;

import uk.ac.ox.oucs.oxam.pages.SimpleSearchPage;

public class SimpleSearchApplication extends SakaiApplication {

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
		getResourceSettings().getStringResourceLoaders().add(new ClassStringResourceLoader(BrowseApplication.class));
		
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return SimpleSearchPage.class;
	}

	@Override
	public boolean isToolbarEnabled() {
		return false;
	}

}
