package uk.ac.ox.it.shoal;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class SakaiApplication extends WebApplication {
	/**
	 * Configure your app here
	 */
	@Override
	protected void init() {

        SpringComponentInjector injector = new SpringComponentInjector(this);
		getComponentInstantiationListeners().add(injector);


		//Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		
		//Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);
		

		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(getHomePage());
		getApplicationSettings().setAccessDeniedPage(getHomePage());

	}

	public abstract boolean isToolbarEnabled();


}
