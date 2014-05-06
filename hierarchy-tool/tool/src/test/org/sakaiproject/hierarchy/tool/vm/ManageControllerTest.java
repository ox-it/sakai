package org.sakaiproject.hierarchy.tool.vm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.sakaiproject.hierarchy.tool.vm.UnitTestUtilities.assertContains;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * This basically tests that the tool is working correctly without running it in
 * a container.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(AutowiringTestExecutionListener.class)
@ContextConfiguration(locations = { "classpath:test-resources.xml", "classpath:applicationContext.xml" }, loader = MockWebApplicationContextLoader.class)
@MockWebApplication(name = "sakai.hierarchy-manager", webapp = "src/webapp")
@Configurable(autowire = Autowire.BY_TYPE)
public class ManageControllerTest {

	@Autowired
	private DispatcherServlet servlet;

	@Autowired
	private PortalHierarchyService portalHierarchyService;

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	private SiteService siteService;

	private Session session;

	@Before
	public void setUp() {
		// Reset the injected mocks.
		reset(portalHierarchyService);
		reset(sessionManager);

		Site site = mock(Site.class);
		when(site.getId()).thenReturn("site-id");
		when(site.getTitle()).thenReturn("Site Title");
		when(site.getShortDescription()).thenReturn("Short Description");

		PortalNodeSite node = mock(PortalNodeSite.class);
		when(node.getId()).thenReturn("id");
		when(node.getName()).thenReturn("name");
		when(node.getPath()).thenReturn("/name");
		when(node.getSite()).thenReturn(site);
		when(portalHierarchyService.getCurrentPortalNode()).thenReturn(node);

		session = mock(Session.class);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		// This is because we try to shorten URLs that could be relative.
		when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost/");
	}

