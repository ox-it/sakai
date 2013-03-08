package uk.ac.ox.oucs.vle;

import java.util.Map;

import com.sun.jersey.api.core.PackagesResourceConfig;

public class RestApplication extends PackagesResourceConfig {

	public RestApplication(Map<String, Object> props) {
		super(props);
		getClasses().add(org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider.class);
	}
}
