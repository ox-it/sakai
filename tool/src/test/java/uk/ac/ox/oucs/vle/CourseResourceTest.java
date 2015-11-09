package uk.ac.ox.oucs.vle;

import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseGroup.CategoryType.JACS;
import static uk.ac.ox.oucs.vle.CourseGroup.CategoryType.RDF;
import static uk.ac.ox.oucs.vle.CourseGroup.CategoryType.RM;
import static uk.ac.ox.oucs.vle.CourseSignupService.Range.ALL;

/**
 * Basic tests of the SignupResource.
 *
 * @author Matthew Buckett
 */
@SuppressWarnings("deprecation")
public class CourseResourceTest extends ResourceTest {

	@Test(expected = javax.ws.rs.NotFoundException.class)
	public void testNothing() {
		target("doesNotExist").request("application/json").get(ClientResponse.class);
	}

	@Test
	public void testAdminCourses() throws JSONException {
		CourseGroup group = mock(CourseGroup.class);
		when(group.getCourseId()).thenReturn("course-id");
		when(group.getTitle()).thenReturn("Course Title");
		when(courseSignupService.getAdministering()).thenReturn(Collections.singletonList(group));
		Response response = target("/course/admin").request("application/json").get();
		String string = response.readEntity(String.class);
		assertEquals(200, response.getStatus());
		// Check for our properties.
		JSONAssert.assertEquals("[{title:'Course Title', courseId: 'course-id'}]", string, JSONCompareMode.LENIENT);
		// We don't want these to get called as they will load more resources from the database.
		verify(group, never()).getComponents();
		verify(group, never()).getAdministrators();
		verify(group, never()).getCategories();
	}

	@Test
	public void testLecturingCourses() throws JSONException {
		CourseGroup group = mock(CourseGroup.class);
		when(group.getCourseId()).thenReturn("course-id");
		when(group.getTitle()).thenReturn("Course Title");
		when(courseSignupService.getLecturing()).thenReturn(Collections.singletonList(group));
		Response response = target("/course/lecture").request("application/json").get();
		String string = response.readEntity(String.class);
		assertEquals(200, response.getStatus());
		// Check we got our properties
		JSONAssert.assertEquals("[{title:'Course Title', courseId: 'course-id'}]", string, JSONCompareMode.LENIENT);
		// We don't want these to get called as they will load more resources from the database.
		verify(group, never()).getComponents();
		verify(group, never()).getAdministrators();
		verify(group, never()).getCategories();
	}

	@Test
	public void testGetCourse() throws JSONException {
		// Currently getCourse has a custom streaming implementation that in time should go
		// but we need to test to make sure the JSON we're returning is the same.
		CourseGroup group = mockGroup();
		when(courseSignupService.getCourseGroup("course-id", ALL)).thenReturn(group);

		Response response = target("/course/course-id").queryParam("range", ALL).request("application/json").get();
		assertEquals(200, response.getStatus());
		String json = response.readEntity(String.class);
		JSONAssert.assertEquals("{" +
				"id: 'id'," +
				"description: 'description'," +
				"title: 'title'," +
				"supervisorApproval: true," +
				"administratorApproval: true," +
				"visibility: 'visibility'," +
				"isAdmin: true," +
				"isSuperuser: true," +
				"department: 'department'," +
				"departmentCode: 'department-code'," +
				"subUnit: 'subunit'," +
				"subUnitCode: 'subunit-code'," +
				"prerequisite: 'prerequisite'," +
				"regulations: 'regulations'," +
				"source: 'source'," +
				"components: [" + "{" +
					"id: 'presentation-id'," +
					"location: 'location'," +
					"teachingDetails: 'teaching-details'," +
					"size: 0," +
					"opens: 0," +
					"opensText: 'opens-text'," +
					"closes: 0," +
					"closesText: 'closes-text'," +
					"starts: 0," +
					"startsText: 'starts-text'," +
					"ends: 0," +
					"endsText: 'ends-text',"+
					"title: 'title',"+
					"when: 'when'," +
					"bookable: true," +
					"places: 0," +
					"componentSet: 'component-set'," +
					"sessionCount: '0'," +
					"applyTo: 'apply-to'," +
					"memberApplyTo: 'member-apply-to'," +
					"attendanceMode: 'attendance-mode'," +
					"attendanceModeText: 'attendance-mode-text'," +
					"attendancePattern: 'attendance-pattern'," +
					"attendancePatternText: 'attendance-pattern-text'," +
					"presenter: {" +
						"name: 'name'," +
						"email: 'my@email.address'" +
					"}," +
					"sessions: [ {" +
						"sessionId: 'session-id'," +
						"sessionStart: 0," +
						"sessionStartText: 'session-start'," +
						"sessionEnd: 0," +
						"sessionEndText: 'session-end'," +
						"location: 'location'" +
					"} ]" +
				"} ]," +
				"administrators: [ {" +
					"id: 'id'," +
					"name: 'name'," +
					"type: 'type'," +
					"email: 'my@email.address'," +
					"firstName: 'first-name'," +
					"lastName: 'last-name'," +
					"departmentName: 'department-name'," +
					"webauthId: 'webauth-id'" +
				"} ]," +
				"superusers: [ {" +
					"id: 'id'," +
					"name: 'name'," +
					"type: 'type'," +
					"email: 'my@email.address'," +
					"firstName: 'first-name'," +
					"lastName: 'last-name'," +
					"departmentName: 'department-name'," +
					"webauthId: 'webauth-id'" +
				"} ]," +
				"otherDepartments: ['other-departments']," +
				"categories_jacs: [ 'name' ]," +
				"categories_rdf: [ 'name' ]," +
				"categories_rm: [ 'name' ]" +
				"}", json, JSONCompareMode.NON_EXTENSIBLE);

	}

