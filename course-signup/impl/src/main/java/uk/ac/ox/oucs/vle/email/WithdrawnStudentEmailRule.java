package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.WITHDRAWN;

/**
 * Whenever the student is withdrawn from an module email them.
 */
public class WithdrawnStudentEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getSignup().getStatus(), WITHDRAWN);
    }

    @Override
    public void perform(StateChange stateChange) {
        service.sendStudentSignupEmail(stateChange.getSignup(),
                "withdraw.student.subject",
                "withdraw.student.body",
                new Object[]{proxy.getCurrentUser().getDisplayName(), proxy.getMyUrl()});

    }
}
