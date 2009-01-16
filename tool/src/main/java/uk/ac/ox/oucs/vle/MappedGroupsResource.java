package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

public class MappedGroupsResource extends Resource {
	
	private ExternalGroupManager externalGroupManager;
	private String group;
	private String externalGroupId;

	public MappedGroupsResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		externalGroupManager = (ExternalGroupManager) context.getAttributes().get(ExternalGroupManager.class.getName());

		group = (String) request.getAttributes().get("group");
		if (Method.GET.equals(request.getMethod())) {
			if (group == null || group.trim().length() == 0) {
				setAvailable(false);
			} else {
				externalGroupId = externalGroupManager.findExternalGroupId(group);
				if (externalGroupId == null) {
					setAvailable(false);
				}
			}
		}
			
		// This representation has only one type of representation.
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
	}
	
	public Representation represent(Variant variant) {
		Map<Object,Object> jsonMap = new HashMap<Object, Object>();
		jsonMap.put("id", group);
		jsonMap.put("group", externalGroupId);
		return new JsonRepresentation(jsonMap);
	}
	
	public boolean allowPost() {
		return true;
	}
	public void acceptRepresentation(Representation representation) {
		if (representation.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
			Form form = new Form(representation);
			String group = form.getFirstValue("group");
			String role = form.getFirstValue("role");
			
			// TODO Should split it out.
			ValidationErrors errors = new ValidationErrors();
			if (group == null || group.length() == 0) {
				errors.addError("group", "Group cannot be empty");
			}
			if (role == null || role.length() == 0) {
				errors.addError("role", "Role cannot be empty");
			}
			
			if (errors.hasErrors()) {
				sendErrors(errors.getAllErrors());
			} else {
				try {
				String id = externalGroupManager.addMappedGroup(group,role);
				Representation responseRep = new JsonRepresentation(new JSONObject(Collections.singletonMap("id", id)));
				getResponse().setEntity(responseRep);
				} catch (IllegalArgumentException iae) {
					sendErrors(Collections.singletonList(iae.getMessage()));
				}
			}
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private void sendErrors(List<String> allErrors) {
		Representation responseRep = new JsonRepresentation(new JSONObject(Collections.singletonMap("errors", new JSONArray(allErrors))));
		getResponse().setEntity(responseRep);
		getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}
	

}
