package uk.ac.ox.oucs.vle;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * Allows to component manager references to be "injected" into resources without an explicit dependency.
 * @author buckett
 *
 */
@Provider
public class SakaiComponentContextProvider implements ContextResolver<Object> {
	
	public Object getContext(Class<?> type) {
		return (Object) ComponentManager.get(type);
	}

}
