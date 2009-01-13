package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

public class ExternalGroupsResource extends Resource {

	private String id;
	
	public ExternalGroupsResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		this.id = (String) request.getAttributes().get("group");
		
		// This representation has only one type of representation.
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
	}
	
	public Representation represent(Variant varient) throws ResourceException {

		Map<Object, Object> group = new HashMap<Object, Object>();
		group.put("id", id);
		group.put("name", "Example Name");

		Representation representation = new JsonRepresentation(group);
		return representation;
	}
	
	
}
