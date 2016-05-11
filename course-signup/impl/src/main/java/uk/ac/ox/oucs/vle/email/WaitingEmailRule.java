package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.WAITING;

/**
 * This looks all wrong and shouldn't be used as the FullEmailRule looks to do the same
 * thing as this.
 */
public class WaitingEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getSignup().getStatus(), WAITING);
    }

    @Override
    public void perform(StateChange stateChange) {
        service.sendStudentSignupEmail(stateChange.getSignup(),
                "withdraw.student.subject",
                "withdraw.student.body",
                new Object[]{proxy.getCurrentUser().getDisplayName(), proxy.getMyUrl()});
    }
}
