package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.*;

/**
 * Rule for when someone is rejected from attending a module when they were accepted.
 */
public class RejectedAcceptedEmailRule extends EmailRule {

    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getOldStatus(), ACCEPTED) &&
                Status.matches(stateChange.getSignup().getStatus(), REJECTED);
    }

    @Override
    public void perform(StateChange stateChange) {
        CourseSignup signup = stateChange.getSignup();
        String placementId = stateChange.getPlacement();
        if (signup.getSupervisor() == null) {
            service.sendStudentSignupEmail(
                    signup,
                    "reject-admin.student.subject",
                    "reject-admin.student.body",
                    new Object[] {proxy.getCurrentUser().getDisplayName(), proxy.getMyUrl(placementId)});
        } else {
            // This may tell the user that the supervisor rejected them, when infact the user might have
            // been the administrator at the time.
            service.sendStudentSignupEmail(
                    signup,
                    "reject-supervisor.student.subject",
                    "reject-supervisor.student.body",
                    new Object[] {signup.getSupervisor().getName(), proxy.getMyUrl(placementId)});
        }
        service.savePlacement(stateChange.getSignup().getUser().getId(), stateChange.getPlacement());

    }

}
