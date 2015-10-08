package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.APPROVED;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.REJECTED;

/**
 * Created by buckett on 02/10/15.
 */
public class RejectedApprovedEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getOldStatus(), APPROVED) &&
                Status.matches(stateChange.getSignup().getStatus(), REJECTED);
    }

    @Override
    public void perform(StateChange stateChange) {
        service.sendStudentSignupEmail(
                stateChange.getSignup(),
                "reject-approver.student.subject",
                "reject-approver.student.body",
                new Object[] {proxy.getCurrentUser().getDisplayName(), proxy.getMyUrl(stateChange.getPlacement())});
    }
}
