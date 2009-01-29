package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sakaiproject.user.api.User;


@Path("/group/")
public class ExternalGroupsResource {

	private ExternalGroupManager externalGroupManager;
	
	static Comparator<ExternalGroup> sorter = new Comparator<ExternalGroup>() {
	
		public int compare(ExternalGroup o1, ExternalGroup o2) {
			return (o1.getName() != null)?o1.getName().compareTo(o2.getName()):-1;
		}
		
	};

	public ExternalGroupsResource(@Context Providers provider) {
		ContextResolver<Object> componentMgr = provider.getContextResolver(Object.class, null);
		this.externalGroupManager = (ExternalGroupManager)componentMgr.getContext(ExternalGroupManager.class);
	}

	@Path("{group}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getGroup(@PathParam("group") String id) {
		ExternalGroup group = externalGroupManager.findExternalGroup(id);
		if (group != null) {
			return Response.ok(convertGroupToMap(group)).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
	
	@Path("autocomplete")
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response getAutocompleteGroups(@QueryParam("q") String query) {
		List<ExternalGroup>groups = externalGroupManager.search(query);
		Collections.sort(groups, ExternalGroupsResource.sorter);
		StringBuilder output = new StringBuilder();
		for(ExternalGroup group: groups) {
			output.append(group.getName());
			output.append("\n");
		}
		return Response.ok(output.toString()).build();
	}
	
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getGroups(@QueryParam("q") String query) {
		List<ExternalGroup>groups = externalGroupManager.search(query);
		Collections.sort(groups, ExternalGroupsResource.sorter);
		JSONArray groupsJson = new JSONArray();
		for(ExternalGroup group: groups) {
			groupsJson.put(convertGroupToMap(group));
		}
		return Response.ok(groupsJson).build();
	}
	
	@Path("{group}/members")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getMembers(@PathParam("group") String id) {
		ExternalGroup group = externalGroupManager.findExternalGroup(id);
		if (group == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		JSONArray membersArray = new JSONArray();
		for (Iterator<User> userIt = group.getMembers(); userIt.hasNext();) {
			User user = userIt.next();
			Map<Object, Object> userObject = new HashMap<Object, Object>();
			userObject.put("id", user.getId());
			userObject.put("name", user.getDisplayName());
			userObject.put("username", user.getDisplayId());
			membersArray.put(new JSONObject(userObject));
		}
		return Response.ok(membersArray).build();
	}
	
	static JSONObject convertGroupToMap(ExternalGroup externalGroup) {
		Map<Object, Object> groupMap = new HashMap<Object, Object>();
		groupMap.put("id", externalGroup.getId());
		groupMap.put("name", externalGroup.getName());
		return new JSONObject(groupMap);
	}

}
