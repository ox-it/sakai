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

}
