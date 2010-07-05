package uk.ac.ox.oucs.vle;

import java.util.Date;

import uk.ac.ox.oucs.vle.proxy.User;

public class CourseComponentImpl implements CourseComponent {
	
	private CourseComponentDAO dao;
	
	public CourseComponentImpl(CourseComponentDAO dao) {
		this.dao = dao;
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
	
	public Person getAdministrator() {
		String adminId = dao.getAdministrator();
		User user = impl.loadUser(adminId);
		return (user != null)?new PersonImpl(user.getId(), user.getName(), user.getEmail()):null;
	}
	

	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getOpens() {
		return dao.getOpens();
	}

	public Date getCloses() {
		return dao.getCloses();
	}

	public String getComponentSet() {
		return dao.getComponentId(); 
	}

}
