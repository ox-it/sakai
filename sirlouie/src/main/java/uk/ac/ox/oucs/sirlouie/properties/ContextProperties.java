package uk.ac.ox.oucs.sirlouie.properties;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Takes the properties from the web.xml config through the context, as opposed to being configured through sakai.propeties.
 */
public class ContextProperties extends SirLouieProperties {

	private static final long serialVersionUID = 1L;

	public ContextProperties(ServletContext context) throws ServletException {
		
		super();
		
        Enumeration<?> keys = context.getInitParameterNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = context.getInitParameter(key);
            setProperty(key, value);
        }
	}

}
