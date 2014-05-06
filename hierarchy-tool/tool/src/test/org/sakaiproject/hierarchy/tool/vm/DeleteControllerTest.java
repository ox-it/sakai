package org.sakaiproject.hierarchy.tool.vm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.DispatcherServlet;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(AutowiringTestExecutionListener.class)
@ContextConfiguration(locations = { "classpath:test-resources.xml", "classpath:applicationContext.xml" }, loader = MockWebApplicationContextLoader.class)
@MockWebApplication(name = "sakai.hierarchy-manager", webapp = "src/webapp")
@Configurable(autowire = Autowire.BY_TYPE)
public class DeleteControllerTest {

	@Autowired
	private DispatcherServlet servlet;

	@Autowired
	private PortalHierarchyService portalHierarchyService;

	@Autowired
	private SiteService siteService;

	private Site site;

	@Before
	public void setUp() {
		// Reset the injected mocks.
		reset(portalHierarchyService);
		reset(siteService);

		// Mock the supporting stuff.
		site = mock(Site.class);
		when(site.getId()).thenReturn("site-id");
		when(site.getTitle()).thenReturn("Site Title");
		when(site.getShortDescription()).thenReturn("Short Description");

		PortalNodeSite node = mock(PortalNodeSite.class);
		when(node.getId()).thenReturn("id");
		when(node.getName()).thenReturn("name");
		when(node.getPath()).thenReturn("/name");
		when(node.getSite()).thenReturn(site);

		PortalNodeSite root = mock(PortalNodeSite.class);
		when(root.getId()).thenReturn("root");
		when(root.getName()).thenReturn("rootName");
		when(root.getPath()).thenReturn("/");

		when(portalHierarchyService.getCurrentPortalNode()).thenReturn(node);
		when(portalHierarchyService.getNodesFromRoot("id")).thenReturn(
				Arrays.asList(new PortalNodeSite[] { root, node }));
	}

	@Test
	public void testDeleteForm() throws ServletException, IOException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("GET", "/delete");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		assertEquals(200, response.getStatus());
	}

	@Test
	public void testDeleteSubmit() throws ServletException, IOException, IllegalStateException, PermissionException, IdUnusedException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/delete");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		verify(portalHierarchyService).deleteNode("id");
		verify(siteService, never()).removeSite(site);
	}

	@Test
	public void testDeleteSubmitAndRemove() throws ServletException, IOException, IllegalStateException,
			PermissionException, IdUnusedException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/delete");
		request.setParameter("deleteSite", "true");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		verify(portalHierarchyService).deleteNode("id");
		verify(siteService).removeSite(site);
	}
}
