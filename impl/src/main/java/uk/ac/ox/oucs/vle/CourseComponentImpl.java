package uk.ac.ox.oucs.vle;

import java.util.Date;

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

	public String getPresenter() {
		return null;
	}

	public String getPresenterEmail() {
		// TODO Auto-generated method stub
		return null;
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
