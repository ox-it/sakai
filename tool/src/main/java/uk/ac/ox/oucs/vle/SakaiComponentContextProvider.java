package uk.ac.ox.oucs.vle;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * Allows to component manager references to be "injected" into resources without an explicit dependency.
 * @author buckett
 */
@Provider
public class SakaiComponentContextProvider implements ContextResolver<Object> {
	
	private static final Log log = LogFactory.getLog(SakaiComponentContextProvider.class);
	
	public Object getContext(Class<?> type) {
		try {
			return (Object) ComponentManager.get(type.getName());
		} catch (NoClassDefFoundError ncdfe) {
			log.warn("Failed to find Sakai component manager");
			return null;
		}
	}

}
