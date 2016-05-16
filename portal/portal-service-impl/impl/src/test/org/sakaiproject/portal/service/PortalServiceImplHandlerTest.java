package org.sakaiproject.portal.service;

import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.service.PortalServiceImpl;

import javax.servlet.ServletContext;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This set of tests was written to check that giving portal handlers a priority worked.
 *
 * Created by buckett on 27/01/15.
 */
public class PortalServiceImplHandlerTest {

    private PortalServiceImpl service;
    private ServletContext context;

    @Before
    public void setUp() {
        // Disable logging
        LogFactory.getFactory().setAttribute(
                "org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");

        service = new PortalServiceImpl();
        context = mock(ServletContext.class);
    }

    @Test
    public void testAddHandlerNoPortal() {
        // Check when there's no portal that a handler of the same priority takes precedence.
        PortalHandler ph1 = mockPortalHandler("/handler", 0);
        PortalHandler ph2 = mockPortalHandler("/handler", 0);

        service.addHandler("portal", ph1);
        service.addHandler("portal", ph2);

        Portal portal = mockPortal();

        verify(ph1, never()).register(portal, service, context);
        verify(ph2).register(portal, service, context);

        Map<String, PortalHandler> handlerMap = service.getHandlerMap(portal);
        assertTrue(handlerMap.containsKey("/handler"));
        assertEquals(ph2, handlerMap.get("/handler"));
        assertNotEquals(ph1, handlerMap.get("/handler"));
    }

    @Test
    public void testAddHandlerWithPortal() {
        // Check when there is a portal that the later handler is returned.
        Portal portal = mockPortal();

        PortalHandler ph1 = mockPortalHandler("/handler", 0);
        PortalHandler ph2 = mockPortalHandler("/handler", 0);

        service.addHandler("portal", ph1);
        service.addHandler("portal", ph2);

        verify(ph1).register(portal, service, context);
        verify(ph1).deregister(portal);
        verify(ph2).register(portal, service, context);

        Map<String, PortalHandler> handlerMap = service.getHandlerMap(portal);
        assertTrue(handlerMap.containsKey("/handler"));
        assertEquals(ph2, handlerMap.get("/handler"));
        assertNotEquals(ph1, handlerMap.get("/handler"));
    }

    @Test
    public void testAddHandlerPriorityNoPortal() {
        // Check when there is a portal that the higher priority handler is returned
        PortalHandler ph1 = mockPortalHandler("/handler", 10);
        PortalHandler ph2 = mockPortalHandler("/handler", 0);

        service.addHandler("portal", ph1);
        service.addHandler("portal", ph2);

        Portal portal = mockPortal();

        Map<String, PortalHandler> handlerMap = service.getHandlerMap(portal);
        assertTrue(handlerMap.containsKey("/handler"));
        assertEquals(ph1, handlerMap.get("/handler"));
        assertNotEquals(ph2, handlerMap.get("/handler"));
    }

    @Test
    public void testAddHandlerPriorityWithPortal() {
        // Check when there is a portal that the higher priority handler is returned
        Portal portal = mockPortal();

        PortalHandler ph1 = mockPortalHandler("/handler", 10);
        PortalHandler ph2 = mockPortalHandler("/handler", 0);

        service.addHandler("portal", ph1);
        service.addHandler("portal", ph2);

        Map<String, PortalHandler> handlerMap = service.getHandlerMap(portal);
        assertTrue(handlerMap.containsKey("/handler"));
        assertEquals(ph1, handlerMap.get("/handler"));
        assertNotEquals(ph2, handlerMap.get("/handler"));
    }

    // Create a mock portal handler.
    protected PortalHandler mockPortalHandler(String fragment, int priority) {
        PortalHandler ph1 = mock(PortalHandler.class);
        when(ph1.getUrlFragment()).thenReturn(fragment);
        when(ph1.getPriority()).thenReturn(priority);
        return ph1;
    }

    // Create a mock portal.
    protected Portal mockPortal() {
        Portal portal = mock(Portal.class);
        when(portal.getPortalContext()).thenReturn("portal");
        when(portal.getServletContext()).thenReturn(context);
        service.addPortal(portal);
        return portal;
    }
}
