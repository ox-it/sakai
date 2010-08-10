package uk.ac.ox.oucs.vle;

import java.util.Date;

import uk.ac.ox.oucs.vle.proxy.UserProxy;

public class CourseComponentImpl implements CourseComponent {
	
	private CourseComponentDAO dao;
	private CourseSignupServiceImpl impl;
	
	/// Local caches.
	private transient Date opens;
	private transient Date closes;
	
	public CourseComponentImpl(CourseComponentDAO dao, CourseSignupServiceImpl impl) {
		this.dao = dao;
		this.impl = impl;
	}

	public String getId() {
		return dao.getId();
	}

	public String getTitle() {
		return dao.getTitle();
	}

	public int getPlaces() {
		return dao.getSize() - dao.getTaken();
	}

	public int getSize() {
		return dao.getSize();
	}

	public Person getPresenter() {
		if (dao.getProperties().containsKey("teacher.name")) {
			return new PersonImpl(null, dao.getProperties().get("teacher.name"), dao.getProperties().get("teacher.email"));
		}
		return null;
	}
	
	public String getLocation() {
		return dao.getProperties().get("location");
	}

	public Date getOpens() {
		// Jackson doesn't like java.sql.Date.
		if (opens == null)
			opens = new Date(dao.getOpens().getTime());
		return opens;
	}

	public Date getCloses() {
		// Jackson doesn't like java.sql.Date.
		if(closes == null)
			closes = new Date(dao.getCloses().getTime());
		return dao.getCloses();
	}

	public String getComponentSet() {
		return dao.getComponentId(); 
	}

	public String getWhen() {
		return dao.getProperties().get("when");
	}

	public String getSlot() {
		return dao.getProperties().get("slot");
	}

	public String getSessions() {
		return dao.getProperties().get("sessions");
	}

}
