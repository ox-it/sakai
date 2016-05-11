/*
 * #%L
 * Course Signup Hibernate
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;


public class CourseCategoryDAO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String categoryId;
	private String categoryName;
	private String categoryType;

	public CourseCategoryDAO() {
	}

	public CourseCategoryDAO(CourseGroup.CategoryType type, String id, String name) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CourseCategoryDAO that = (CourseCategoryDAO) o;

		if (categoryId != null ? !categoryId.equals(that.categoryId) : that.categoryId != null) return false;
		if (categoryName != null ? !categoryName.equals(that.categoryName) : that.categoryName != null) return false;
		if (categoryType != null ? !categoryType.equals(that.categoryType) : that.categoryType != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = categoryId != null ? categoryId.hashCode() : 0;
		result = 31 * result + (categoryName != null ? categoryName.hashCode() : 0);
		result = 31 * result + (categoryType != null ? categoryType.hashCode() : 0);
		return result;
	}
}
