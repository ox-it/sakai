package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

@Path("/signup")
public class SignupResource {

	
	private CourseSignupService courseService;
	private JsonFactory jsonFactory;
	private ObjectMapper objectMapper;

	public SignupResource(@Context ContextResolver<Object> resolver) {
		this.courseService = (CourseSignupService) resolver.getContext(CourseSignupService.class);
		jsonFactory = new JsonFactory();
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationConfig.Feature.USE_STATIC_TYPING, true);
		objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
	}
	
	@Path("/my")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getMySignups() {
		final List<CourseSignup> signups = courseService.getMySignups(null);
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}
		};
	}
	
	@Path("/my/course/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getMyCourseSignups(@PathParam("id") String courseId) {
		final List<CourseSignup> signups = courseService.getMySignups(null);
		final List<CourseSignup> courseSignups = new ArrayList<CourseSignup>();
		for(CourseSignup signup: signups) {
			if (courseId.equals(signup.getGroup().getId())) {
				courseSignups.add(signup);
			}
		}
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, courseSignups);
			}
		}; 
	}
	
	@Path("/new")
	@POST
	public Response signup(@FormParam("courseId") String courseId, @FormParam("components")Set<String> components, @FormParam("email")String email, @FormParam("message")String message) {
		courseService.signup(courseId, components, email, message);
		return Response.ok().build();
	}
	
	@Path("{id}/accept")
	@POST
	public Response accept(@PathParam("id") final String signupId) {
		courseService.accept(signupId);
		return Response.ok().build();
	}

	@Path("{id}/reject")
	@POST
	public Response reject(@PathParam("id") final String signupId) {
		courseService.reject(signupId);
		return Response.ok().build();
	}

	@Path("{id}/withdraw")
	@POST
	public Response withdraw(@PathParam("id") final String signupId) {
		courseService.withdraw(signupId);
		return Response.ok().build();
	}

	@Path("{id}/approve")
	@POST
	public Response approve(@PathParam("id") final String signupId) {
		courseService.approve(signupId);
		return Response.ok().build();
	}
	
	@Path("/course/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getCourseSignups(@PathParam("id") final String courseId) {
		// All the pending 
		return new StreamingOutput() {
			
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				List<CourseSignup> signups = courseService.getCourseSignups(courseId);
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}
		};
	}
}
