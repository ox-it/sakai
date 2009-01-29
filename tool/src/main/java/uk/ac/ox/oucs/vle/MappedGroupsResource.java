package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;

import org.json.JSONArray;
import org.json.JSONObject;

@Path("/mapped/")
public class MappedGroupsResource {
	
	private ExternalGroupManager externalGroupManager;

	public MappedGroupsResource(@Context ContextResolver<Object> resolver) {
		externalGroupManager = (ExternalGroupManager)resolver.getContext(ExternalGroupManager.class);
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response addMappedGroup(@FormParam("group") String group, @FormParam("role") String role) {
		// TODO Should split it out.
		ValidationErrors errors = new ValidationErrors();
		if (group == null || group.length() == 0) {
			errors.addError("group", "Group cannot be empty");
		}
		if (role == null || role.length() == 0) {
			errors.addError("role", "Role cannot be empty");
		}
		
		if (errors.hasErrors()) {
			return Response.status(Status.BAD_REQUEST).entity(convertErrors(errors.getAllErrors())).build();
		} else {
			try {
				String id = externalGroupManager.addMappedGroup(group,role);
				return Response.ok(new JSONObject(Collections.singletonMap("id", id))).build();
			} catch (IllegalArgumentException iae) {
				return Response.status(Status.BAD_REQUEST).entity(Collections.singletonList(iae.getMessage())).build();
			}
		}
	}
	
	
	@Path("{group}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getMappedGroup(@PathParam("group") String group) {
		String externalGroupId = externalGroupManager.findExternalGroupId(group);
		Map<Object,Object> jsonMap = new HashMap<Object, Object>();
		jsonMap.put("id", group);
		jsonMap.put("group", externalGroupId);
		return Response.ok(jsonMap).build();
	}


	private JSONObject convertErrors(List<String> allErrors) {
		return new JSONObject(Collections.singletonMap("errors", new JSONArray(allErrors)));
	}
	

}
