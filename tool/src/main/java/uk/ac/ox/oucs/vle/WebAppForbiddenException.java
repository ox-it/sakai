package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WebAppForbiddenException extends WebApplicationException {
	
	private static final Map<String, String> myMap = new HashMap<String, String>();
	static {
		myMap.put("status", "failed");
		myMap.put("message", "Not Authorized");
	}

	public WebAppForbiddenException() {
		super(Response
				.status(Response.Status.FORBIDDEN)
				.type(MediaType.APPLICATION_JSON)
				.entity(myMap)
				.build());
	}

}
