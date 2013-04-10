package uk.ac.ox.oucs.vle;

import java.util.HashSet;
import java.util.Set;


public class CourseCategoryDAO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String categoryId;
	private String categoryName;
	private String categoryType;
	private Set<CourseGroupDAO> groups = new HashSet<CourseGroupDAO>(0);

	public CourseCategoryDAO() {
	}

	public CourseCategoryDAO(CourseGroup.Category_Type type, String id, String name) {
		this.categoryType = type.name();
		this.categoryId = id;
		this.categoryName = name;
	}

	public String getCategoryId() {
		return this.categoryId;
	}

	public void setCategoryId(String id) {
		this.categoryId = id;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(String name) {
		this.categoryName = name;
	}

	public String getCategoryType() {
		return this.categoryType;
	}

	public void setCategoryType(String type) {
		this.categoryType = type;
	}

	public Set<CourseGroupDAO> getGroups() {
		return this.groups;
	}

	public void setGroups(Set<CourseGroupDAO> groups) {
		this.groups = groups;
	}
}
