package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupImpl;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.Person;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.*;

/**
 * Sends an email to the supervisor about the signup.
 */
public class AcceptedEmailRule extends EmailRule {

    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getOldStatus(), PENDING, WAITING) &&
            Status.matches(stateChange.getSignup().getStatus(),ACCEPTED);
    }

    @Override
    public void perform(StateChange stateChange) {
        CourseSignup signup = stateChange.getSignup();
        Person supervisor = signup.getSupervisor();
        if (supervisor == null) {
            throw new IllegalStateException("Signup without a supervisor can't get approval");
        }
        String placementId = stateChange.getPlacement();
        String url = proxy.getConfirmUrl(signup.getId(), placementId);
        String advanceUrl = proxy.getAdvanceUrl(signup.getId(), "approve", placementId);
        service.sendSignupEmail(supervisor.getId(), signup,
                "approval.supervisor.subject",
                "approval.supervisor.body",
                new Object[]{url, advanceUrl});
        service.savePlacement(supervisor.getId(), placementId);
    }
}
