package uk.ac.ox.oucs.vle;

import java.util.Map;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.UserDirectoryService;

import edu.amc.sakai.user.LdapConnectionManager;

public class ExternalGroupsApplication extends Application {

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createRoot() {
		// Create a router Restlet that routes each call to a
		// new instance of HelloWorldResource.
		Router router = new Router(getContext());
		
//		ExternalGroupManagerImpl groupManager = new ExternalGroupManagerImpl();
//		groupManager.setLdapConnectionManager((LdapConnectionManager) ComponentManager.get(LdapConnectionManager.class));
//		groupManager.setUserDirectoryService((UserDirectoryService) ComponentManager.get(UserDirectoryService.class));
//				
//		Map<String, Object> attributes = getContext().getAttributes();
//		attributes.put(ExternalGroupManager.class.getName(), groupManager);

		// Defines only one route
		router.attach("/group/{group}", ExternalGroupsResource.class);

		return router;
	}
}
