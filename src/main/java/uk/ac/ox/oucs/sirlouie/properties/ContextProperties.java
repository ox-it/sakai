package uk.ac.ox.oucs.sirlouie.properties;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class ContextProperties extends SirLouieProperties {

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
