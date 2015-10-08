package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.Person;
import uk.ac.ox.oucs.vle.UserProxy;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.*;

/**
 * Created by buckett on 07/10/15.
 */
public class WithdrawnAdministratorEmailRuleTest extends EmailRuleBase {

    private WithdrawnAdministratorEmailRule rule;

    @Before
    public void setUp() {
        rule = new WithdrawnAdministratorEmailRule();
        rule.setProxy(proxy);
        rule.setService(emailSendingService);
    }

    @Test
    public void testMatches() throws Exception {
        setNewStatus(WITHDRAWN);

        setOldStatus(APPROVED);
        assertTrue(rule.matches(change));

        setOldStatus(ACCEPTED);
        assertTrue(rule.matches(change));

        setOldStatus(CONFIRMED);
        assertTrue(rule.matches(change));
    }

    @Test
    public void testPerform() throws Exception {
        Person admin1 = mock(Person.class);
        when(admin1.getId()).thenReturn("admin-1");
        Person admin2 = mock(Person.class);
        when(admin2.getId()).thenReturn("admin-2");

        CourseGroup group = mock(CourseGroup.class);
        when(group.getAdministrators()).thenReturn(Arrays.asList(admin1, admin2));
        when(signup.getGroup()).thenReturn(group);

        UserProxy current = mock(UserProxy.class);
        when(current.getDisplayName()).thenReturn("Current User");
        when(proxy.getCurrentUser()).thenReturn(current);

        rule.perform(change);

        verify(emailSendingService).sendSignupEmail(eq("admin-1"), eq(signup), anyString(), anyString(), any(Object[].class));
    }
}