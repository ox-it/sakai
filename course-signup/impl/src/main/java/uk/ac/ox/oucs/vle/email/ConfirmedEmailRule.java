package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.CONFIRMED;

/**
 * Created by buckett on 02/10/15.
 */
public class ConfirmedEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getSignup().getStatus(), CONFIRMED);
    }

    @Override
    public void perform(StateChange stateChange) {
        String url = proxy.getMyUrl(stateChange.getPlacement());
        service.sendStudentSignupEmail(stateChange.getSignup(),
                "confirmed.student.subject",
                "confirmed.student.body",
                new Object[]{url});
    }
}
