package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WebAppNotFoundException extends WebApplicationException {
	
	private static final Map<String, String> myMap = new HashMap<String, String>();
	static {
		myMap.put("status", "failed");
		myMap.put("message", "The requested resource was not found");
	}
	
	public WebAppNotFoundException() {
		super(Response
				.status(Response.Status.NOT_FOUND)
				// We set the type as when the request doesn't have an accept header
				// jersey will attempt to convert a Map to application/octet-stream
				// which will fail.
				.type(MediaType.APPLICATION_JSON)
				.entity(myMap)
				.build());
	}

}
