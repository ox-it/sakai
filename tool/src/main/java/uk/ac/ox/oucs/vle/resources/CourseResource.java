/*
 * #%L
 * Course Signup Webapp
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle.resources;

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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import uk.ac.ox.oucs.vle.*;
import uk.ac.ox.oucs.vle.CourseSignupService.Range;

//@Path("/course/")
@Path("course{cobomo:(/cobomo)?}")
public class CourseResource {

	private static final Log log = LogFactory.getLog(CourseResource.class);

	private CourseSignupService courseService;
	private SearchService searchService;
	private JsonFactory jsonFactory;
	private ObjectMapper objectMapper;

	public CourseResource(@Context ContextResolver<Object> resolver) {
		this.courseService = (CourseSignupService) resolver.getContext(CourseSignupService.class);
		searchService = (SearchService) resolver.getContext(SearchService.class);
		jsonFactory = new JsonFactory();
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationConfig.Feature.USE_STATIC_TYPING, true);
		objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
	}
	
	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public StreamingOutput getCourse(@PathParam("id") final String courseId, @QueryParam("range") final Range range) {
		
		final CourseGroup course = courseService.getCourseGroup(courseId, range);
		if (course == null) {
			throw new WebAppNotFoundException();
		}
		return new GroupStreamingOutput(course);
	} 
	
	@Path("/{id}")
	@GET
	// Commented because of a problem with MSIE Accept Headers (See WL-2563)
	// @Produces({MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML})
	public String getCoursePage(
			@PathParam("id") final String courseId, 
			@QueryParam("range") final Range range,
			@Context final HttpServletRequest request,
		    @Context final HttpServletResponse response) throws ServletException, IOException {
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/static/course.jsp");
		request.setAttribute("openCourse", courseId);
		dispatcher.forward(request, response);
		return "";
	} 

	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public StreamingOutput getCourses(@QueryParam("range") final Range range) {
		boolean externalUser = false;
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			externalUser = true;
		}
		final Map<String, String> departments = courseService.getDepartments();
		final List<CourseGroup> groups = courseService.search("", range, externalUser);
		if (groups == null) {
			throw new WebAppNotFoundException();
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
		if (isProvider(deptId)) { 
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
			throw new WebAppForbiddenException();
		}
		List <CourseGroup> groups = courseService.getAdministering();
		// TODO Just return the coursegroups (no nested objects).
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
		
	}
	
	@Path("/lecture")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLectureCourse() throws JsonGenerationException, JsonMappingException, IOException {
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			throw new WebAppForbiddenException();
		}
		Collection <CourseGroup> groups = courseService.getLecturing();
		// TODO Just return the coursegroups (no nested objects).
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
		
	}
	
	@Path("/search")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCourses(@QueryParam("terms") String terms) throws JsonGenerationException, JsonMappingException, IOException {
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			throw new WebAppForbiddenException();
		}
		if (terms == null) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("No query parameter terms supplied").build());
		}
		List<CourseGroup> groups = courseService.search(terms, Range.UPCOMING, false);
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
	}
	
	@Path("/solr/{command}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput setSolr(@PathParam("command") final String command, @Context UriInfo uriInfo) 
			throws JsonGenerationException, JsonMappingException, IOException {
		
		if (!"select".equals(command)) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("command not supported").build());
		}
		String query = uriInfo.getRequestUri().getRawQuery();
		if (null == query) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("invalid query").build());
		}
	
		ResultsWrapper results;
		try {
			results = searchService.select(query);
			return new StringStreamingOutput(results);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.serverError().entity("Search not currently available.").build());
		}
	}
	
	@Path("/calendar")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCourseCalendar() throws JsonGenerationException, JsonMappingException, IOException {
		boolean externalUser = false;
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			externalUser = true;
		}
		List <CourseGroup> groups = courseService.getCourseCalendar(externalUser, null);
		// TODO Just return the coursegroups (no nested objects).
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
		
	}
	
	@Path("/nodates")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCourseNoDates() throws JsonGenerationException, JsonMappingException, IOException {
		boolean externalUser = false;
		if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
			externalUser = true;
		}
		List <CourseGroup> groups = courseService.getCourseNoDates(externalUser, null);
		// TODO Just return the coursegroups (no nested objects).
		return Response.ok(objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseGroup.class)).writeValueAsString(groups)).build();
		
	}

	@Path("/hide")
	@POST
	public Response hide(@FormParam("courseId")String courseId, @FormParam("hideCourse")Boolean hideCourse) {
		courseService.setHideCourse(courseId, hideCourse);
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
			
			if (null != component.getOpens()) {
				// Check if component is the earliest one opening in the future.
				//boolean isGoingToOpen = component.getOpens().after(now) && component.getOpens().before(nextOpen);
				if (component.getOpens().after(now) && component.getOpens().before(nextOpen)) {
					nextOpen = component.getOpens();
				}
				
				if (null != component.getCloses()) {
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
	
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	private boolean isProvider(String code) {
		return courseService.isDepartmentCode(code);
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
				gen.writeObjectField("id", courseGroup.getCourseId());
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
				
				gen.writeObjectField("id", courseGroup.getCourseId());
				gen.writeObjectField("description", courseGroup.getDescription());
				gen.writeObjectField("title", courseGroup.getTitle());
				
				gen.writeArrayFieldStart("components");
				for (CourseComponent component : courseGroup.getComponents()) {
					gen.writeStartObject();
					gen.writeObjectField("id", component.getPresentationId());
					gen.writeObjectField("title", component.getTitle());
					gen.writeObjectField("location", component.getLocation());
					gen.writeObjectField("slot", component.getTeachingDetails());
					gen.writeObjectField("size", component.getSize());
					if (null != component.getOpens()) {
						gen.writeObjectField("opens", component.getOpens().getTime());
					}
					if (null != component.getCloses()) {
						gen.writeObjectField("closes", component.getCloses().getTime());
					}
					if (null != component.getBaseDate()) {
						gen.writeObjectField("baseDate", component.getBaseDate().getTime());
					}
					gen.writeObjectField("opensText", component.getOpensText());
					gen.writeObjectField("sessions", component.getSessions());
					gen.writeObjectField("when", component.getWhen());
					gen.writeObjectField("bookable", component.getBookable());
					gen.writeObjectField("places", component.getPlaces());
					gen.writeObjectField("created", component.getCreated().getTime());
					gen.writeObjectField("source", component.getSource());
					gen.writeObjectField("componentSet", component.getComponentSet());
					gen.writeObjectField("source", component.getSource());
					if (null != component.getPresenter()) {
						gen.writeObjectFieldStart("presenter");
						gen.writeObjectField("name", component.getPresenter().getName());
						gen.writeObjectField("email", component.getPresenter().getEmail());
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
				for (CourseCategory category : courseGroup.getCategories(CourseGroup.CategoryType.RDF)) {
					gen.writeObject(category.getName());
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("categories_jacs");
				for (CourseCategory category : courseGroup.getCategories(CourseGroup.CategoryType.JACS)) {
					gen.writeObject(category.getName());
				}
				gen.writeEndArray();
				
				gen.writeArrayFieldStart("categories_rm");
				for (CourseCategory category : courseGroup.getCategories(CourseGroup.CategoryType.RM)) {
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
			gen.writeObjectField("id", course.getCourseId());
			gen.writeObjectField("description", course.getDescription());
			gen.writeObjectField("title", course.getTitle());
			gen.writeObjectField("supervisorApproval", course.getSupervisorApproval());
			gen.writeObjectField("administratorApproval", course.getAdministratorApproval());
			gen.writeObjectField("visibility", course.getVisibility());
			//gen.writeObjectField("homeApproval", courseGroup.getHomeApproval());
			gen.writeObjectField("isAdmin", course.getIsAdmin());
			gen.writeObjectField("isSuperuser", course.getIsSuperuser());
			gen.writeObjectField("department", course.getDepartment());
			gen.writeObjectField("departmentCode", course.getDepartmentCode());
			gen.writeObjectField("subUnit", course.getSubUnit());
			gen.writeObjectField("subUnitCode", course.getSubUnitCode());
			gen.writeObjectField("prerequisite", course.getPrerequisite());
			gen.writeObjectField("regulations", course.getRegulations());
			gen.writeObjectField("source", course.getSource());
				
			gen.writeArrayFieldStart("components");
			for (CourseComponent component : course.getComponents()) {
				gen.writeStartObject();
				gen.writeObjectField("id", component.getPresentationId());
				gen.writeObjectField("location", component.getLocation());
				gen.writeObjectField("slot", component.getTeachingDetails());
				gen.writeObjectField("size", component.getSize());
				if (null != component.getOpens()) {
					gen.writeObjectField("opens", component.getOpens().getTime());
				}
				gen.writeObjectField("opensText", component.getOpensText());
				if (null != component.getCloses()) {
					gen.writeObjectField("closes", component.getCloses().getTime());
				}
				gen.writeObjectField("closesText", component.getClosesText());
				if (null != component.getBaseDate()) {
					gen.writeObjectField("baseDate", component.getBaseDate().getTime());
				}
				gen.writeObjectField("title", component.getTitle());
				gen.writeObjectField("when", component.getWhen());
				gen.writeObjectField("bookable", component.getBookable());
				if (null != component.getStarts()) {
					gen.writeObjectField("starts", component.getStarts().getTime());
				}
				gen.writeObjectField("startsText", component.getStartsText());
				if (null != component.getEnds()) {
					gen.writeObjectField("ends", component.getEnds().getTime());
				}
				gen.writeObjectField("endsText", component.getEndsText());
				gen.writeObjectField("places", component.getPlaces());
				gen.writeObjectField("componentSet", component.getComponentSet());
				gen.writeObjectField("sessionCount", component.getSessions());
				
				gen.writeObjectField("applyTo", component.getApplyTo());
				gen.writeObjectField("memberApplyTo", component.getMemberApplyTo());
				gen.writeObjectField("teachingDetails", component.getTeachingDetails());
				
				gen.writeObjectField("attendanceMode", component.getAttendanceMode());
				gen.writeObjectField("attendanceModeText", component.getAttendanceModeText());
				gen.writeObjectField("attendancePattern", component.getAttendancePattern());
				gen.writeObjectField("attendancePatternText", component.getAttendancePatternText());
				
				if (null != component.getPresenter()) {
					gen.writeObjectFieldStart("presenter");
					gen.writeObjectField("name", component.getPresenter().getName());
					gen.writeObjectField("email", component.getPresenter().getEmail());
					//gen.writeObjectField("units", component.getPresenter().getUnits());
					gen.writeEndObject();
				}
				gen.writeArrayFieldStart("sessions");
				for (CourseComponentSession session : component.getComponentSessions()) {
					gen.writeStartObject();
					gen.writeObjectField("sessionId", session.getSessionId());
					gen.writeObjectField("sessionStart", session.getSessionStart().getTime());
					gen.writeObjectField("sessionStartText", session.getSessionStartText());
					gen.writeObjectField("sessionEnd", session.getSessionEnd().getTime());
					gen.writeObjectField("sessionEndText", session.getSessionEndText());
					gen.writeObjectField("location", session.getLocation());
					gen.writeEndObject();
				}
				gen.writeEndArray();
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
			for (CourseCategory category : course.getCategories(CourseGroup.CategoryType.RDF)) {
				gen.writeObject(category.getName());
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("categories_jacs");
			for (CourseCategory category : course.getCategories(CourseGroup.CategoryType.JACS)) {
				gen.writeObject(category.getName());
			}
			gen.writeEndArray();
				
			gen.writeArrayFieldStart("categories_rm");
			for (CourseCategory category : course.getCategories(CourseGroup.CategoryType.RM)) {
				gen.writeObject(category.getName());
			}
			gen.writeEndArray();
				
			gen.writeEndObject();
			gen.close();
		}
	}

	/**
	 * This class is so that we can close the results wrapper after we've streamed the content back to the client.
	 */
	private class StringStreamingOutput implements StreamingOutput {
		
		private final ResultsWrapper results;
	
		private StringStreamingOutput(ResultsWrapper results) {
			this.results = results;
		}
	
		public void write(OutputStream out) {
			try {
				IOUtils.copy(results.getInputStream(), out);
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
				throw new WebApplicationException(Response.status(Status.SERVICE_UNAVAILABLE)
						.entity("Service Unavailable, IO Exception with Search Engine").build());
			} finally {
				results.disconnect();
				
			}
		}
	}
}
