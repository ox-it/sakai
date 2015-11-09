package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.Person;
import uk.ac.ox.oucs.vle.UserProxy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.ACCEPTED;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.REJECTED;

/**
 * Created by buckett on 07/10/15.
 */
public class RejectedAcceptedEmailRuleTest extends EmailRuleBase {

    private RejectedAcceptedEmailRule rule;

    @Before
    public void setUp() {
        rule = new RejectedAcceptedEmailRule();
        rule.setProxy(proxy);
        rule.setService(emailSendingService);
    }

    @Test
    public void testMatches() throws Exception {
        setOldStatus(ACCEPTED);
        setNewStatus(REJECTED);
        assertTrue(rule.matches(change));
    }

    @Test
    public void testPerformNoSupervisor() throws Exception {
        UserProxy current = mock(UserProxy.class);
        when(current.getDisplayName()).thenReturn("Current User");
        when(proxy.getCurrentUser()).thenReturn(current);

        Person signupUser = mock(Person.class);
        when (signupUser.getId()).thenReturn("signup-user-id");
        when(signup.getUser()).thenReturn(signupUser);

        rule.perform(change);
        verify(emailSendingService).sendStudentSignupEmail(eq(signup), anyString(), anyString(), any(Object[].class));
    }

    @Test
    public void testPerformSupervisor() throws Exception {
        Person supervisor = mock(Person.class);
        when(supervisor.getName()).thenReturn("Supervisor");
        when(signup.getSupervisor()).thenReturn(supervisor);

        Person signupUser = mock(Person.class);
        when (signupUser.getId()).thenReturn("signup-user-id");
        when(signup.getUser()).thenReturn(signupUser);

        rule.perform(change);
        verify(emailSendingService).sendStudentSignupEmail(eq(signup), anyString(), anyString(), any(Object[].class));
    }
}