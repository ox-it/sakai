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

	private ExternalGroup group;

	public ExternalGroupsResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		String id = (String) request.getAttributes().get("group");
		ExternalGroupManager externalGroupManager = (ExternalGroupManager) context.getAttributes().get(ExternalGroupManager.class.getName());
		
		group = externalGroupManager.findExternalGroup(id);
		if (group == null) {
			setAvailable(false);
		}
			
		// This representation has only one type of representation.
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
	}
	
	public Representation represent(Variant varient) throws ResourceException {

		
		Map<Object, Object> groupMap = new HashMap<Object, Object>();
		groupMap.put("id", group.getId());
		groupMap.put("name", group.getName());

		Representation representation = new JsonRepresentation(groupMap);
		return representation;
	}
	
	
}
