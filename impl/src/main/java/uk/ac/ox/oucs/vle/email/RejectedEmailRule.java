package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;
import uk.ac.ox.oucs.vle.Person;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.*;

/**
 * Rule for when someone is rejected from attending a module.
 */
public class RejectedEmailRule extends EmailRule {

    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getOldStatus(), PENDING, WAITING) &&
                Status.matches(stateChange.getSignup().getStatus(), REJECTED);
    }

    @Override
    public void perform(StateChange stateChange) {
        service.sendStudentSignupEmail(
                stateChange.getSignup(),
                "reject-admin.student.subject",
                "reject-admin.student.body",
                new Object[] {proxy.getCurrentUser().getDisplayName(), proxy.getMyUrl(stateChange.getPlacement())});
        Person user = stateChange.getSignup().getUser();
        service.savePlacement(user.getId(), stateChange.getPlacement());

    }

}
