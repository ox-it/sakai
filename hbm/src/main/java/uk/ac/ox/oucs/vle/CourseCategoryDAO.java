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
