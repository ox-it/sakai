package uk.ac.ox.oucs.vle;

import java.util.Date;

public class CourseComponentSessionImpl implements CourseComponentSession {
	
	private CourseComponentSessionDAO dao;

	public CourseComponentSessionImpl(CourseComponentSessionDAO dao) {
		this.dao = dao;
	}

	public int getCourceComponentMuid() {
		return dao.getCourseComponentMuid();
	}
	
	public String getSessionId() {
		return dao.getSessionId();
	}

	public Date getSessionStart() {
		return dao.getSessionStart();
	}

	public String getSessionStartText() {
		return dao.getSessionStartText();
	}

	public Date getSessionEnd() {
		return dao.getSessionEnd();
	}

	public String getSessionEndText() {
		return dao.getSessionEndText();
	}

	

}
