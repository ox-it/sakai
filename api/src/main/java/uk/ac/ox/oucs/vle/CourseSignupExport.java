package uk.ac.ox.oucs.vle;

/**
 * Holds the signup and group that were made against a component.
 * This class exists so that we know we have loaded both objects and don't
 * have to go back to hibernate when wanting details of the group associated with the signup.
 */
public class CourseSignupExport {
    private CourseSignup signup;
    private CourseGroup group;

    public CourseSignupExport(CourseSignup signup, CourseGroup group) {
        this.signup = signup;
        this.group = group;
    }

    public CourseSignup getSignup() {
        return signup;
    }

    public CourseGroup getGroup() {
        return group;
    }
}
