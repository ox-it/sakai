package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.Comparator;
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
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class ExternalGroupsSearch extends Resource {

	private List<ExternalGroup> groups;
	
	private static Comparator<ExternalGroup> sorter = new Comparator<ExternalGroup>() {

		public int compare(ExternalGroup o1, ExternalGroup o2) {
			return (o1.getName() != null)?o1.getName().compareTo(o2.getName()):-1;
		}
		
	};

	public ExternalGroupsSearch(Context context, Request request,
			Response response) {
		super(context, request, response);
		ExternalGroupManager externalGroupManager = (ExternalGroupManager) context
				.getAttributes().get(ExternalGroupManager.class.getName());

		String query = (String) request.getResourceRef().getQueryAsForm()
				.getFirstValue("q");
		groups = externalGroupManager.search(query);
		Collections.sort(groups,sorter);
		
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}

	public Representation represent(Variant varient) throws ResourceException {
		if (MediaType.TEXT_JAVASCRIPT.equals(varient.getMediaType())) {
			JSONArray groupsJSON = new JSONArray();

			for (ExternalGroup group : groups) {
				groupsJSON.put(ExternalGroupsResource.convertGroupToMap(group));
			}

			Representation representation = new JsonRepresentation(groupsJSON);
			return representation;
		} else {
			StringBuilder output = new StringBuilder();
			for (ExternalGroup group : groups) {
				output.append(group.getName());
				output.append("\n");
			}
			return new StringRepresentation(output.toString());
		}
	}
	

}
