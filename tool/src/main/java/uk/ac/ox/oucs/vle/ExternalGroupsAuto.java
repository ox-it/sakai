package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class ExternalGroupsAuto extends Resource {

	private List<ExternalGroup> groups;

	public ExternalGroupsAuto(Context context, Request request,
			Response response) {
		super(context, request, response);
		ExternalGroupManager externalGroupManager = (ExternalGroupManager) context
				.getAttributes().get(ExternalGroupManager.class.getName());

		String query = (String) request.getResourceRef().getQueryAsForm()
				.getFirstValue("q");
		groups = externalGroupManager.search(query);
		Collections.sort(groups, ExternalGroupsSearch.sorter);
		
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
	}
	
	public Representation getRepresentation(Variant variant) {
		if (MediaType.TEXT_JAVASCRIPT.equals(variant.getMediaType())) {
			StringBuilder output = new StringBuilder();
			for (ExternalGroup group : groups) {
				output.append(group.getName());
				output.append("\n");
			}
			if (output.length() == 0) {
				return Representation.createEmpty();
			}
			return new StringRepresentation(output.toString());
		}
		return null;
	}
	
}
