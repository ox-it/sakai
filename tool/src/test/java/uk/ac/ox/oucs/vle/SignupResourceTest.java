package uk.ac.ox.oucs.vle;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by buckett on 21/10/15.
 */
public class SignupResourceTest extends ResourceTest {

    @Test
    public void testSignup() throws JSONException {
        when(proxy.isAnonymousUser()).thenReturn(false);

        CourseSignup signup = mock(CourseSignup.class);
        when(signup.getId()).thenReturn("id");
        when(signup.getNotes()).thenReturn("notes");

        when(courseSignupService.signup(anyString(), anyString(), anyString(), anyString(), anySet(), anyString()))
                .thenReturn(signup);
        MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<String, String>();
        Response response = target("/signup/new").request("application/json").post(Entity.form(formData));
        assertEquals(201, response.getStatus());
        verify(courseSignupService, times(1)).signup(anyString(), anyString(), anyString(), anyString(), anySet(), anyString());
        String json = response.readEntity(String.class);
        JSONAssert.assertEquals("{id: 'id', notes: 'notes'}", json, JSONCompareMode.LENIENT);
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
    public void testNotAllowedExportError() {
        // Check that when you're not allowed to export we generate a good message.
        when(courseSignupService.exportComponentSignups("all", null, 2014)).thenThrow(PermissionDeniedException.class);
        when(proxy.isAnonymousUser()).thenReturn(false);
        Response response = target("/signup/component/2014/all.xml").queryParam("_auth", "basic").request().get();
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testNotAllowedExportAnonError() {
        // Check that when you're not logged in we give a good error.
        when(proxy.isAnonymousUser()).thenReturn(true);
        Response response = target("/signup/component/2014/all.xml").queryParam("_auth", "basic").request().get();
        assertEquals(403, response.getStatus());
    }
}
