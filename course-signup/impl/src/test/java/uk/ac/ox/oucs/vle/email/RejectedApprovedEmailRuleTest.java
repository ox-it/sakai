package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.UserProxy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.APPROVED;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.REJECTED;

/**
 * Created by buckett on 07/10/15.
 */
public class RejectedApprovedEmailRuleTest extends EmailRuleBase {

    private RejectedApprovedEmailRule rule;

    @Before
    public void setUp() {
        rule = new RejectedApprovedEmailRule();
        rule.setService(emailSendingService);
        rule.setProxy(proxy);
    }

    @Test
    public void testMatches() throws Exception {
        setOldStatus(APPROVED);
        setNewStatus(REJECTED);
        assertTrue(rule.matches(change));
    }

    @Test
    public void testPerform() throws Exception {
        UserProxy current = mock(UserProxy.class);
        when(current.getDisplayName()).thenReturn("Current User");
        when(proxy.getCurrentUser()).thenReturn(current);
        rule.perform(change);
        verify(emailSendingService).sendStudentSignupEmail(eq(signup), anyString(), anyString(), any(Object[].class));
    }
}