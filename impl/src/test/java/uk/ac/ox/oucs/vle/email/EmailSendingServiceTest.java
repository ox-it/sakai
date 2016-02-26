package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ox.oucs.vle.*;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the sending of emails.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailSendingServiceTest {

    @Mock
    private SakaiProxy proxy;

    @Mock
    private UserProxy recepient;

    @Mock
    private Person signupUser;

    @Mock
    private UserProxy currentUser;

    @Mock
    private CourseSignup signup;
    @Mock
    private CourseGroup group;
    @Mock
    private CourseComponent component;

    private EmailSendingService service;

    @Before
    public void setUp() {
        service = new EmailSendingService();
        service.setProxy(proxy);
    }

    @Test
    public void testSendSignupEmail() throws Exception {
        // This test was initially developed to aid in the removal of duplicate methods that send email
        when(proxy.findUserById("recepientId")).thenReturn(recepient);
        when(signup.getUser()).thenReturn(signupUser);
        when(recepient.getEmail()).thenReturn("recepient@example.com");

        when(signup.getGroup()).thenReturn(group);
        when(group.getTitle()).thenReturn("Group Title");
        when(group.getDepartment()).thenReturn("Group Department");
        when(signup.getComponents()).thenReturn(Collections.singleton(component));
        when(component.getTitle()).thenReturn("Component Title");
        when(component.getSessions()).thenReturn("2 sessions");
        // Ignore when and presenter for now.

        when(proxy.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getDisplayName()).thenReturn("Current User");
        when(signupUser.getName()).thenReturn("Signup User");
        when(signupUser.getDegreeProgram()).thenReturn("Degree Program");

        when(proxy.getMessage("subject")).thenReturn("{0} {2}");
        when(proxy.getMessage("body")).thenReturn("{0} {1} {2} {3} {4} {5}");

        service.sendSignupEmail("recepientId", signup, "subject", "body", new Object[]{"http://example.com"});
        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(proxy).sendEmail(eq("recepient@example.com"), subject.capture(), body.capture());
        assertEquals("Current User Group Title", subject.getValue());
        assertEquals("Current User Group Title (Group Department )\n" +
                "  - Component Title for 2 sessions\n" +
                " Group Title Signup User Degree Program http://example.com", body.getValue());
    }

}
