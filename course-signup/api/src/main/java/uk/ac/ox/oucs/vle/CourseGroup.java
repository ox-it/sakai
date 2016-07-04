/*
 * #%L
 * Course Signup API
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

import java.util.List;

public interface CourseGroup {

	//RM Research Methods
	//RDF Skills
	public static enum CategoryType {
		/**
		 * The Oxford RDF skills.
		 */
		RDF,
		/**
		 * The JACS subjects recommended by JISC.
		 */
		JACS,
		/**
		 * This defines if the course is qualitative or quantitative.
		 */
		RM,
		/**
		 * The Vitae RDF domain skills
		 */
		VITAE
	};
	
	public int getMuid();
		
	public String getCourseId();
	
	public String getTitle();
	
	public String getDescription();
	
	public String getDepartment();
	
	public String getDepartmentCode();
	
	public String getSubUnit();
	
	public String getSubUnitCode();
	
	public boolean getSupervisorApproval();
	
	public boolean getAdministratorApproval();

	/**
	 * Should this course group be hidden.
	 * Hidden groups are ones that are present in feed but shouldn't be findable any more to users.
	 * @return <code>true</code> if the course group should be hidden.
	 */
	public boolean getHideGroup();
	
	public String getContactEmail();
	
	public String getVisibility();
	
	public String getPrerequisite();
	
	public String getRegulations();
	
	public String getSource();
	
	public List<CourseComponent> getComponents();
	
	public List<CourseCategory> getCategories();
	
	public List<CourseCategory> getCategories(CategoryType categoryType);
	
	public List<Person> getAdministrators();
	
	public List<Person> getSuperusers();
	
	public List<String> getOtherDepartments();

	/**
	 * Is the current user an admin for this group.
	 * @deprecated This shouldn't be here as it means that the coursegroup object is specific to each user
	 * and can't be easily cached.
	 */
	public boolean getIsAdmin();

	/**
	 * Is the current user a superuser for this group.
	 * @deprecated This shouldn't be here as it means that the coursegroup object is specific to each user.
	 * and can't be easily cached.
	 */
	public boolean getIsSuperuser();
}
