package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.Person;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.WAITING;

/**
 * Created by buckett on 07/10/15.
 */
public class FullEmailRuleTest extends EmailRuleBase {

    private FullEmailRule rule;

    @Before
    public void setUp() {
        rule = new FullEmailRule();
        rule.setProxy(proxy);
        rule.setService(emailSendingService);
    }

    @Test
    public void testMatches() throws Exception {
        setOldStatus(null);
        setNewStatus(WAITING);
        assertTrue(rule.matches(change));
    }

    @Test
    public void testPerform() throws Exception {
        CourseGroup group = mock(CourseGroup.class);
        Person admin1 = mock(Person.class);
        Person admin2 = mock(Person.class);
        when(admin1.getId()).thenReturn("admin-1");
        when(admin2.getId()).thenReturn("admin-2");
        when(group.getAdministrators()).thenReturn(Arrays.asList(admin1, admin2));
        when(signup.getGroup()).thenReturn(group);
        rule.perform(change);

        verify(emailSendingService).sendSignupWaitingEmail(eq("admin-1"), eq(signup), anyString(), anyString(), any(Object[].class));
        verify(emailSendingService).sendSignupWaitingEmail(eq("admin-2"), eq(signup), anyString(), anyString(), any(Object[].class));
        verify(emailSendingService).sendStudentSignupEmail(eq(signup), anyString(), anyString(), any(Object[].class));

    }
}