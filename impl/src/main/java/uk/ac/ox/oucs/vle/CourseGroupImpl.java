package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CourseGroupImpl implements CourseGroup {

	private CourseGroupDAO courseGroupDAO;
	private CourseSignupServiceImpl impl;
	private List<CourseComponent> components;
	
	public CourseGroupImpl(CourseGroupDAO courseGroupDAO, CourseSignupServiceImpl impl) {
		this.courseGroupDAO = courseGroupDAO;
		this.impl = impl;
	}

	public String getDescription() {
		return courseGroupDAO.getDescription();
	}

	public String getId() {
		return courseGroupDAO.getId();
	}

	public String getTitle() {
		return courseGroupDAO.getTitle();
	}

	public String getDepartment() {
		return courseGroupDAO.getDepartmentName();
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
		
		Collections.sort(components, new Comparator<CourseComponent>() {
			public int compare(CourseComponent c1,CourseComponent c2) {
				return c1.getId().compareTo(c2.getId());
			}
		});
		
		return components;
	}

	public Person getAdministrator() {
		UserProxy user = impl.loadUser(courseGroupDAO.getAdministrator());
		return (user != null)? new PersonImpl(user.getId(), user.getName(), user.getEmail(), Collections.EMPTY_LIST, null, user.getType()):null;
	}

}
