package uk.ac.ox.oucs.vle;

import java.util.List;

/**
 * Just a wrapper for a UserProxy, this is needed so that lazily loaded data on the
 * userProxy remains lazy in the Person. Copying all the data from UserProxy to Person
 * resulted in losing any lazy loading gains.
 */
public class UserProxyPersonImpl implements Person {

    private final UserProxy userProxy;
    private final CourseSignupService service;

    public UserProxyPersonImpl(UserProxy userProxy, CourseSignupService service) {
        this.userProxy = userProxy;
        this.service = service;
    }

    @Override
    public String getId() {
        return userProxy.getId();
    }

    @Override
    public String getFirstName() {
        return userProxy.getFirstName();
    }

    @Override
    public String getLastName() {
        return userProxy.getLastName();
    }

    @Override
    public String getName() {
        return userProxy.getDisplayName();
    }

    @Override
    public String getEmail() {
        return userProxy.getEmail();
    }

    @Override
    public List<String> getUnits() {
        return userProxy.getUnits();
    }

    @Override
    public String getWebauthId() {
        return userProxy.getWebauthId();
    }

    @Override
    public String getOssId() {
        return userProxy.getOssId();
    }

    @Override
    public String getYearOfStudy() {
        return userProxy.getYearOfStudy();
    }

    @Override
    public String getDegreeProgram() {
        return userProxy.getDegreeProgram();
    }

    @Override
    public String getDepartmentName() {
        String departmentName = null;
        // If this isn't a Sakai person we won't have a good primaryOrgUnit
        if (service != null) {
            String primaryOrgUnit = userProxy.getPrimaryOrgUnit();
            if (primaryOrgUnit != null) {
                Department department = service.findPracDepartment(primaryOrgUnit);
                if (null != department) {
                    departmentName = department.getName();
                }
            }
        }
        return departmentName;
    }

    @Override
    public String getType() {
        return userProxy.getType();
    }
}
