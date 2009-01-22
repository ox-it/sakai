package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public class SiteAddGroup extends Resource {

	private Site site;

	public SiteAddGroup(Context context, Request request, Response response) {
		super(context, request, response);
		String siteId = (String) request.getAttributes().get("site");
		SiteService siteService = (SiteService) context.getAttributes().get(SiteService.class.getName());
		try {
			site = siteService.getSite(siteId); 
		} catch (IdUnusedException idue) {
			setAvailable(false);
		}
		
		getVariants().add(new Variant(MediaType.TEXT_JAVASCRIPT));
	}
	
	public Representation getRepresentation(Variant variant) {
		if (MediaType.TEXT_JAVASCRIPT.equals(variant.getMediaType())) {
			// If we attempt to just push the whole site object over json 
			// it finds too much data and we run out of heap.
			Map<Object, Object> data = new HashMap<Object, Object>();
			data.put("id", site.getId());
			data.put("title", site.getTitle());
			JSONArray jsonRoles = new JSONArray();
			for (Role role: (Set<Role>)site.getRoles()) {
				Map<String, String> jsonRole = new HashMap<String, String>();
				jsonRole.put("id", role.getId());
				jsonRole.put("description", role.getDescription());
				jsonRoles.put(jsonRole);
			}
			data.put("roles", jsonRoles);
			return new JsonRepresentation(data);
		}
		return null;
		
	}
}
