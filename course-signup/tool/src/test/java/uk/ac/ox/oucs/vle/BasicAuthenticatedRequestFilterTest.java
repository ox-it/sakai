package uk.ac.ox.oucs.vle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.RequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Test our basic auth filter.
 */
@RunWith(PowerMockRunner.class)

@PrepareForTest( {
        // This is so that we can mock the statics.
        ComponentManager.class,
        // This is so that the we can mock the new BasicAuth()
        BasicAuthenticatedRequestFilter.class
        })
public class BasicAuthenticatedRequestFilterTest {

    public static final String ANON_ID = "";
    private static final String USER_ID = "userId";
    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Session session;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private BasicAuth basicAuth;

    private BasicAuthenticatedRequestFilter filter;

    @Before
    public void setUp() throws Exception {
        mockStatic(ComponentManager.class);
        when(ComponentManager.get(UserDirectoryService.class)).thenReturn(userDirectoryService);
        when(ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        // Mock the anonymous user
        User anon = mock(User.class);
        when(anon.getId()).thenReturn(ANON_ID);
        when(userDirectoryService.getAnonymousUser()).thenReturn(anon);

        whenNew(BasicAuth.class).withNoArguments().thenReturn(basicAuth);
        filter = new BasicAuthenticatedRequestFilter();
        filter.init(mock(FilterConfig.class));
    }

    @Test
    public void testNormalRequest() throws IOException, ServletException {
        when(request.getAttribute(RequestFilter.ATTR_FILTERED)).thenReturn(RequestFilter.ATTR_FILTERED);
        when(session.getUserId()).thenReturn(ANON_ID);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(session, never()).invalidate();
    }

    @Test(expected = IllegalStateException.class)
    public void testNotFiltered() throws IOException, ServletException {
        when(session.getUserId()).thenReturn(ANON_ID);
        filter.doFilter(request, response, chain);
    }

    @Test
    public void testAuthGood() throws IOException, ServletException {
        when(request.getAttribute(RequestFilter.ATTR_FILTERED)).thenReturn(RequestFilter.ATTR_FILTERED);
        when(session.getUserId()).thenReturn(ANON_ID).thenReturn(USER_ID);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(basicAuth).doLogin(request);
        verify(session).invalidate();
    }

    @Test
    public void testAuthFailed() throws IOException, ServletException {
        when(request.getAttribute(RequestFilter.ATTR_FILTERED)).thenReturn(RequestFilter.ATTR_FILTERED);
        when(session.getUserId()).thenReturn(ANON_ID).thenReturn(ANON_ID);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(basicAuth).doLogin(request);
        verify(session, never()).invalidate();
    }

    @Test
    public void testNoAuthAlreadyLoggedIn() throws IOException, ServletException {
        when(request.getAttribute(RequestFilter.ATTR_FILTERED)).thenReturn(RequestFilter.ATTR_FILTERED);
        when(session.getUserId()).thenReturn(USER_ID).thenReturn(USER_ID);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(basicAuth).doLogin(request);
        verify(session, never()).invalidate();
    }
}
