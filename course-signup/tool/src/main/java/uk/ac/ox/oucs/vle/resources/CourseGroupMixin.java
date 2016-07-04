package uk.ac.ox.oucs.vle.resources;

import org.codehaus.jackson.map.annotate.JsonView;
import uk.ac.ox.oucs.vle.CourseCategory;
import uk.ac.ox.oucs.vle.CourseComponent;
import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.Person;
import uk.ac.ox.oucs.vle.resources.Views.*;

import java.util.List;

/**
 * This Mixin is so that we can annotate the CourseGroup interface without having the annotations in the main
 * API as that forces the whole of Jackson into the shared classloader which we want to avoid. This mixin
 * allows a shallow output of the object tree, without walking all the nodes.
 */
public abstract class CourseGroupMixin implements CourseGroup {

    @Override
    @JsonView(Flat.class)
    public abstract int getMuid();

    @Override
    @JsonView(Flat.class)
    public abstract String getCourseId();

    @Override
    @JsonView(Flat.class)
    public abstract String getTitle();

    @Override
    @JsonView(Flat.class)
    public abstract String getDescription();

    @Override
    @JsonView(Flat.class)
    public abstract String getDepartment();

    @Override
    @JsonView(Flat.class)
    public abstract String getDepartmentCode();

    @Override
    @JsonView(Flat.class)
    public abstract String getSubUnit();

    @Override
    @JsonView(Flat.class)
    public abstract String getSubUnitCode();

    @Override
    @JsonView(Flat.class)
    public abstract boolean getSupervisorApproval();

    @Override
    @JsonView(Flat.class)
    public abstract boolean getAdministratorApproval();

    @Override
    @JsonView(Flat.class)
    public abstract boolean getHideGroup();

    @Override
    @JsonView(Flat.class)
    public abstract String getContactEmail();

    @Override
    @JsonView(Flat.class)
    public abstract String getVisibility();

    @Override
    @JsonView(Flat.class)
    public abstract String getPrerequisite();

    @Override
    @JsonView(Flat.class)
    public abstract String getRegulations();

    @Override
    @JsonView(Flat.class)
    public abstract String getSource();

    @Override
    public abstract List<CourseComponent> getComponents();

    @Override
    public abstract List<CourseCategory> getCategories();

    @Override
    public abstract List<CourseCategory> getCategories(CategoryType categoryType);

    @Override
    public abstract List<Person> getAdministrators();

    @Override
    public abstract List<Person> getSuperusers();

    @Override
    @JsonView(Flat.class)
    public abstract List<String> getOtherDepartments();

    @Override
    @JsonView(Flat.class)
    public abstract boolean getIsAdmin();

    @Override
    @JsonView(Flat.class)
    public abstract boolean getIsSuperuser();

}
