package uk.ac.ox.oucs.vle;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.springframework.web.context.support.WebApplicationContextUtils;

//@Provider
public class SpringComponentContextProvider implements ContextResolver<Object> {

	private ServletContext context;

	public SpringComponentContextProvider(@Context ServletContext context) {
		this.context = context;
	}
	
	public Object getContext(Class<?> type) {
		Map beans = WebApplicationContextUtils.getWebApplicationContext(context).getBeansOfType(type);
		if (!beans.isEmpty()) {
			return beans.values().iterator().next();
		}
		return null;
	}

}
