package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;
import uk.ac.ox.oucs.vle.Person;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.PENDING;

/**
 * Send out an an email to course administrators when a new signup is made.
 * Also send out an email to the student informing them about the signup.
 */
public class PendingEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        return (stateChange.getOldStatus() == null)
            && Status.matches(stateChange.getSignup().getStatus(), PENDING);
    }

    @Override
    public void perform(StateChange stateChange) {
        CourseSignup signup = stateChange.getSignup();
        CourseGroup group = signup.getGroup();

        String advanceUrl = proxy.getAdvanceUrl(signup.getId(), "accept", null);
        String url = proxy.getConfirmUrl(signup.getId());
        for (Person administrator : group.getAdministrators()) {
            service.sendSignupEmail(
                    administrator.getId(), signup,
                    "signup.admin.subject",
                    "signup.admin.body",
                    new Object[]{url, advanceUrl});
            service.savePlacement(administrator.getId(), stateChange.getPlacement());
        }

        String myUrl = proxy.getMyUrl();
        service.sendStudentSignupEmail(signup,
                "signup.student.subject",
                "signup.student.body",
                new Object[]{myUrl});

    }
}
