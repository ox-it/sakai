package uk.ac.ox.oucs.vle;

import java.util.Map;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;

public class ExternalGroupsApplication extends Application {

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createRoot() {
		// Create a router Restlet that routes each call to a
		// new instance of HelloWorldResource.
		Router router = new Router(getContext());
		
		Map<String, Object> attributes = getContext().getAttributes();
		attributes.put(ExternalGroupManager.class.getName(), ExternalGroupManagerCover.getInstance());

		// Defines only one route
		router.attach("/group/{group}", ExternalGroupsResource.class);

		return router;
	}
}
