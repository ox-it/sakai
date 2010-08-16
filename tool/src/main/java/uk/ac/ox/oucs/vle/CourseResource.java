package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
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

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.type.TypeFactory;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;

@Path("/course/")
public class CourseResource {

	private CourseSignupService courseService;
	private JsonFactory jsonFactory;
	private ObjectMapper objectMapper;

	public CourseResource(@Context ContextResolver<Object> resolver) {
		this.courseService = (CourseSignupService) resolver.getContext(CourseSignupService.class);
		jsonFactory = new JsonFactory();
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationConfig.Feature.USE_STATIC_TYPING, true);
		objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
	}

	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getCourse(@PathParam("id") final String courseId, @QueryParam("range") final Range range) {
		final CourseGroup course = courseService.getCourseGroup(courseId, range);
		if (course == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				objectMapper.writeValue(output, course);
			}
		};
	}

	/**
	 * This gets all the courses for a department that have upcoming
	 * parts.
	 * @param deptId The department to load the courses for.
	 * @return An array of jsTree nodes.
	 */
	@Path("/dept/{deptId}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public StreamingOutput getCoursesUpcoming(@PathParam("deptId") final String deptId, @QueryParam("components") final Range range) {
		
		final List<CourseGroup> courses = courseService.getCourseGroups(deptId, range);
		return new GroupsStreamingOutput(courses, deptId, range.name());
	}
	
	@Path("/admin")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAdminCourse() throws JsonGenerationException, JsonMappingException, IOException {
		List <CourseGroup> groups = courseService.getAdministering();
		// TODO Just return the coursegroups (no nested objects).
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
		
	}
	
	@Path("/search")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCourses(@QueryParam("terms") String terms) throws JsonGenerationException, JsonMappingException, IOException {
		if (terms == null) {
			throw new WebApplicationException();
		}
		List<CourseGroup> groups = courseService.search(terms);
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
	}

	/**
	 * Formats a duration sensibly.
	 * @param remaining Time remaining in milliseconds.
	 * @return a String roughly representing the durnation.
	 */
	private String formatDuration(long remaining) {
		if (remaining < 1000) {
			return "< 1 second";
		} else if (remaining < 60000) {
			return remaining / 1000 + " seconds";
		} else if (remaining < 3600000) {
			return remaining / 60000 + " minutes";
		} else if (remaining < 86400000) {
			return remaining / 3600000 + " hours";
		} else {
			return remaining / 86400000 + " days";
		}
	}

	private String summary(Date now, CourseGroup courseGroup) {
		// Calculate the summary based on the available components.
		if (courseGroup.getComponents().isEmpty()) {
			return "none available";
		}
		Date nextOpen = new Date(Long.MAX_VALUE);
		Date willClose = new Date(0);
		boolean isOneOpen = false;
		boolean isOneBookable = false;
		boolean areSomePlaces = false;
		for (CourseComponent component: courseGroup.getComponents()) {
			if (!isOneBookable) {
				isOneBookable = component.getBookable();
			}
			// Check if component is the earliest one opening in the future.
			if (component.getOpens().after(now) && component.getOpens().before(nextOpen)) {
				nextOpen = component.getOpens();
			}
			// Check if the component is open and is open for the longest.
			if (component.getOpens().before(now) && component.getCloses().after(willClose)) {
				willClose = component.getCloses();
			}
			if (!isOneOpen && component.getOpens().before(now) && component.getCloses().after(now)) {
				isOneOpen = true;
				if (component.getPlaces() > 0) {
					areSomePlaces = true;
				}
			}
		}
		String detail = null;
		if (!isOneBookable) {
			return null; // No signup available.
		}
		if (isOneOpen) {
			if (areSomePlaces) {
				long remaining = willClose.getTime() - now.getTime();
				detail = "close in "+ formatDuration(remaining);
			} else {
				detail = "full";
			}
		} else {
			if (nextOpen.getTime() == Long.MAX_VALUE) {
				return null; // Didn't find one open
			}
			long until = nextOpen.getTime() - now.getTime();
			detail = "open in "+ formatDuration(until);
		}
		return detail;
	}

	private class GroupsStreamingOutput implements StreamingOutput {
		private final List<CourseGroup> courses;
		private final String deptId;
		private final String range;
	
		private GroupsStreamingOutput(List<CourseGroup> courses, String deptId, String range) {
			this.courses = courses;
			this.deptId = deptId;
			this.range = range;
		}
	
		public void write(OutputStream out) throws IOException {
			
			Date now = courseService.getNow();
			
			JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
			gen.writeStartObject();
			gen.writeObjectField("dept", deptId);
			gen.writeObjectField("range", range);
			gen.writeArrayFieldStart("tree");
			for (CourseGroup courseGroup : courses) {
				gen.writeStartObject();
				
				gen.writeObjectFieldStart("attr");
				gen.writeObjectField("id", courseGroup.getId());
				gen.writeEndObject();
				
				String detail = summary(now, courseGroup);
				gen.writeObjectField("data", courseGroup.getTitle() + 
						(detail == null?"":(" ("+detail+")"))
				);
				
				gen.writeEndObject();
			}
			gen.writeEndArray();
			gen.writeEndObject();
			gen.close();
		}
	}
}
