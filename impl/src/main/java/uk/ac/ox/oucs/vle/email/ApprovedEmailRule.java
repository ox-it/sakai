package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseDepartment;
import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.APPROVED;

/**
 * When a signup is approved an email goes out asking for department approvers to
 * allow the signup.
 */
public class ApprovedEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        return Status.matches(stateChange.getSignup().getStatus(), APPROVED);
    }

    @Override
    public void perform(StateChange stateChange) {
        String signupId = stateChange.getSignup().getId();
        String placementId = stateChange.getPlacement();
        CourseSignup signup = stateChange.getSignup();
        CourseDepartment department= stateChange.getDepartment();

        for (String approverId : department.getApprovers()) {
            String url = proxy.getApproveUrl(signupId, placementId);
            String advanceUrl = proxy.getAdvanceUrl(signup.getId(), "confirm", placementId);
            if (approverId != null) {
                service.sendSignupEmail(
                        approverId, signup,
                        "approval.supervisor.subject",
                        "approval.supervisor.body",
                        new Object[]{url, advanceUrl});
                service.savePlacement(approverId, placementId);
            }
        }


    }
}
