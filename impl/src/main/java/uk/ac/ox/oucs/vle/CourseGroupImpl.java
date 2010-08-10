package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.sakaiproject.user.api.User;

import uk.ac.ox.oucs.vle.proxy.UserProxy;

public class CourseGroupImpl implements CourseGroup {

	private CourseGroupDAO courseGroupDAO;
	private CourseSignupServiceImpl impl;
	private List<CourseComponent> components;
	
	public CourseGroupImpl(CourseGroupDAO courseGroupDAO, CourseSignupServiceImpl impl) {
		this.courseGroupDAO = courseGroupDAO;
		this.impl = impl;
	}

	public String getDescription() {
		return getProperties().get("desc");
	}

	private Map<String, String> getProperties() {
		return courseGroupDAO.getProperties();
	}

	public String getId() {
		return courseGroupDAO.getId();
	}

	public String getTitle() {
		return courseGroupDAO.getTitle();
	}

	public String getDepartment() {
		return courseGroupDAO.getProperties().get("department");
	}

	public String getDepartmentCode() {
		return courseGroupDAO.getDept();
	}

	public List<CourseComponent> getComponents() {
		if (components == null) {
			components = new ArrayList<CourseComponent>();
			for(CourseComponentDAO component:  courseGroupDAO.getComponents()) {
				components.add(new CourseComponentImpl(component, impl));
			}
		}
		return components;
	}

	public Person getAdministrator() {
		UserProxy user = impl.loadUser(courseGroupDAO.getAdministrator());
		return (user != null)? new PersonImpl(user.getId(), user.getName(), user.getEmail()):null;
	}

}
