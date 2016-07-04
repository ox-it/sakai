package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;
import uk.ac.ox.oucs.vle.Person;

import static uk.ac.ox.oucs.vle.CourseSignupService.Status.WAITING;

/**
 * When a signup is made and the user is placed onto the waiting list send an email
 * to the administrator of the course and to the signup.
 */
public class FullEmailRule extends EmailRule {
    @Override
    public boolean matches(StateChange stateChange) {
        // No old status as it's a new signup.
        return stateChange.getOldStatus() == null
            && Status.matches(stateChange.getSignup().getStatus(), WAITING);
    }

    @Override
    public void perform(StateChange stateChange) {
        CourseSignup signup = stateChange.getSignup();
        CourseGroup group = signup.getGroup();
        for (Person administrator : group.getAdministrators()) {
            service.sendSignupEmail(
                    administrator.getId(), signup,
                    "waiting.admin.subject",
                    "waiting.admin.body",
                    new Object[]{proxy.getAdminUrl()});
            service.savePlacement(administrator.getId(), stateChange.getPlacement());
        }

        String myUrl = proxy.getMyUrl();
        service.sendStudentSignupEmail(signup,
                "waiting.student.subject",
                "waiting.student.body",
                new Object[]{myUrl});

    }
}
