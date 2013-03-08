package uk.ac.ox.oucs.vle;

import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WebAppNotFoundException extends WebApplicationException {
	
	public WebAppNotFoundException() {
		super(Response
				.status(Response.Status.NOT_FOUND)
				.entity(new HashMap<String,String>() {{
					put("status", "failed");
					put("message", "The requested resource was not found");}})
				.build());
	}

}
