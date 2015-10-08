package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.Person;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by buckett on 02/10/15.
 */
public class AcceptedEmailRuleTest extends EmailRuleBase {

    private AcceptedEmailRule rule;

    @Before
    public void setUp() {
        rule = new AcceptedEmailRule();
        rule.setService(emailSendingService);
        rule.setProxy(proxy);
    }

    @Test
    public void testMatchesPending() {
        setOldStatus(CourseSignupService.Status.PENDING);
        setNewStatus(CourseSignupService.Status.ACCEPTED);
        assertTrue(rule.matches(change));
    }

    @Test
    public void testMatchesWaiting() {
        setOldStatus(CourseSignupService.Status.WAITING);
        setNewStatus(CourseSignupService.Status.ACCEPTED);
        assertTrue(rule.matches(change));
    }

    @Test
    public void testPerform() {
        Person supervisor = mock(Person.class);
        when(supervisor.getId()).thenReturn("supervisor-id");
        when(signup.getSupervisor()).thenReturn(supervisor);
        when(signup.getId()).thenReturn("signup-id");
        change.placement = "placement-id";
        when(proxy.getConfirmUrl(signup.getId(), "placement-id")).thenReturn("http://server/confirm/placement-id");
        when(proxy.getAdvanceUrl(signup.getId(), "approve", "placement-id")).thenReturn("http://server/approve/placement-id");
        rule.perform(change);
        verify(emailSendingService).sendSignupEmail(eq("supervisor-id"), eq(signup), anyString(), anyString(), any(Object[].class));
    }

}