package uk.ac.ox.oucs.vle;

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Before;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Base for tests that run with a full container.
 */
abstract public class ResourceTest extends JerseyTest {

	protected CourseSignupService courseSignupService;
	protected SakaiProxy proxy;

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
}
