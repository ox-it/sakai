package uk.ac.ox.oucs.vle;

import java.util.Set;

/**
 * Created by buckett on 24/09/15.
 */
public class CourseDepartmentImpl implements CourseDepartment {

    private String code;
    private boolean approvalRequired;
    private Set<String> approvers;

    public CourseDepartmentImpl(String code, boolean approvalRequired, Set<String> approvers) {
        this.code = code;
        this.approvalRequired = approvalRequired;
        this.approvers = approvers;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    @Override
    public Set<String> getApprovers() {
        return approvers;
    }
}
