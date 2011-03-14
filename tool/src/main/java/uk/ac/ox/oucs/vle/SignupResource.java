package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;

import uk.ac.ox.oucs.vle.CourseSignupService.Status;

@Path("/signup")
public class SignupResource {

	
	private CourseSignupService courseService;
	private ObjectMapper objectMapper;

	public SignupResource(@Context ContextResolver<Object> resolver) {
		this.courseService = (CourseSignupService) resolver.getContext(CourseSignupService.class);
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

	@Path("/my/new")
	@POST
	public Response signup(@FormParam("courseId") String courseId, @FormParam("components")Set<String> components, @FormParam("email")String email, @FormParam("message")String message) {
		String user = courseService.findSupervisor(email);
		courseService.signup(courseId, components, email, message);
		return Response.ok().build();
	}

	@Path("/new")
	@POST
	public Response signup(@FormParam("userId")String userId, @FormParam("courseId") String courseId, @FormParam("components")Set<String> components) {
		courseService.signup(userId, courseId, components);
		return Response.ok().build();
	}
	
	@Path("/{id}")
	@GET
	@Produces("application/json")
	public Response getSignup(@PathParam("id") final String signupId) throws JsonGenerationException, JsonMappingException, IOException {
		CourseSignup signup = courseService.getCourseSignup(signupId);
		if (signup == null) {
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
		}
		return Response.ok(objectMapper.writeValueAsString(signup)).build();
	}
	
	@Path("/{id}")
	@POST // PUT Doesn't seem to make it through the portal :-(
	public void updateSignup(@PathParam("id") final String signupId, @FormParam("status") final Status status){
		courseService.setSignupStatus(signupId, status);
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
	public StreamingOutput getCourseSignups(@PathParam("id") final String courseId, @QueryParam("status") final Status status) {
		// All the pending 
		return new StreamingOutput() {
			
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				Set statuses = null;
				if (null != status) {
					statuses = Collections.singleton(status);
				}
				List<CourseSignup> signups = courseService.getCourseSignups(courseId, statuses);
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}
		};
	}
	
	@Path("/component/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getComponentSignups(@PathParam("id") final String componentId, @QueryParam("status") final Status status) {
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				Set statuses = null;
				if (null != status) {
					statuses = Collections.singleton(status);
				}
				List<CourseSignup> signups = courseService.getComponentSignups(componentId, statuses);
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}
			
		};
	}
	
	@Path("/component/{id}.csv")
	@GET
	@Produces("text/comma-separated-values")
	public StreamingOutput getComponentSignupsCSV(@PathParam("id") final String componentId, @Context final HttpServletResponse response) {
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				List<CourseSignup> signups = courseService.getComponentSignups(componentId, null);
				response.addHeader("Content-disposition", "attachment; filename="+componentId+".csv"); // Force a download
				Writer writer = new OutputStreamWriter(output);
				CSVWriter csvWriter = new CSVWriter(writer);
				for(CourseSignup signup : signups) {
					Person user = signup.getUser();
					csvWriter.writeln(new String[]{user.getName(), user.getEmail(), signup.getStatus().toString()});
				}
				writer.flush();
			}
			
		};
	}
	
	@Path("/pending")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getPendingSignups() {
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				List<CourseSignup> signups = courseService.getApprovals();
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}
			
		}; 
	}
	
	@Path("/previous")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getPreviousSignups(@QueryParam("userid") final String userId,
											  @QueryParam("componentid") final String componentId,
											  @QueryParam("groupid") final String groupId) {
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				List<CourseSignup> signups = new ArrayList<CourseSignup>();
				for (CourseSignup signup : courseService.getUserComponentSignups(userId, null)) {
					if (signup.getGroup().getId().equals(groupId)) {
						signups.add(signup);
					}
				}
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}
			
		}; 
		
	}
}
