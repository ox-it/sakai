package uk.ac.ox.oucs.vle;

import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.ac.ox.oucs.vle.stub.CourseSignupStub;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Basic tests of the SignupResource.
 *
 * @author Matthew Buckett
 */
public class CourseSignupResourceTest  extends JerseyTest {

	private CourseSignupService courseSignupService;
	private SakaiProxy proxy;

	private ServletContainer container;

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new GrizzlyWebTestContainerFactory();
	}

	@Before
	public void setupBeans() throws Exception {
		// We grab the beans as
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(container.getServletContext());
		proxy = webApplicationContext.getBean(SakaiProxy.class);
		courseSignupService = webApplicationContext.getBean(CourseSignupService.class);
	}

	@Override
	protected DeploymentContext configureDeployment(){
		container = new ServletContainer();
		return ServletDeploymentContext.forServlet(container)
				.addListener(ContextLoaderListener.class)
				.contextParam("contextConfigLocation", "classpath:test.xml")
				.initParam("jersey.config.server.provider.packages", "uk.ac.ox.oucs.vle.resources;org.codehaus.jackson.jaxrs")
				.build();
	}

	@Test(expected = javax.ws.rs.NotFoundException.class)
	public void testNothing() {
		target("doesNotExist").request("application/json").get(ClientResponse.class);
	}


	@Test
	public void testSignup() {
		when(proxy.isAnonymousUser()).thenReturn(false);

		CourseSignupStub stubSignup = new CourseSignupStub();
		stubSignup.setId("id");
		stubSignup.setNotes("notes");

		when(courseSignupService.signup(anyString(), anyString(), anyString(), anyString(), anySet(), anyString()))
				.thenReturn(stubSignup);
		MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<String, String>();
		Response response = target("/signup/new").request("application/json").post(Entity.form(formData));
		assertEquals(201, response.getStatus());
		verify(courseSignupService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anySet(), anyString());
	}

	@Test
	public void testSignupNotFound() {
		// Check that we map exceptions correctly.
		when(proxy.isAnonymousUser()).thenReturn(false);
		when(courseSignupService.signup(anyString(), anySet(), anyString(), anyString())).thenThrow(new NotFoundException("id"));
		MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<String, String>();
		Response response = target("/signup/my/new").request("application/json").post(Entity.form(formData));
		assertEquals(404, response.getStatus());
	}

	@Test
	public void testSignupSplit() {
		when(proxy.isAnonymousUser()).thenReturn(false);
		when(courseSignupService.split(eq("signupId"), anySetOf(String.class))).thenReturn("newSignupId");
		MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<String, String>();
		Response response = target("/signup/signupId/split").queryParam("componentPresentationId", "1").request("application/json").post(Entity.form(formData));
		assertEquals(201, response.getStatus());
	}

	@Test
	public void testMySignups() {
		CourseSignup signup = mock(CourseSignup.class);
		when(courseSignupService.getMySignups(null)).thenReturn(Collections.singletonList(signup));
		Response response = target("/signup/my").request("application/json").get();
		assertEquals(200, response.getStatus());
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

}
