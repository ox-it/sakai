package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

@Path("/site/")
public class SiteResource {

	private final static Log log = LogFactory.getLog(SiteResource.class);
	
	private SiteService siteService;
	
	private AuthzGroupService authzGroupService;
	
	public SiteResource(@Context ContextResolver<Object> resolver) {
		siteService = (SiteService) resolver.getContext(SiteService.class);
		authzGroupService = (AuthzGroupService) resolver.getContext(AuthzGroupService.class);
	}

	@Path("/{site}/addgroup")
	@GET
	public Response getSiteAddGroup(@PathParam("site") String siteId) {
		Site site;
		try {
			site = siteService.getSiteVisit(siteId); 
		} catch (IdUnusedException idue) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (PermissionException pe) {
			return Response.status(Status.FORBIDDEN).build();
		}
		return convertSiteToJSON(site);
	}

	@Path("/placement/{placement}")
	@GET
	public Response getSiteFromPlacement(@PathParam("placement") String placementId) {
		Site site;
		ToolConfiguration toolConfiguration = siteService.findTool(placementId);
		try {
			if (toolConfiguration != null) {
				site = siteService.getSiteVisit(toolConfiguration.getSiteId());
				return convertSiteToJSON(site);
			}
		} catch (IdUnusedException idue) {
			log.warn("Placement had siteId that doesn't exist: "+ placementId);
		} catch (PermissionException pe) {
			return Response.status(Status.FORBIDDEN).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@SuppressWarnings("unchecked")
	private Response convertSiteToJSON(Site site) {
		Map<Object, Object> data = new HashMap<Object, Object>();
		data.put("id", site.getId());
		data.put("title", site.getTitle());
		JSONArray jsonRoles = new JSONArray();
		for (Role role: (Set<Role>)site.getRoles()) {
			if (authzGroupService.isRoleAssignable(role.getId())) {
				Map<String, String> jsonRole = new HashMap<String, String>();
				jsonRole.put("id", role.getId());
				jsonRole.put("description", role.getDescription());
				jsonRole.put("name", authzGroupService.getRoleName(role.getId()));
				jsonRoles.put(jsonRole);
			}
		}
		data.put("roles", jsonRoles);
		return Response.ok(new JSONObject(data)).build();
	}

}