	public CourseGroup mockGroup() {
		CourseGroup group = mock(CourseGroup.class);

		// The basic fields.
		when(group.getCourseId()).thenReturn("id");
		when(group.getDescription()).thenReturn("description");
		when(group.getTitle()).thenReturn("title");
		when(group.getSupervisorApproval()).thenReturn(true);
		when(group.getAdministratorApproval()).thenReturn(true);
		when(group.getVisibility()).thenReturn("visibility");
		when(group.getIsAdmin()).thenReturn(true);
		when(group.getIsSuperuser()).thenReturn(true);
		when(group.getDepartment()).thenReturn("department");
		when(group.getDepartmentCode()).thenReturn("department-code");
		when(group.getSubUnit()).thenReturn("subunit");
		when(group.getSubUnitCode()).thenReturn("subunit-code");
		when(group.getPrerequisite()).thenReturn("prerequisite");
		when(group.getRegulations()).thenReturn("regulations");
		when(group.getSource()).thenReturn("source");

		CourseComponent component = mockComponent();
		when(group.getComponents()).thenReturn(Collections.singletonList(component));

		Person person = mockPerson();
		when(group.getAdministrators()).thenReturn(Collections.singletonList(person));
		when(group.getSuperusers()).thenReturn(Collections.singletonList(person));

		when(group.getOtherDepartments()).thenReturn(Collections.singletonList("other-departments"));

		CourseCategory category = mock(CourseCategory.class);
		when(category.getName()).thenReturn("name");
		List<CourseCategory> categories = Collections.singletonList(category);
		when(group.getCategories(RDF)).thenReturn(categories);
		when(group.getCategories(JACS)).thenReturn(categories);
		when(group.getCategories(RM)).thenReturn(categories);
		return group;
	}

	public CourseComponent mockComponent() {
		CourseComponent component = mock(CourseComponent.class);
		when(component.getPresentationId()).thenReturn("presentation-id");
		when(component.getTitle()).thenReturn("title");
		when(component.getLocation()).thenReturn("location");
		when(component.getTeachingDetails()).thenReturn("teaching-details");
		when(component.getSize()).thenReturn(0);
		when(component.getOpens()).thenReturn(new Date(0));
		when(component.getOpensText()).thenReturn("opens-text");
		when(component.getCloses()).thenReturn(new Date(0));
		when(component.getClosesText()).thenReturn("closes-text");
		when(component.getWhen()).thenReturn("when");
		when(component.getBookable()).thenReturn(true);
		when(component.getStarts()).thenReturn(new Date(0));
		when(component.getStartsText()).thenReturn("starts-text");
		when(component.getEnds()).thenReturn(new Date(0));
		when(component.getEndsText()).thenReturn("ends-text");
		when(component.getPlaces()).thenReturn(0);
		when(component.getComponentSet()).thenReturn("component-set");
		when(component.getSessions()).thenReturn("0");
		when(component.getApplyTo()).thenReturn("apply-to");
		when(component.getMemberApplyTo()).thenReturn("member-apply-to");
		when(component.getAttendanceMode()).thenReturn("attendance-mode");
		when(component.getAttendanceModeText()).thenReturn("attendance-mode-text");
		when(component.getAttendancePattern()).thenReturn("attendance-pattern");
		when(component.getAttendancePatternText()).thenReturn("attendance-pattern-text");

		Person person = mockPerson();

		when(component.getPresenter()).thenReturn(person);

		CourseComponentSession session = mock(CourseComponentSession.class);
		when(session.getSessionId()).thenReturn("session-id");
		when(session.getSessionStart()).thenReturn(new Date(0));
		when(session.getSessionStartText()).thenReturn("session-start");
		when(session.getSessionEnd()).thenReturn(new Date(0));
		when(session.getSessionEndText()).thenReturn("session-end");
		when(session.getLocation()).thenReturn("location");

		when(component.getComponentSessions()).thenReturn(Collections.singletonList(session));
		return component;
	}

	public Person mockPerson() {
		Person administrator = mock(Person.class);
		when(administrator.getId()).thenReturn("id");
		when(administrator.getName()).thenReturn("name");
		when(administrator.getType()).thenReturn("type");
		when(administrator.getEmail()).thenReturn("my@email.address");
		when(administrator.getFirstName()).thenReturn("first-name");
		when(administrator.getLastName()).thenReturn("last-name");
		when(administrator.getDepartmentName()).thenReturn("department-name");
		when(administrator.getWebauthId()).thenReturn("webauth-id");
		return administrator;
	}

}
