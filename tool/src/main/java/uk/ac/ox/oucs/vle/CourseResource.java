package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

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

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;
import org.sakaiproject.user.cover.UserDirectoryService;

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
		return new GroupStreamingOutput(course);
		/*
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				objectMapper.writeValue(output, course);
			}
		};
		
		*/
	} 

	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getCourses(@QueryParam("range") final Range range) {
		boolean externalUser = false;
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			externalUser = true;
		}
		final Map<String, String> departments = courseService.getDepartments();
		final List<CourseGroup> groups = courseService.search("", range, externalUser);
		if (groups == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return new AllGroupsStreamingOutput(departments, groups);
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
		boolean externalUser = false;
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			externalUser = true;
		}
		if (deptId.length() == 4) { 
			if (range.equals(Range.PREVIOUS)) {
				List<CourseGroup> courses = courseService.getCourseGroupsByDept(deptId, range, externalUser);
				return new GroupsStreamingOutput(Collections.<SubUnit>emptyList(), courses, deptId, range.name(), false);
			} else {
				List<SubUnit> subUnits = courseService.getSubUnitsByDept(deptId);
				List<CourseGroup> courses = courseService.getCourseGroupsByDept(deptId, range, externalUser);
				List<CourseGroup> previous = courseService.getCourseGroupsByDept(deptId, Range.PREVIOUS, externalUser);
				return new GroupsStreamingOutput(subUnits, courses, deptId, range.name(), !previous.isEmpty());
			}
		} else {
			if (range.equals(Range.PREVIOUS)) {
				List<CourseGroup> courses = courseService.getCourseGroupsBySubUnit(deptId, range, externalUser);
				return new GroupsStreamingOutput(Collections.<SubUnit>emptyList(), courses, deptId, range.name(), false);
			} else {
				List<CourseGroup> courses = courseService.getCourseGroupsBySubUnit(deptId, range, externalUser);
				List<CourseGroup> previous = courseService.getCourseGroupsBySubUnit(deptId, Range.PREVIOUS, externalUser);
				return new GroupsStreamingOutput(Collections.<SubUnit>emptyList(), courses, deptId, range.name(), !previous.isEmpty());
			}
		}
	}

	@Path("/admin")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAdminCourse() throws JsonGenerationException, JsonMappingException, IOException {
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		List <CourseGroup> groups = courseService.getAdministering();
		// TODO Just return the coursegroups (no nested objects).
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
		
	}
	
	@Path("/search")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCourses(@QueryParam("terms") String terms) throws JsonGenerationException, JsonMappingException, IOException {
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		if (terms == null) {
			throw new WebApplicationException();
		}
		List<CourseGroup> groups = courseService.search(terms, Range.UPCOMING, false);
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
	}
	
	@Path("/url/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getCourseURL(@PathParam("id") final String courseId) 
	throws JsonGenerationException, JsonMappingException, IOException {
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		final String url = courseService.getDirectUrl(courseId);
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				objectMapper.typedWriter(TypeFactory.fromClass(String.class)).writeValue(output, url);
			}
		};
	}
	
	@Path("/hide")
	@POST
	public Response hide(@FormParam("courseId")String courseId, @FormParam("hideCourse")String hideCourse) {
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		courseService.setHideCourse(courseId, Boolean.parseBoolean(hideCourse));
		return Response.ok().build();
	}

	/**
	 * Formats a duration sensibly.
	 * @param remaining Time remaining in milliseconds.
	 * @return a String roughly representing the duration.
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

	private CourseSummary summary(Date now, CourseGroup courseGroup) {
		// Calculate the summary based on the available components.	
		if (courseGroup.getComponents().isEmpty()) {
			return new CourseSummary("none available", Collections.singletonList(CourseState.UNKNOWN));
		}
		
		Integer recentDays = courseService.getRecentDays();
		Collection<CourseState> states = new ArrayList<CourseState>();
		//CourseState state = CourseState.UNKNOWN;
		Date nextOpen = new Date(Long.MAX_VALUE);
		Date willClose = new Date(0);
		boolean isOneOpen = false;
		boolean isOneBookable = false;
		boolean areSomePlaces = false;
		boolean isNew = false;
		
		for (CourseComponent component: courseGroup.getComponents()) {
			if (!isOneBookable) {
				isOneBookable = component.getBookable();
			}
			// Check if component is the earliest one opening in the future.
			boolean isGoingToOpen = component.getOpens().after(now) && component.getOpens().before(nextOpen);
			if (isGoingToOpen) {
				nextOpen = component.getOpens();
			}
			// Check if the component is open and is open for the longest.
			if (component.getOpens().before(now) && component.getCloses().after(willClose)) {
				willClose = component.getCloses();
			}
			boolean isOpen = component.getOpens().before(now) && component.getCloses().after(now);
			if (!isOneOpen && isOpen) {
				isOneOpen = true;
			}
			if (isOpen) {
				if (component.getPlaces() > 0) {
					areSomePlaces = true;
				}
			}
			Calendar recent = new GregorianCalendar();
			recent.setTime(component.getCreated());
			recent.add(Calendar.DATE, (recentDays));
			
			if (now.before(recent.getTime())) {
				isNew = true;
			}
		}
		String detail = null;
		if (isNew) {
			states.add(CourseState.NEW);
		}
		//String newCourse = isNew ? "*NEW*" : "";
		if (!isOneBookable) {
			states.add(CourseState.UNBOOKABLE);
			return new CourseSummary(null, states);
		}
	
		if (isOneOpen) {
			if (areSomePlaces) {
				long remaining = willClose.getTime() - now.getTime();
				detail = "close in "+ formatDuration(remaining);
				states.add(CourseState.OPEN);
			} else {
				detail = "full";
				states.add(CourseState.FULL);
			}
		} else {
			if (nextOpen.getTime() == Long.MAX_VALUE) {
				states.add(CourseState.UNBOOKABLE);
			} else {
				long until = nextOpen.getTime() - now.getTime();
				detail = "open in "+ formatDuration(until);
			}
		}
		return new CourseSummary(detail, states);
	}
	
	

	// Maybe I should have just implemented a tuple.
	static enum CourseState {OPEN, FULL, UNBOOKABLE, UNKNOWN, NEW}

	class CourseSummary {
		
		private String detail;
		private Collection<CourseState> states;
		
		CourseSummary(String detail, Collection<CourseState> states) {
			this.detail = detail;
			this.states = states;
		}
		
		String getDetail() {
			return this.detail;
		}
		
		String getCourseState() {
			StringBuffer sb = new StringBuffer();
			for (CourseState state : states) {
				sb.append(state.toString().toLowerCase());
				sb.append(" ");
			}
			return sb.toString();
		}
	}

	private class GroupsStreamingOutput implements StreamingOutput {
		private final List<SubUnit> subUnits;
		private final List<CourseGroup> courses;
		private final String deptId;
		private final String range;
		private final boolean previous;
	
		private GroupsStreamingOutput(List<SubUnit> subUnits, List<CourseGroup> courses, String deptId, String range, boolean previous) {
			this.subUnits = subUnits;
			this.courses = courses;
			this.deptId = deptId;
			this.range = range;
			this.previous = previous;
		}
	
		public void write(OutputStream out) throws IOException {
			
			Date now = courseService.getNow();
			
			JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
			gen.writeStartObject();
			gen.writeObjectField("dept", deptId);
			gen.writeObjectField("range", range);
			gen.writeArrayFieldStart("tree");
			for (SubUnit subUnit : subUnits) {
				gen.writeStartObject();
				gen.writeObjectFieldStart("attr");
				gen.writeStringField("id", subUnit.getCode());
				gen.writeEndObject();
				gen.writeStringField("data", (String)subUnit.getName());
				gen.writeStringField("state", "closed");
				gen.writeArrayFieldStart("children");
				gen.writeEndArray();
				gen.writeEndObject();
			}
			
			for (CourseGroup courseGroup : courses) {
				gen.writeStartObject();
				
				CourseSummary state = summary(now, courseGroup);
				gen.writeObjectFieldStart("attr");
				gen.writeObjectField("id", courseGroup.getId());
				gen.writeObjectField("class", state.getCourseState().toString().toLowerCase());
				gen.writeEndObject();
				
				gen.writeObjectField("data", 
						courseGroup.getTitle() + 
						(state.getDetail() == null ? "" : (" ("+state.getDetail()+")")) 
						//(state.getNew() == null ? "" : " " + (state.getNew()))
				);
				
				gen.writeEndObject();
			}
			
			if (previous) {
				gen.writeStartObject();
				gen.writeObjectFieldStart("attr");
				gen.writeStringField("id", deptId+"-PREVIOUS");
				gen.writeEndObject();
				gen.writeStringField("data", "Previous");
				gen.writeStringField("state", "closed");
				//gen.writeArrayFieldStart("children");
				//gen.writeEndArray();
				gen.writeEndObject();
			}
			gen.writeEndArray();
			gen.writeEndObject();
			gen.close();
		}
	}
	
	private class AllGroupsStreamingOutput implements StreamingOutput {
		private final Map<String, String> departments;
		private final List<CourseGroup> courses;
	
		private AllGroupsStreamingOutput(Map<String, String> departments, List<CourseGroup> courses) {
			this.departments = departments;
			this.courses = courses;
		}
	
		public void write(OutputStream out) throws IOException {
			
			JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
			gen.writeStartArray();
			
			for (CourseGroup courseGroup : courses) {
				gen.writeStartObject();
				
				gen.writeObjectField("id", courseGroup.getId());
				gen.writeObjectField("description", courseGroup.getDescription());
				gen.writeObjectField("title", courseGroup.getTitle());
				//gen.writeObjectField("supervisorApproval", courseGroup.getSupervisorApproval());
				//gen.writeObjectField("administratorApproval", courseGroup.getAdministratorApproval());
				//gen.writeObjectField("publicView", courseGroup.getPublicView());
				//gen.writeObjectField("homeApproval", courseGroup.getHomeApproval());
				//gen.writeObjectField("isAdmin", courseGroup.getIsAdmin());
				
				gen.writeArrayFieldStart("components");
				for (CourseComponent component : courseGroup.getComponents()) {
					gen.writeStartObject();
					gen.writeObjectField("id", component.getId());
					gen.writeObjectField("location", component.getLocation());
					gen.writeObjectField("slot", component.getSlot());
					gen.writeObjectField("size", component.getSize());
					gen.writeObjectField("subject", component.getSubject());
					gen.writeObjectField("opens", component.getOpens().getTime());
					gen.writeObjectField("closes", component.getCloses().getTime());
					gen.writeObjectField("title", component.getTitle());
					gen.writeObjectField("sessions", component.getSessions());
					gen.writeObjectField("when", component.getWhen());
					gen.writeObjectField("bookable", component.getBookable());
					gen.writeObjectField("places", component.getPlaces());
					gen.writeObjectField("created", component.getCreated().getTime());
					gen.writeObjectField("componentSet", component.getComponentSet());
					if (null != component.getPresenter()) {
						gen.writeObjectFieldStart("presenter");
						gen.writeObjectField("name", component.getPresenter().getName());
						gen.writeObjectField("email", component.getPresenter().getEmail());
						//gen.writeObjectField("units", component.getPresenter().getUnits());
						gen.writeEndObject();
					}
					gen.writeEndObject();
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("administrators");
				for (Person administrator : courseGroup.getAdministrators()) {
					gen.writeStartObject();
					gen.writeObjectField("id", administrator.getId());
					gen.writeObjectField("name", administrator.getName());
					gen.writeObjectField("type", administrator.getType());
					gen.writeObjectField("email", administrator.getEmail());
					//gen.writeObjectField("units", administrator.getUnits());
					gen.writeEndObject();
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("superusers");
				for (Person superuser : courseGroup.getSuperusers()) {
					gen.writeStartObject();
					gen.writeObjectField("id", superuser.getId());
					gen.writeObjectField("name", superuser.getName());
					gen.writeObjectField("type", superuser.getType());
					gen.writeObjectField("email", superuser.getEmail());
					//gen.writeObjectField("units", superuser.getUnits());
					gen.writeEndObject();
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("department");
				gen.writeObject(departments.get(courseGroup.getDepartmentCode()));
				for (String code : courseGroup.getOtherDepartments()) {
					gen.writeObject(departments.get(code));
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("categories_rdf");
				for (CourseCategory category : courseGroup.getCategories(CourseGroup.Category_Type.RDF)) {
					gen.writeObject(category.getName());
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("categories_jacs");
				for (CourseCategory category : courseGroup.getCategories(CourseGroup.Category_Type.JACS)) {
					gen.writeObject(category.getName());
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("categories_rm");
				for (CourseCategory category : courseGroup.getCategories(CourseGroup.Category_Type.RM)) {
					gen.writeObject(category.getName());
				}
				gen.writeEndArray();
				
				gen.writeEndObject();
			}
			gen.writeEndArray();
			gen.close();
		}
	}
	
	private class GroupStreamingOutput implements StreamingOutput {
		
		private final CourseGroup course;
	
		private GroupStreamingOutput(CourseGroup course) {
			
			this.course = course;
		}
	
		public void write(OutputStream out) throws IOException {
			
			JsonGenerator gen = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
			
			gen.writeStartObject();
			gen.writeObjectField("id", course.getId());
			gen.writeObjectField("description", course.getDescription());
			gen.writeObjectField("title", course.getTitle());
			gen.writeObjectField("supervisorApproval", course.getSupervisorApproval());
			gen.writeObjectField("administratorApproval", course.getAdministratorApproval());
			gen.writeObjectField("publicView", course.getPublicView());
			//gen.writeObjectField("homeApproval", courseGroup.getHomeApproval());
			gen.writeObjectField("isAdmin", course.getIsAdmin());
			gen.writeObjectField("isSuperuser", course.getIsSuperuser());
			gen.writeObjectField("department", course.getDepartment());
			gen.writeObjectField("departmentCode", course.getDepartmentCode());
			gen.writeObjectField("subUnit", course.getSubUnit());
			gen.writeObjectField("subUnitCode", course.getSubUnitCode());
				
			gen.writeArrayFieldStart("components");
			for (CourseComponent component : course.getComponents()) {
				gen.writeStartObject();
				gen.writeObjectField("id", component.getId());
				gen.writeObjectField("location", component.getLocation());
				gen.writeObjectField("slot", component.getSlot());
				gen.writeObjectField("size", component.getSize());
				gen.writeObjectField("subject", component.getSubject());
				gen.writeObjectField("opens", component.getOpens().getTime());
				gen.writeObjectField("closes", component.getCloses().getTime());
				gen.writeObjectField("title", component.getTitle());
				gen.writeObjectField("sessions", component.getSessions());
				gen.writeObjectField("when", component.getWhen());
				gen.writeObjectField("bookable", component.getBookable());
				if (null != component.getStarts()) {
					gen.writeObjectField("starts", component.getStarts().getTime());
				}
				if (null != component.getEnds()) {
					gen.writeObjectField("ends", component.getEnds().getTime());
				}
				gen.writeObjectField("places", component.getPlaces());
				gen.writeObjectField("componentSet", component.getComponentSet());
				if (null != component.getPresenter()) {
					gen.writeObjectFieldStart("presenter");
					gen.writeObjectField("name", component.getPresenter().getName());
					gen.writeObjectField("email", component.getPresenter().getEmail());
					//gen.writeObjectField("units", component.getPresenter().getUnits());
					gen.writeEndObject();
				}
				gen.writeEndObject();
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("administrators");
			for (Person administrator : course.getAdministrators()) {
				gen.writeStartObject();
				gen.writeObjectField("id", administrator.getId());
				gen.writeObjectField("name", administrator.getName());
				gen.writeObjectField("type", administrator.getType());
				gen.writeObjectField("email", administrator.getEmail());
				gen.writeObjectField("firstName", administrator.getFirstName());
				gen.writeObjectField("lastName", administrator.getLastName());
				gen.writeObjectField("departmentName", administrator.getDepartmentName());
				//gen.writeObjectField("units", administrator.getUnits());
				gen.writeObjectField("webauthId", administrator.getWebauthId());
				gen.writeEndObject();
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("superusers");
			for (Person superuser : course.getSuperusers()) {
				gen.writeStartObject();
				gen.writeObjectField("id", superuser.getId());
				gen.writeObjectField("name", superuser.getName());
				gen.writeObjectField("type", superuser.getType());
				gen.writeObjectField("email", superuser.getEmail());
				gen.writeObjectField("firstName", superuser.getFirstName());
				gen.writeObjectField("lastName", superuser.getLastName());
				gen.writeObjectField("departmentName", superuser.getDepartmentName());
				//gen.writeObjectField("units", superuser.getUnits());
				gen.writeObjectField("webauthId", superuser.getWebauthId());
				gen.writeEndObject();
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("otherDepartments");
			for (String code : course.getOtherDepartments()) {
				gen.writeObject(code);
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("categories_rdf");
			for (CourseCategory category : course.getCategories(CourseGroup.Category_Type.RDF)) {
				gen.writeObject(category.getName());
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("categories_jacs");
			for (CourseCategory category : course.getCategories(CourseGroup.Category_Type.JACS)) {
				gen.writeObject(category.getName());
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("categories_rm");
			for (CourseCategory category : course.getCategories(CourseGroup.Category_Type.RM)) {
				gen.writeObject(category.getName());
			}
			gen.writeEndArray();
				
			gen.writeEndObject();
			gen.close();
		}
	}
}
