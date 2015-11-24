package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold the signups found for a component.
 */
public class CourseComponentExport {

    private CourseComponent component;
    private List<CourseSignupExport> signups = new ArrayList<>();

    public CourseComponentExport(CourseComponent component) {
        this.component = component;
    }

    public CourseComponent getComponent() {
        return component;
    }

    public List<CourseSignupExport> getSignups() {
        return signups;
    }

    public void addSignup(CourseSignupExport signup) {
        signups.add(signup);
    }

    /**
     * Holds the signup and group that were made against a component.
     * This class exists so that we know we have loaded both objects and don't
     * have to go back to hibernate when wanting details of the group associated with the signup.
     */
    public static class CourseSignupExport {
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
}
