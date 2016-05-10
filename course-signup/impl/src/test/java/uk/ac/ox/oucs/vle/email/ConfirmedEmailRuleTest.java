package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.ACCEPTED;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.CONFIRMED;

/**
 * Created by buckett on 02/10/15.
 */
public class ConfirmedEmailRuleTest extends EmailRuleBase {

    private ConfirmedEmailRule rule;

    @Before
    public void setUp() {
        rule = new ConfirmedEmailRule();
        rule.setProxy(proxy);
        rule.setService(emailSendingService);
    }

    @Test
    public void testMatching() {
        setNewStatus(CONFIRMED);
        StateChange change = new StateChange(ACCEPTED, signup, null, "placementId");
        assertTrue(rule.matches(change));
    }


    @Test
    public void testEmailOutput() {
        StateChange change = new StateChange(ACCEPTED, signup, null, "placementId");
        rule.perform(change);
        verify(emailSendingService).sendStudentSignupEmail(eq(signup), anyString(), anyString(), any(Object[].class));
    }
}
