package uk.ac.ox.oucs.vle;

import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;
import uk.ac.ox.oucs.vle.stub.CourseSignupStub;

import javax.ws.rs.ext.ContextResolver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Matthew Buckett
 */

public class CourseSignupResourceTest extends AbstractSpringAwareJerseyTest {

	@Autowired
	private CourseSignupService courseSignupService;
	@Autowired
	private ServerConfigurationService serverConfigurationService;
	@Autowired
	private SakaiProxy proxy;

	@Override
	public AppDescriptor configure() {
		// Pickup the resources.
		// Need to inject the spring context
		// See ResourceConfig for init params.
		WebAppDescriptor wa = new WebAppDescriptor.Builder("uk.ac.ox.oucs.vle.resources,org.codehaus.jackson.jaxrs")
				.contextParam("contextConfigLocation", "classpath:test.xml")
				.contextListenerClass(ContextLoaderListener.class)
				.servletClass(SpringServlet.class)

				// This enables logging of request/response
				//.initParam(ResourceConfig.FEATURE_TRACE, "true")
				//.initParam(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getCanonicalName())
				//.initParam(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getCanonicalName())
				// Uncomment to stop logging of entity.
				//.initParam(LoggingFilter.FEATURE_LOGGING_DISABLE_ENTITY, "true")
				.build();
		return wa;
	}

	@Test
	public void testNothing() {
		ClientResponse response = resource().path("/doesNotExist").accept("application/json").get(ClientResponse.class);
		assertEquals(404, response.getStatus());

	}

	@Test
	public void testSignup() {
		when(proxy.isAnonymousUser()).thenReturn(false);

		CourseSignupStub stubSignup = new CourseSignupStub();
		stubSignup.setId("id");
		stubSignup.setNotes("notes");

		when(courseSignupService.signup(anyString(), anyString(), anyString(), anyString(), anySet(), anyString()))
				.thenReturn(stubSignup);
		ClientResponse response = resource().path("/signup/new").accept("application/json").post(ClientResponse.class);
		verify(courseSignupService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anySet(), anyString());
		assertEquals(201, response.getStatus());
	}

	@Test
	public void testSignupNotFound() {
		// Check that we map exceptions correctly.
		when(proxy.isAnonymousUser()).thenReturn(false);
		when(courseSignupService.signup(anyString(), anySet(), anyString(), anyString())).thenThrow(new NotFoundException("id"));
		ClientResponse response = resource().path("/signup/my/new").accept("application/json").post(ClientResponse.class);
		assertEquals(404, response.getStatus());
	}




}
