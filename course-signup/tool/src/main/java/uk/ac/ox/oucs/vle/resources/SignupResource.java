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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.glassfish.jersey.server.mvc.Viewable;
import org.sakaiproject.component.api.ServerConfigurationService;
import uk.ac.ox.oucs.vle.*;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("signup{cobomo:(/cobomo)?}")
public class SignupResource {

	private static final Log log = LogFactory.getLog(SignupResource.class);

	@Inject
	private CourseSignupService courseService;
	@Inject
	private StatusProgression statusProgression;
	@Inject
	private ServerConfigurationService serverConfigurationService;
	@Inject
	private SakaiProxy proxy;
	@Inject
	private ObjectMapper objectMapper;

	@Path("/my")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getMySignups() {
		checkAuthenticated();
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
		checkAuthenticated();
		final List<CourseSignup> signups = courseService.getMySignups(null);
		final List<CourseSignup> courseSignups = new ArrayList<CourseSignup>();
		for(CourseSignup signup: signups) {
			if (courseId.equals(signup.getGroup().getCourseId())) {
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

	/**
	 * Make a new signup for the current user.
	 *
	 * @param courseId
	 * 		the courseId of the signup
	 * @param components
	 * 		the components to sign up to
	 * @param email
	 * 		the email of the supervisor
	 * @param message
	 * 		the reason for the signup
	 * @return CourseSignup
	 * 		the coursesignup object created
	 */
	@Path("/my/new")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response signup(@FormParam("courseId") String courseId,
	                       @FormParam("components")Set<String> components,
	                       @FormParam("email")String email,
	                       @FormParam("message")String message) {
		checkAuthenticated();
		try {
			CourseSignup entity = courseService.signup(courseId, components, email, message);
			ResponseBuilder builder = Response.status(Response.Status.CREATED);
			builder.entity(entity);
			return builder.build();
		} catch (IllegalStateException e) {
			throw new WebAppBadRequestException(new FailureMessage(e.getMessage()));
		} catch (IllegalArgumentException e) {
			throw new WebAppBadRequestException(new FailureMessage(e.getMessage()));
		}
	}

	/**
	 * Create a signup specifying the user.
	 *
	 * @param userId The ID of the user to be signed up. If <code>null</code> the we use the email address to lookup user.
	 *               If the string "newUser" is supplied we attempt to create a new user anyway (deprecated).
	 * @param userName The name of the user if we are creating a new user.
	 * @param userEmail The email address of the user.
	 * @param courseId The course ID to sign up to. Cannot be <code>null</code>.
	 * @param components The components IDs to sign up to. Cannot be <code>null</code>.
	 * @param supervisorId The ID of the supervisor to link the signups to. Can be <code>null</code>.
	 * @return The created CourseSignup.
	 * @see CourseSignupService#signup(String, String, String, String, java.util.Set, String)
	 */
	@Path("/new")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response signup( @FormParam("userId")String userId,
	                        @FormParam("userName")String userName,
	                        @FormParam("userEmail")String userEmail,
	                        @FormParam("courseId") String courseId,
	                        @FormParam("components")Set<String> components,
	                        @FormParam("supervisorId")String supervisorId) {
		checkAuthenticated();
		// Support old idea of a special ID for new users.
		// When the frontend is refactored this can be removed.
		if ("newUser".equals(userId)) {
			userId = null;
		}
		CourseSignup signup = courseService.signup(userId, userName, userEmail, courseId, components, supervisorId);
		return Response.status(Response.Status.CREATED).entity(signup).build();
	}

	@Path("/supervisor")
	@POST
	public Response signup(@FormParam("signupId")String signupId, @FormParam("supervisorId")String supervisorId) {
		checkAuthenticated();
		courseService.setSupervisor(signupId, supervisorId);
		return Response.ok().build();
	}

	@Path("/{id}")
	@GET
	@Produces("application/json")
	public Response getSignup(@PathParam("id") final String signupId) throws JsonGenerationException, JsonMappingException, IOException {
		checkAuthenticated();
		CourseSignup signup = courseService.getCourseSignup(signupId);
		if (signup == null) {
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
		}
		return Response.ok(objectMapper.writeValueAsString(signup)).build();
	}

	@Path("/{id}")
	@POST // PUT Doesn't seem to make it through the portal :-(
	public void updateSignup(@PathParam("id") final String signupId, @FormParam("status") final Status status){
		checkAuthenticated();
		courseService.setSignupStatus(signupId, status);
	}

	@Path("{id}/accept")
	@POST
	public Response accept(@PathParam("id") final String signupId) {
		checkAuthenticated();
		courseService.accept(signupId);
		return Response.ok().build();
	}

	@Path("{id}/reject")
	@POST
	public Response reject(@PathParam("id") final String signupId) {
		checkAuthenticated();
		courseService.reject(signupId);
		return Response.ok().build();
	}

	@Path("{id}/withdraw")
	@POST
	public Response withdraw(@PathParam("id") final String signupId) {
		checkAuthenticated();
		courseService.withdraw(signupId);
		return Response.ok().build();
	}

	@Path("{id}/waiting")
	@POST
	public Response waiting(@PathParam("id") final String signupId) {
		checkAuthenticated();
		courseService.waiting(signupId);
		return Response.ok().build();
	}

	@Path("{id}/approve")
	@POST
	public Response approve(@PathParam("id") final String signupId) {
		checkAuthenticated();
		courseService.approve(signupId);
		return Response.ok().build();
	}

	@Path("{id}/confirm")
	@POST
	public Response confirm(@PathParam("id") final String signupId) {
		checkAuthenticated();
		courseService.confirm(signupId);
		return Response.ok().build();
	}

	@Path("{id}/split")
	@POST
	public Response split(@PathParam("id") final String signupId,
	                      @FormParam("componentPresentationId") final Set<String> componentIds) {
		checkAuthenticated();
		try {
			String newSignupId = courseService.split(signupId, componentIds);
			return Response.status(Response.Status.CREATED).entity(newSignupId).build();
		} catch(IllegalArgumentException iae) {
			throw new CourseSignupException(iae.getMessage(), iae);
		}
	}

	@Path("/course/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getCourseSignups(@PathParam("id") final String courseId, @QueryParam("status") final Status status) {
		checkAuthenticated();
		// All the pending 
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				Set<Status> statuses = null;
				if (null != status) {
					statuses = Collections.singleton(status);
				}
				List<CourseSignup> signups = courseService.getCourseSignups(courseId, statuses);
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}
		};
	}

	@Path("/count/course/signups/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCountCourseSignup(@PathParam("id") final String courseId, @QueryParam("status") final Status status) throws JsonGenerationException, JsonMappingException, IOException {
		checkAuthenticated();
		// All the pending 
		Set<Status> statuses = null;
		if (null != status) {
			statuses = Collections.singleton(status);
		}
		Integer signups = courseService.getCountCourseSignups(courseId, statuses);
		return Response.ok(objectMapper.writeValueAsString(signups)).build();
	}

	@Path("/component/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getComponentSignups(@PathParam("id") final String componentId, @QueryParam("status") final Status status) {
		checkAuthenticated();
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				Set<Status> statuses = null;
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
		checkAuthenticated();
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				CourseComponent component = courseService.getCourseComponent(componentId);
				List<CourseSignup> signups = courseService.getComponentSignups(componentId, null);
				response.addHeader("Content-disposition", "attachment; filename="+getFileName(component)+".csv"); // Force a download
				Writer writer = new OutputStreamWriter(output);
				CSVWriter csvWriter = new CSVWriter(writer);
				csvWriter.writeln(new String[]{
						component.getTitle(), startsText(component)});
				csvWriter.writeln(new String[]{
						"Surname", "Forname", "Email", "SES Status",
						"Year of Study", "Degree Programme", "Affiliation"});
				for(CourseSignup signup : signups) {
					Person user = signup.getUser();
					if (null == user) {
						continue;
					}
					csvWriter.writeln(new String[]{
							user.getLastName(), user.getFirstName(), user.getEmail(), signup.getStatus().toString(),
							user.getYearOfStudy(), user.getDegreeProgram(), buildString(user.getUnits())});
				}
				writer.flush();
			}

		};
	}

	@Path("/component/{id}.pdf")
	@GET
	@Produces("application/pdf")
	public StreamingOutput getComponentSignupsPDF(@PathParam("id") final String componentId, @Context final HttpServletResponse response) {
		checkAuthenticated();

		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {


				CourseComponent courseComponent = courseService.getCourseComponent(componentId);
				Collection<CourseGroup> courseGroups = courseService.getCourseGroupsByComponent(componentId);

				List<CourseSignup> signups = courseService.getComponentSignups(
						componentId, Collections.singleton(Status.CONFIRMED));

				response.addHeader("Content-disposition", "attachment; filename="+componentId+".pdf"); // Force a download
				PDFWriter pdfWriter = new PDFWriter(output);
				pdfWriter.writeHead(courseGroups, courseComponent);
				pdfWriter.writeTableHead();

				if (!signups.isEmpty()) {

					List<Person> persons = new ArrayList<Person>();
					for (CourseSignup signup : signups) {
						if (null != signup.getUser()) {
							persons.add(signup.getUser());
						}
					}
					Collections.sort(persons, new Comparator<Person>() {
						public int compare(Person p1,Person p2) {
							return p1.getLastName().compareTo(p2.getLastName());
						}
					});

					pdfWriter.writeTableBody(persons);

				}
				pdfWriter.writeTableFoot();
				pdfWriter.close();
			}
		};
	}

	/**
	 * Exports all the signups for a year.
	 * @param status If specified only export signups with this status.
	 * @param year The year to export components from.
	 * @return A streaming out that writes out XML.
	 * @see #exportComponent(String, uk.ac.ox.oucs.vle.CourseSignupService.Status, int)
	 */
	@Path("/component/{year}.xml")
	@GET
	@Produces(MediaType.TEXT_XML)
	public StreamingOutput exportYear(@QueryParam("status") final Status status,
	                                  @PathParam("year") final int year) {
		return exportComponent("all", status, year);
	}


	/**
	 * Export signups for a component or if "all" all components in that year.
	 * We support the "all" parameter so that existing URLs don't break.
	 * @param componentId The component ID to export the signups for.
	 * @param status If specified only export signups with this status.
	 * @param year The year to export components from.
	 * @return A streaming out that writes out XML.
	 */
	@Path("/component/{year}/{id}.xml")
	@GET
	@Produces(MediaType.TEXT_XML)
	public StreamingOutput exportComponent(@PathParam("id") final String componentId,
	                                       @QueryParam("status") final Status status,
	                                       @PathParam("year") final int year) {

		checkAuthenticated();
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {



				Set<Status> statuses = null;
				if (null != status) {
					statuses = Collections.singleton(status);
				}
				List<CourseComponentExport> components = courseService.exportComponentSignups(componentId, statuses, year);

				AttendanceWriter attendance = new AttendanceWriter(output);

				for (CourseComponentExport courseComponent : components) {

					List<CourseSignupExport> signups = courseComponent.getSignups();

						Collections.sort(signups, new Comparator<CourseSignupExport>() {
							public int compare(CourseSignupExport s1, CourseSignupExport s2) {
								Person p1 = s1.getSignup().getUser();
								Person p2 = s2.getSignup().getUser();

								int ret = s1.getGroup().getCourseId().compareTo(s2.getGroup().getCourseId());

								// this line is giving a NullPointerException
								//return ret == 0 ? p1.getLastName().compareTo(p2.getLastName()) : ret;
								if (ret != 0) {
									return ret;
								}

								if (p1 == null) {
									return (p2 == null) ? 0 : -1;
								}
								if (p2 == null) {
									return 1;
								}

								if (p1.getLastName() == null) {
									return (p2.getLastName() == null) ? 0 : -1;
								}
								if (p2.getLastName() == null) {
									return 1;
								}

								return p1.getLastName().compareTo(p2.getLastName());
							}
						});

						attendance.writeTeachingInstance(courseComponent);

				}
				attendance.close();
			}
		};
	}

	@Path("/attendance")
	@GET
	@Produces(MediaType.TEXT_XML)
	public StreamingOutput sync() {
		checkAuthenticated();
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {

				AttendanceWriter attendance = new AttendanceWriter(output);
				List<CourseComponentExport> signups = courseService.exportComponentSignups(null, Collections.singleton(Status.CONFIRMED), null);

				for (CourseComponentExport courseComponent : signups ) {
					attendance.writeTeachingInstance(courseComponent);
				}
				attendance.close();
			}
		};
	}

	@Path("/pending")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getPendingSignups() {
		checkAuthenticated();
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				List<CourseSignup> signups = courseService.getPendings();
				objectMapper.typedWriter(TypeFactory.collectionType(List.class, CourseSignup.class)).writeValue(output, signups);
			}

		};
	}

	@Path("/approve")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getApproveSignups() {
		checkAuthenticated();
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

		checkAuthenticated();
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				List<String> componentIds = Arrays.asList(componentId.split(","));
				Set<CourseSignup> signups = new HashSet<CourseSignup>();
				for (CourseSignup signup : courseService.getUserComponentSignups(userId, null)) {
					if (signup.getGroup().getCourseId().equals(groupId)) {
						for (CourseComponent component : signup.getComponents()) {
							if (!componentIds.contains(component.getPresentationId())) {
								signups.add(signup);
							}
						}

					}
				}
				objectMapper.typedWriter(TypeFactory.collectionType(Set.class, CourseSignup.class)).writeValue(output, signups);
			}

		};
	}

	@Path("/advance/{id}")
	@GET
	@Produces("text/html")
	public Response advanceGet(@PathParam("id") final String encoded) {

		String[] params = courseService.getCourseSignupFromEncrypted(encoded);
		if (log.isDebugEnabled()) {
			for (int i = 0; i < params.length; i++) {
				log.debug("decoded parameter [" + params[i] + "]");
			}
		}
		String signupId = params[0];
		// This is the status that is being advanced to.
		String emailStatus = params[1];
		Status status = toStatus(emailStatus);
		CourseSignup signup = courseService.getCourseSignupAnyway(signupId);
		Map<String, Object> model = new HashMap<>();
		model.put("signup", signup);
		model.put("encoded", encoded);

		// Check that the status we're trying to advance to is valid
		if (!statusProgression.next(signup.getStatus()).contains(status)) {
			model.put("errors", Collections.singletonList("The signup has already been dealt with."));
		} else {
			// We only put the status in if we're happy for it to be changed.
			model.put("status", emailStatus);
		}

		addStandardAttributes(model);

		return Response.ok(new Viewable("/static/advance", model)).build();
	}

	@Path("/advance/{id}")
	@POST
	@Produces("text/html")
	public Response advancePost(@PathParam("id") final String encoded,
	                            @FormParam("formStatus") final String formStatus) {

		if (null == encoded) {
			return Response.noContent().build();
		}
		String[] params = courseService.getCourseSignupFromEncrypted(encoded);

		String signupId = params[0];
		Status status = toStatus(params[1]);
		String placementId = params[2];

		CourseSignup signup = courseService.getCourseSignupAnyway(signupId);
		if (null == signup) {
			return Response.noContent().build();
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("signup", signup);
		if (!statusProgression.next(signup.getStatus()).contains(status)) {
			model.put("errors", Collections.singletonList("The signup has already been dealt with."));
		} else {
			try {
				switch (formStatus.toLowerCase()) {
					case "accept":
						courseService.accept(signupId, true, placementId);
						break;
					case "approve":
						courseService.approve(signupId, true, placementId);
						break;
					case "confirm":
						courseService.confirm(signupId, true, placementId);
						break;
					case "reject":
						courseService.reject(signupId, true, placementId);
						break;
					default:
						throw new IllegalStateException("No mapping for action of: "+ formStatus);
				}
			} catch (IllegalStateException ise) {
				model.put("errors", Collections.singletonList(ise.getMessage()));
			}
		}

		addStandardAttributes(model);
		return Response.ok(new Viewable("/static/ok", model)).build();
	}

	/**
	 * The statuses that go out in emails are action rather that actual statuses, this
	 * method converts the email status into an actual status.
	 * @param emailStatus The status to convert.
	 * @thows IllegalArgumentException
	 */
	public Status toStatus(String emailStatus) {
		switch (emailStatus) {
			case "accept":
				return Status.ACCEPTED;
			case "approve":
				return Status.APPROVED;
			case "confirm":
				return Status.CONFIRMED;
			case "reject":
				return Status.REJECTED;
			default:
				throw new IllegalArgumentException("Not a valid email status: "+ emailStatus);
		}
	}

	/**
	 * This just adds the standard skin values that are needed when rendering a page.
	 * @param model The model to add the values to.
	 */
	public void addStandardAttributes(Map<String, Object> model) {
		model.put("skinRepo",
				serverConfigurationService.getString("skin.repo", "/library/skin"));
		model.put("skinDefault",
				serverConfigurationService.getString("skin.default", "default"));
		model.put("skinPrefix",
				serverConfigurationService.getString("portal.neoprefix", ""));
	}

	private String buildString(Collection<String> collection) {
		StringBuilder sb = new StringBuilder();
		if (!collection.isEmpty()) {
			for(String s: collection) {
				sb.append(s).append('/');
			}
		}
		return sb.toString();
	}

	private String startsText(CourseComponent component) {
		if (null != component.getStartsText() &&
				!component.getStartsText().isEmpty()) {
			return component.getStartsText();
		}
		if (null != component.getStarts()) {
			return new SimpleDateFormat("EEE d MMM yyyy").format(component.getStarts());
		}
		return "";
	}

	private String getFileName(CourseComponent component) {
		StringBuilder sb = new StringBuilder();
		sb.append(component.getPresentationId().replaceAll(" ", "_"));
		if (null != component.getWhen()) {
			sb.append("_");
			sb.append(component.getWhen().replaceAll(" ", "_"));
		}
		return sb.toString();
	}

	/**
	 * This just checks that the request is authenticated and if no throws an exception.
	 * @throws WebAppForbiddenException
	 */
	private void checkAuthenticated() {
		if(proxy.isAnonymousUser()) {
			throw new WebAppForbiddenException();
		}
	}
}
