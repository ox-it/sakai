package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.List;

public class CourseGroupImpl implements CourseGroup {

	private CourseGroupDAO courseGroupDAO;
	private CourseDAO dao;
	
	public CourseGroupImpl(CourseGroupDAO courseGroupDAO, CourseDAO dao) {
		this.courseGroupDAO = courseGroupDAO;
		this.dao = dao;
	}

	public String getDescription() {
		return courseGroupDAO.getProperties().get("description");
	}

	public String getId() {
		return courseGroupDAO.getId();
	}

	public int getSize() {
		
		// Calculated.
		return 0;
	}

	protected void getOpenComponents(Date at) {
		// This should filter the sets so we only get ones for this course.
		List<CourseComponentDAO> courseComponentDAOs = dao.findOpenComponents(courseGroupDAO.getId(), at);
		for (CourseComponentDAO component: courseComponentDAOs) {
			
		}
	}

	public String getTitle() {
		return courseGroupDAO.getTitle();
	}

	public String getDepartmentCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CourseComponent> getComponents() {
		// TODO Auto-generated method stub
		return null;
	}

}
