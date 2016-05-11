package uk.ac.ox.oucs.vle;

import java.util.Set;

/**
 * Details of a department that has students going on courses.
 */
public interface CourseDepartment {

    String getCode();
    boolean isApprovalRequired();
    Set<String> getApprovers();

}
