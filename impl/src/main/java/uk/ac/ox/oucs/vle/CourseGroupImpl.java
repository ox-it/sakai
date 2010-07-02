package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;

public class CourseGroupImpl implements CourseGroup {

	private CourseGroupDAO courseGroupDAO;
	private CourseDAO dao;
	private List<CourseComponent> components;
	
	public CourseGroupImpl(CourseGroupDAO courseGroupDAO, CourseDAO dao) {
		this.courseGroupDAO = courseGroupDAO;
		this.dao = dao;
	}

	public String getDescription() {
		return getProperties().get("description");
	}

	private Map<String, String> getProperties() {
		return courseGroupDAO.getProperties();
	}

	public String getId() {
		return courseGroupDAO.getId();
	}

	public int getSize() {
		
		// Calculated.
		return 0;
	}

	public String getTitle() {
		return courseGroupDAO.getTitle();
	}

	public String getDepartmentCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CourseComponent> getComponents() {
		if (components == null) {
			components = new ArrayList<CourseComponent>();
			for(CourseComponentDAO component:  courseGroupDAO.getComponents()) {
				components.add(new CourseComponentImpl(component));
			}
		}
		return components;
	}

}
