package uk.ac.ox.oucs.vle.email;

import org.junit.Before;
import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.SakaiProxy;
import uk.ac.ox.oucs.vle.UserPlacementDAO;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base test class for testing email rules.
 */
public class EmailRuleBase {

    protected SakaiProxy proxy;

    protected CourseSignup signup;

    protected UserPlacementDAO placementDAO;

    protected EmailSendingService emailSendingService;

    protected StateChange change;

    @Before
    public final void setUpBase() {
        proxy = mock(SakaiProxy.class);
        signup = mock(CourseSignup.class);
        placementDAO = mock(UserPlacementDAO.class);
        emailSendingService = mock(EmailSendingService.class);
        change = new StateChange();
        change.signup = signup;
    }

    public void setNewStatus(CourseSignupService.Status status) {
        when(signup.getStatus()).thenReturn(status);
    }

    public void setOldStatus(CourseSignupService.Status status) {
        change.oldStatus = status;
    }
}
