package uk.ac.ox.oucs.vle;

import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WebAppForbiddenException extends WebApplicationException {
	
	public WebAppForbiddenException() {
		super(Response
				.status(Response.Status.FORBIDDEN)
				.entity(new HashMap<String,String>() {{
					put("status", "failed");
					put("message", "Not Authorized");}})
				.build());
	}

}
