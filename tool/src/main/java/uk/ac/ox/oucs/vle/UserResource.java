package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.ObjectNode;

import uk.ac.ox.oucs.vle.proxy.SakaiProxy;
import uk.ac.ox.oucs.vle.proxy.UserProxy;

/**
 * Class to allow details of the current user to be returned.
 * At the moment this is used to get details of the current date so we don't rely on client clocks.
 * @author buckett
 *
 */
@Path("/user")
public class UserResource {

	private SakaiProxy proxy;
	private CourseSignupService courseService;
	private JsonFactory jsonFactory;
	private ObjectMapper objectMapper;

	public UserResource(@Context ContextResolver<Object> resolver) {
		this.courseService = (CourseSignupService) resolver.getContext(CourseSignupService.class);
		this.proxy = (SakaiProxy) resolver.getContext(SakaiProxy.class);
		
		jsonFactory = new JsonFactory();
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationConfig.Feature.USE_STATIC_TYPING, true);
		objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
	}

	@Path("/current")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrent() throws JsonGenerationException, JsonMappingException, IOException {
		UserProxy user = proxy.getCurrentUser();
		Date now = courseService.getNow();
		ObjectNode rootNode = objectMapper.createObjectNode();
		rootNode.putPOJO("user", user);
		rootNode.put("date", now.getTime());
		return Response.ok(objectMapper.writeValueAsString(rootNode)).build();
	}
	
	@Path("/find")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers(@QueryParam("search")String search) throws JsonGenerationException, JsonMappingException, IOException {
		if (search == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		UserProxy user = proxy.findUserById(search);
		if (user == null) {
			user = proxy.findUserByEid(search);
			if (user == null) {
				user = proxy.findUserByEmail(search);
			}
		}
		// Now process.
		if (user == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.ok(objectMapper.writeValueAsString(user)).build();
		}
	}
	
}
