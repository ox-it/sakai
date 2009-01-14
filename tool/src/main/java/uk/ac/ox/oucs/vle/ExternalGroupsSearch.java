package uk.ac.ox.oucs.vle;

import java.util.List;

import org.json.JSONArray;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

public class ExternalGroupsSearch extends Resource {

	private List<ExternalGroup> groups;

	public ExternalGroupsSearch(Context context, Request request,
			Response response) {
		super(context, request, response);
		ExternalGroupManager externalGroupManager = (ExternalGroupManager) context.getAttributes().get(ExternalGroupManager.class.getName());
		
		String query = (String) request.getResourceRef().getQueryAsForm().getFirstValue("query");
		groups = externalGroupManager.search(query);
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
	}

	public Representation represent(Variant varient) throws ResourceException {
		JSONArray groupsJSON = new JSONArray();
		
		for (ExternalGroup group: groups) {
			groupsJSON.put(ExternalGroupsResource.convertGroupToMap(group));
		}

		Representation representation = new JsonRepresentation(groupsJSON);
		return representation;
	}
}
