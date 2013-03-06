package uk.ac.ox.oucs.vle;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

public class WebApplicationNotFoundException extends WebApplicationException {

	public WebApplicationNotFoundException(JSONObject jsonObject) {
		super(Response.status(Response.Status.NOT_FOUND)
				.entity(jsonObject)
				.type(MediaType.APPLICATION_JSON)
				.build());
	}
}
