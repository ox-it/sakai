package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.CourseDepartment;
import uk.ac.ox.oucs.vle.CourseDepartmentDAO;
import uk.ac.ox.oucs.vle.CourseSignup;

import static uk.ac.ox.oucs.vle.CourseSignupService.*;

/**
 * Stores the state needed for sending out emails.
 */
public class StateChange {
    public Status oldStatus;
    public CourseSignup signup;
    public CourseDepartment department;
    public String placement;

    public StateChange(Status oldStatus, CourseSignup signup, CourseDepartment department, String placement) {
        this.oldStatus = oldStatus;
        this.signup = signup;
        this.department = department;
        this.placement = placement;
    }

    public StateChange() {
    }

    public Status getOldStatus() {
        return oldStatus;
    }

    public CourseSignup getSignup() {
        return signup;
    }

    /**
     * @return <code>null</code> or the department associated with the signup.
     */
    public CourseDepartment getDepartment() {
        return department;
    }

    public String getPlacement() {
        return placement;
    }
}
