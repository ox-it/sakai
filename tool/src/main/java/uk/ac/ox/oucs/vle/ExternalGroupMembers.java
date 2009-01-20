package uk.ac.ox.oucs.vle;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.sakaiproject.user.api.User;

public class ExternalGroupMembers extends Resource {

	private ExternalGroupManager externalGroupManager;
	private ExternalGroup group;

	public ExternalGroupMembers(Context context, Request request,
			Response response) {

		super(context, request, response);
		externalGroupManager = (ExternalGroupManager) context.getAttributes()
				.get(ExternalGroupManager.class.getName());

		String id = (String) request.getAttributes().get("group");
		group = externalGroupManager.findExternalGroup(id);
		if (group == null) {
			setAvailable(false);
		}

		// This representation has only one type of representation.
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));

	}

	public Representation getRepresentation(Variant variant) {
		if (MediaType.TEXT_JAVASCRIPT.equals(variant.getMediaType())) {
			JSONArray membersArray = new JSONArray();
			try {
				for (Iterator<User> userIt = group.getMembers(); userIt
						.hasNext();) {
					User user = userIt.next();
					JSONObject userObject = new JSONObject();

					userObject.put("id", user.getId());
					userObject.put("name", user.getDisplayName());
					userObject.put("username", user.getDisplayId());
					membersArray.put(userObject);
				}
			} catch (JSONException jsone) {
				throw new IllegalArgumentException(
						"Looks like we had a null key, who knows how, stupid API.",
						jsone);
			}
			return new JsonRepresentation(membersArray);
		}
		return null;
	}

}
