package uk.ac.ox.oucs.vle;

public class CourseCategoryImpl implements CourseCategory {
	
	private CourseCategoryDAO dao;

	public CourseCategoryImpl(CourseCategoryDAO dao) {
		this.dao = dao;
	}

	public String getType() {
		return dao.getCategoryType();
	}

	public String getCode() {
		return dao.getCategoryCode();
	}

	public String getName() {
		return dao.getCategoryName();
	}

}