	@Test
	public void testManageHome() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/context/");
		request.setContextPath("/context");
		// As in sakai the path info is where we do our controller routing from.
		request.setPathInfo("/");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void testRedirectAddEmpty() throws ServletException, IOException, PermissionException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/context/redirect/add");
		request.setContextPath("/context");
		request.setPathInfo("/redirect/add");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		assertEquals(200, response.getStatus());
		assertTrue(response.getContentAsString().contains("Title cannot be empty."));
		// Check that validation should have stopped the request.
		verify(portalHierarchyService, never()).newRedirectNode(anyString(), anyString(), anyString(), anyString(),
				anyBoolean(), anyBoolean());
	}

	@Test
	public void testRedirectAddUntickPath() throws ServletException, IOException, PermissionException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/context/redirect/add");
		request.setContextPath("/context");
		request.setPathInfo("/redirect/add");
		request.addParameter("name", "name-value");
		request.addParameter("title", "titleValue");
		request.addParameter("url", "urlValue");
		// Check we manage to unset the boolean
		request.addParameter("_appendPath", "true");
		request.addParameter("_hidden", "true");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		assertEquals(200, response.getStatus());

		verify(portalHierarchyService, times(1)).newRedirectNode("id", "name-value", "urlValue", "titleValue", false, false);

	}

	@Test
	public void testRedirectAddGood() throws ServletException, IOException, PermissionException {
		// Check we attempt to call the service when everything is ok.
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/context/redirect/add");
		request.setContextPath("/context");
		request.setPathInfo("/redirect/add");
		request.addParameter("name", "name-value");
		request.addParameter("title", "titleValue");
		request.addParameter("url", "urlValue");
		request.addParameter("appendPath", "true");
		request.addParameter("_appendPath", "true");
		request.addParameter("hidden", "true");
		request.addParameter("_hidden", "true");

		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);

		assertEquals(200, response.getStatus());

		verify(portalHierarchyService, times(1)).newRedirectNode("id", "name-value", "urlValue", "titleValue", true, true);
	}

	@Test
	public void testRedirectAddShorterning() throws ServletException, IOException, PermissionException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/redirect/add");
		request.addParameter("name", "name-value");
		request.addParameter("title", "titleValue");
		request.addParameter("url", "http://localhost/access/file/test.txt");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);
		// Check that the relative URL got trimmed.
		verify(portalHierarchyService).newRedirectNode("id", "name-value", "/access/file/test.txt", "titleValue", true, false);
	}

	@Test
	public void testRedirectAddExists() throws ServletException, IOException, PermissionException {
		when(portalHierarchyService.newRedirectNode("id", "name-value", "urlValue", "titleValue", true, false)).thenThrow(
				new IllegalArgumentException());
		// Check we attempt to call the service when everything is ok.
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/redirect/add");
		request.addParameter("name", "name-value");
		request.addParameter("title", "titleValue");
		request.addParameter("url", "urlValue");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);

		assertEquals(200, response.getStatus());
		assertTrue(response.getContentAsString().contains("The name is already used here."));
	}

	@Test
	public void testRedirectDeleteNone() throws ServletException, IOException, PermissionException {
		// Need existing redirect or we don't see the error
		PortalNodeRedirect redirect = mock(PortalNodeRedirect.class);
		when(redirect.getId()).thenReturn("redirect");
		when(redirect.getName()).thenReturn("Redirect Name");
		when(redirect.getUrl()).thenReturn("/name/redirect");
		when(portalHierarchyService.getNodeChildren(anyString())).thenReturn(
				Arrays.asList(new PortalNode[] { redirect }));

		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/redirect/delete");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		assertEquals(200, response.getStatus());
		// Check that validation should have stopped the request.
		verify(portalHierarchyService, never()).deleteNode(anyString());
		assertTrue(response.getContentAsString().contains("You have not selected an item."));
	}

	@Test
	public void testRedirectDeleteOk() throws ServletException, IOException, PermissionException {
		// Check we attempt to call the service when everything is ok.
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/redirect/delete");
		request.addParameter("id", "id-to-remove");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);

		assertEquals(200, response.getStatus());

		verify(portalHierarchyService, times(1)).deleteNode("id-to-remove");
	}

	@Test
	public void testRedirectDeletePermission() throws ServletException, IOException, PermissionException {

		// Need existing redirect or we don't see the error
		PortalNodeRedirect redirect = mock(PortalNodeRedirect.class);
		when(redirect.getId()).thenReturn("redirect");
		when(redirect.getName()).thenReturn("Redirect Name");
		when(redirect.getUrl()).thenReturn("/name/redirect");
		when(portalHierarchyService.getNodeChildren(anyString())).thenReturn(
				Arrays.asList(new PortalNode[] { redirect }));

		// As it's a void method.
		doThrow(PermissionException.class).when(portalHierarchyService).deleteNode(anyString());
		// Check we attempt to call the service when everything is ok.
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/redirect/delete");
		request.addParameter("id", "id-to-remove");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);

		assertEquals(200, response.getStatus());
		assertContains("Failed due to lack of permission.", response.getContentAsString());
	}

	@Test
	public void testChangeSiteHelper() throws ServletException, IOException {
		// Need a tool session for this.
		ToolSession toolSession = mock(ToolSession.class);
		when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);

		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/site/change");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		assertNotNull(response.getRedirectedUrl());

		ArgumentCaptor<String> attributeKeys = ArgumentCaptor.forClass(String.class);
		verify(toolSession, Mockito.atLeastOnce()).setAttribute(attributeKeys.capture(), anyString());
		assertTrue(attributeKeys.getAllValues().contains(SiteHelper.SITE_PICKER_PERMISSION));
		assertTrue(attributeKeys.getAllValues().contains(Tool.HELPER_DONE_URL));
	}

	@Test
	public void testSaveSite() throws ServletException, IOException, PermissionException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/site/save");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setParameter(ManagerController.REQUEST_SITE, "new-site-id");
		servlet.service(request, response);

		verify(portalHierarchyService, times(1)).changeSite("id", "new-site-id");
	}

	@Test
	// Here we don't set the new site ID and so the container will throw an
	// exception, which gets mapped to a 400 error.
	public void testSaveSiteBad() throws ServletException, IOException, PermissionException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/site/save");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals(400, response.getStatus());
	}

	@Test
	public void testCut() throws ServletException, IOException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/cut");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		verify(session).setAttribute(ManagerController.CUT_ID, "id");
	}

	@Test
	public void testCancel() throws ServletException, IOException {
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/cancel");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		verify(session).removeAttribute(ManagerController.CUT_ID);
	}

	@Test
	public void testPaste() throws PermissionException, ServletException, IOException {
		when(session.getAttribute(ManagerController.CUT_ID)).thenReturn("other-node-id");
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/paste");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		verify(portalHierarchyService).moveNode("other-node-id", "id");
	}


	@Test
	public void testPasteNoPermission() throws PermissionException, ServletException, IOException {
		when(session.getAttribute(ManagerController.CUT_ID)).thenReturn("other-node-id");
		doThrow(new PermissionException("user", "lock", "resource")).when(portalHierarchyService).moveNode("other-node-id", "id");
		MockHttpServletRequest request = UnitTestUtilities.newRequest("POST", "/paste");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertContains("Failed due to lack of permission", response.getContentAsString());
	}

	@Test
	public void testEditSite() throws ServletException, IOException, IdUnusedException {
		// Need a tool session for this.
		ToolSession toolSession = mock(ToolSession.class);
		when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
		when(toolSession.getAttribute(SiteHelper.SITE_PICKER_SITE_ID)).thenReturn("new-site-id");
		Site newSite = mock(Site.class);
		when(newSite.getId()).thenReturn("new-site-id");
		when(newSite.getTitle()).thenReturn("New Site Title");
		when(newSite.getShortDescription()).thenReturn("New Short Description");
		when(siteService.getSite("new-site-id")).thenReturn(newSite);

		MockHttpServletRequest request = UnitTestUtilities.newRequest("GET", "/site/edit");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		verify(siteService).getSite("new-site-id");
		assertEquals(200, response.getStatus());
	}
}
