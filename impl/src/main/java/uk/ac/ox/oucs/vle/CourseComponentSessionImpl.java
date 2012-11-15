package uk.ac.ox.oucs.vle;

import java.util.Date;

public class CourseComponentSessionImpl implements CourseComponentSession {
	
	private CourseComponentSessionDAO dao;

	public CourseComponentSessionImpl(CourseComponentSessionDAO dao) {
		this.dao = dao;
	}

	public String getCourceComponentId() {
		return dao.getCourseComponentId();
	}

	public Date getSessionStart() {
		return dao.getSessionStart();
	}

	public String getSessionStartText() {
		return dao.getSessionStartText();
	}

	public Date getEndStart() {
		return dao.getSessionEnd();
	}

	public String getSessionEndText() {
		return dao.getSessionEndText();
	}

	

}
