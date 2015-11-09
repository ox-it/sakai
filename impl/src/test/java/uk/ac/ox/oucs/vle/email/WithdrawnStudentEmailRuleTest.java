package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.UserProxy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.WITHDRAWN;

/**
 * Created by buckett on 08/10/15.
 */
public class WithdrawnStudentEmailRuleTest extends EmailRuleBase {

    private WithdrawnStudentEmailRule rule;

    @Before
    public void setUp() {
        rule = new WithdrawnStudentEmailRule();
        rule.setProxy(proxy);
        rule.setService(emailSendingService);
    }

    @Test
    public void testMatches() throws Exception {
        setNewStatus(WITHDRAWN);
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