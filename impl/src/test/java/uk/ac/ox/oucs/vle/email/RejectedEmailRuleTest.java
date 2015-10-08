package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ox.oucs.vle.Person;
import uk.ac.ox.oucs.vle.UserProxy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.*;

/**
 * Created by buckett on 24/09/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RejectedEmailRuleTest extends EmailRuleBase {

    private RejectedEmailRule rule;

    @Before
    public void setUp() {
        rule = new RejectedEmailRule();
        rule.setService(emailSendingService);
        rule.setProxy(proxy);
    }

    @Test
    public void testMatchingPending() {
        when(signup.getStatus()).thenReturn(REJECTED);
        StateChange stateChange = new StateChange(PENDING, signup, null, "placementId");
        assertTrue(rule.matches(stateChange));
    }

    @Test
    public void testMatchingWaiting() {
        when(signup.getStatus()).thenReturn(REJECTED);
        StateChange stateChange = new StateChange(WAITING, signup, null, "placementId");
        assertTrue(rule.matches(stateChange));
    }

    @Test
    public void testEmailOuput() {
        Person studentUser = mock(Person.class);
        when(studentUser.getId()).thenReturn("student-user-1");
        when(signup.getStatus()).thenReturn(REJECTED);
        when(signup.getUser()).thenReturn(studentUser);
        StateChange stateChange = new StateChange(WAITING, signup, null, "placementId");

        UserProxy user = mock(UserProxy.class);
        when(user.getDisplayName()).thenReturn("Display Name");
        when(proxy.getCurrentUser()).thenReturn(user);

        rule.perform(stateChange);
        verify(emailSendingService).sendStudentSignupEmail(eq(signup), anyString(), anyString(), any(Object[].class));
    }

}
