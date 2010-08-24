package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.node.ObjectNode;


@Path("/debug")
public class DebugResource {

	
	private JsonFactory jsonFactory;
	private CourseSignupService courseService;
	private Populator populator;
	private SakaiProxy proxy;
	private ObjectMapper objectMapper;

	public DebugResource(@Context ContextResolver<Object> resolver) {
		this.courseService = (CourseSignupService) resolver.getContext(CourseSignupService.class);
		this.populator = (Populator)resolver.getContext(Populator.class);
		this.proxy = (SakaiProxy) resolver.getContext(SakaiProxy.class);
		this.objectMapper = new ObjectMapper();
	}
	
	@Path("/user")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser() {
		OutputStream out = new ByteArrayOutputStream();
		try {
			objectMapper.writeValue(out, proxy.getCurrentUser());
			return Response.ok(out.toString()).build();
		} catch (JsonGenerationException e) {
			throw new WebApplicationException(e);
		} catch (JsonMappingException e) {
			throw new WebApplicationException(e);
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}
	
	@Path("/date")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDate() throws JsonGenerationException, JsonMappingException, IOException {
		Date now = courseService.getNow();
		ObjectNode node = objectMapper.createObjectNode();
		node.put("now", now.getTime());
		return Response.ok(objectMapper.writeValueAsString(node)).build();
	}

	@Path("/date")
	@POST
	public Response setDate(@FormParam("now")long newNow) {
		Date now = new Date(newNow);
		courseService.setNow(now);
		return Response.ok().build();
	}
	
	@Path("/reload")
	@POST
	public Response reload() {
		populator.update();
		return Response.ok().build();
	}

	@Path("/emails")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEmails() throws JsonGenerationException,
			JsonMappingException, IOException {
		List<Email> emails = proxy.getEmails();
		String json = objectMapper.typedWriter(
				TypeFactory.collectionType(List.class, Email.class))
				.writeValueAsString(emails);
		return Response.ok(json).build();
	}

}
