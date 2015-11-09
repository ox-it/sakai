package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.CourseDepartment;
import uk.ac.ox.oucs.vle.CourseSignup;

import java.util.HashSet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.APPROVED;

/**
 * Created by buckett on 07/10/15.
 */
public class ApprovedEmailRuleTest extends EmailRuleBase {

    private ApprovedEmailRule rule;

    @Before
    public void setUp() {
        rule = new ApprovedEmailRule();
        rule.setProxy(proxy);
        rule.setService(emailSendingService);
    }

    @Test
    public void testMatches() {
        setNewStatus(APPROVED);
        assertTrue(rule.matches(change));
    }

    @Test
    public void testPerform() {
        CourseSignup signup = mock(CourseSignup.class);
        when(signup.getId()).thenReturn("signup-id");
        change.signup = signup;
        change.placement = "placement-id";
        CourseDepartment department = mock(CourseDepartment.class);
        when(department.getApprovers()).thenReturn(new HashSet<String>(){{
            add("approver-1");
            add("approver-2");
        }});
        change.department = department;
        rule.perform(change);
        // Check both approvers get an email
        verify(emailSendingService).sendSignupEmail(eq("approver-1"), eq(signup), anyString(), anyString(), any(Object[].class));
        verify(emailSendingService).sendSignupEmail(eq("approver-2"), eq(signup), anyString(), anyString(), any(Object[].class));
    }
}
