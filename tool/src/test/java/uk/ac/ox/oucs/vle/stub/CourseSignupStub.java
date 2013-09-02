package uk.ac.ox.oucs.vle.stub;

import uk.ac.ox.oucs.vle.CourseComponent;
import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.CourseSignup;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;
import uk.ac.ox.oucs.vle.Person;

import java.util.Date;
import java.util.Set;

/**
 * The stubs are created as we can't send mocks to Jackson as it's reflection doesn't work with them.
 * We can't use the implementations out of the impl as they aren't available (dependencies).
 *
 * @author Matthew Buckett
 */
public class CourseSignupStub implements CourseSignup{

	private String id;
	private Person user;
	private Person supervisor;
	private String notes;
	private Status status;
	private Date created;
	private Set<CourseComponent> components;
	private String department;
	private CourseGroup group;


	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public CourseGroup getGroup() {
		return group;
	}

	public void setGroup(CourseGroup group) {
		this.group = group;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = user;
	}

	public Person getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(Person supervisor) {
		this.supervisor = supervisor;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Set<CourseComponent> getComponents() {
		return components;
	}

	public void setComponents(Set<CourseComponent> components) {
		this.components = components;
	}
}
