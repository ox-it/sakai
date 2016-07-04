package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;
import uk.ac.ox.oucs.vle.Person;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.*;

/**
 * This is for when a student withdraws from a course, an email is send to all
 * the administrators.
 */
public class WithdrawnAdministratorEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getOldStatus(), ACCEPTED, APPROVED, CONFIRMED)
                && Status.matches(stateChange.getSignup().getStatus(), WITHDRAWN);
    }

    @Override
    public void perform(StateChange stateChange) {
        CourseSignup signup = stateChange.getSignup();

        for (Person administrator : signup.getGroup().getAdministrators()) {
            service.sendSignupEmail(
                    administrator.getId(), signup,
                    "withdraw.admin.subject",
                    "withdraw.admin.body",
                    new Object[]{proxy.getCurrentUser().getDisplayName(), proxy.getAdminUrl()});
            service.savePlacement(administrator.getId(), proxy.getCurrentPlacementId());
        }

    }
}
