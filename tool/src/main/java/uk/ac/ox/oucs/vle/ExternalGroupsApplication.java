package uk.ac.ox.oucs.vle;

import java.util.Map;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Redirector;
import org.restlet.Restlet;
import org.restlet.Router;
import org.sakaiproject.site.api.SiteService;

public class ExternalGroupsApplication extends Application {

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createRoot() {
		// Create a router Restlet that routes each call to a
		// new instance of HelloWorldResource.
		
		Router router = new Router(getContext());
		
		// Info messages get made into ERROR messages :-( 
		getContext().getLogger().setLevel(Level.WARNING);
		
		Map<String, Object> attributes = getContext().getAttributes();
		attributes.put(ExternalGroupManager.class.getName(), ExternalGroupManagerCover.getInstance());
		attributes.put(SiteService.class.getName(), org.sakaiproject.site.cover.SiteService.getInstance());

		router.attach("/group/autocomplete", ExternalGroupsAuto.class);
		router.attach("/group/search", ExternalGroupsSearch.class); 
		router.attach("/group/{group}", ExternalGroupsResource.class);
		router.attach("/group/{group}/members", ExternalGroupMembers.class);
		router.attach("/mapped/", MappedGroupsResource.class);
		router.attach("/mapped/{group}", MappedGroupsResource.class);
		router.attach("/site/{site}/addgroup", SiteAddGroup.class);
				// Load stuff out of the webapp (ServletWarClient.class)
		Directory directory =  new Directory(getContext(), "war:///static/");
		router.attach("/static/",directory);
        router.attach("/", new Redirector(getContext(), "static/search.html?site=admin", Redirector.MODE_CLIENT_TEMPORARY));

		return router;
	}
}
