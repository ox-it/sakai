package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.sakaiproject.user.api.User;

public class ExternalGroupsResource extends Resource {

	private ExternalGroup group;
	private ExternalGroupManager externalGroupManager;

	public ExternalGroupsResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		externalGroupManager = (ExternalGroupManager) context.getAttributes().get(ExternalGroupManager.class.getName());

		String id = (String) request.getAttributes().get("group");
		group = externalGroupManager.findExternalGroup(id);
		if (group == null) {
			setAvailable(false);
		}
			
		// This representation has only one type of representation.
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
	}
	
	public Representation represent(Variant varient) throws ResourceException {

		Map<Object, Object> groupMap = convertGroupToMap(group, false);

		Representation representation = new JsonRepresentation(groupMap);
		return representation;
	}

	static Map<Object, Object> convertGroupToMap(ExternalGroup externalGroup, boolean includeMembers) {
		Map<Object, Object> groupMap = new HashMap<Object, Object>();
		groupMap.put("id", externalGroup.getId());
		groupMap.put("name", externalGroup.getName());
		if (includeMembers) {
			JSONArray members = new JSONArray();
			for(Iterator<User> userIt = externalGroup.getMembers(); userIt.hasNext();) {
				User user = userIt.next();
				members.put(convertUserToMap(user));
			}
			groupMap.put("members", members);
		}
		return groupMap;
	}
	
	static Map<String, String> convertUserToMap(User user) {
		Map<String, String> userMap = new HashMap<String, String>();
		userMap.put("id", user.getId());
		userMap.put("name", user.getDisplayName());
		return userMap;
	}
	
	
}
