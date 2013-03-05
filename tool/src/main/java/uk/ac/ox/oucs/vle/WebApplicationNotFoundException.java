package uk.ac.ox.oucs.vle;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;

public class WebApplicationNotFoundException extends WebApplicationException {

	public WebApplicationNotFoundException() {
		super(Response.status(Response.Status.NOT_FOUND)
				.entity(mapper)
				.type(MediaType.APPLICATION_JSON)
				.build());
	}
}
