package org.sakaiproject.hierarchy.tool.vm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.DispatcherServlet;



/**
 * This basically tests that the tool is working correctly without running it in a container.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(AutowiringTestExecutionListener.class)
@ContextConfiguration(locations={"classpath:test-resources.xml", "classpath:applicationContext.xml"},
		loader=MockWebApplicationContextLoader.class)
@MockWebApplication(name="sakai.hierarchy-manager", webapp="src/webapp")
@Configurable(autowire=Autowire.BY_TYPE)
public class AddRedirectControllerTest {
	
    @Autowired
    private DispatcherServlet servlet;
    
    @Autowired
    private PortalHierarchyService portalHierarchyService;
    
    @Autowired
    private SessionManager sessionManager;
    
    public void setServlet(DispatcherServlet servlet) {
		this.servlet = servlet;
	}

    public void setPortalHierarchyService(
			PortalHierarchyService portalHierarchyService) {
		this.portalHierarchyService = portalHierarchyService;
	}

    public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Before
    public void setUp() {
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
    	
    	Session session = mock(Session.class);
    	when(sessionManager.getCurrentSession()).thenReturn(session);
    }

	@Test
	public void testManageHome() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
        // As in sakai the path info is where we do our controller routing from.
        request.setPathInfo("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        assertEquals(200, response.getStatus());
	}

	@Test
    public void testForm() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
        // As in sakai the path info is where we do our controller routing from.
        request.setPathInfo("/redirect/add");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        assertEquals(200, response.getStatus());
    }
	
	@Test
	public void testSubmit() throws ServletException, IOException, PermissionException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setPathInfo("/redirect/add");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		
		assertEquals(200, response.getStatus());
		assertTrue(response.getContentAsString().contains("Title cannot be empty."));
		// Check that validation should have stopped the request.
		verify(portalHierarchyService, never()).newRedirectNode(anyString(), anyString(), anyString(), anyString(), anyBoolean());
	}
}
