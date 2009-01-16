package uk.ac.ox.oucs.vle;

import java.util.Map;

import org.restlet.Application;
import org.restlet.Directory;
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


		router.attach("/group/{group}", ExternalGroupsResource.class);
		router.attach("/group/search/", ExternalGroupsSearch.class);
		router.attach("/mapped/", MappedGroupsResource.class);
		router.attach("/mapped/{group}", MappedGroupsResource.class);
		// Load stuff out of the webapp (ServletWarClient.class)
		router.attach("/static/", new Directory(getContext(), "war:///static/"));

		return router;
	}
}
