/*
 * #%L
 * Course Signup Implementation
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

import java.util.*;

public class CourseGroupImpl implements CourseGroup {

	private CourseGroupDAO courseGroupDAO;
	private CourseSignupServiceImpl impl;
	private List<CourseComponent> components;
	private List<CourseCategory> categories;
	private List<Person> administrators;
	private List<Person> superusers;
	private List<String> otherDepartments;
	
	public CourseGroupImpl(CourseGroupDAO courseGroupDAO, CourseSignupServiceImpl impl) {
		this.courseGroupDAO = courseGroupDAO;
		this.impl = impl;
	}

	public CourseGroupImpl(CourseGroupDAO courseGroupDAO, CourseSignupServiceImpl impl, List<CourseComponentDAO> courseComponentDAOs) {
		this.courseGroupDAO = courseGroupDAO;
		this.impl = impl;
		setComponents(courseComponentDAOs);
	}


	public int getMuid() {
		return courseGroupDAO.getMuid();
	}
	
	public String getDescription() {
		return courseGroupDAO.getDescription();
	}

	public String getCourseId() {
		return courseGroupDAO.getCourseId();
	}

	public String getTitle() {
		return courseGroupDAO.getTitle();
	}

	public String getDepartment() {
		return courseGroupDAO.getDepartmentName();
	}

	public String getDepartmentCode() {
		return courseGroupDAO.getDept();
	}
	
	public String getSubUnit() {
		return courseGroupDAO.getSubunitName();
	}

	public String getSubUnitCode() {
		return courseGroupDAO.getSubunit();
	}
	
	public boolean getSupervisorApproval() {
		return courseGroupDAO.getSupervisorApproval();
	}
	
	public boolean getAdministratorApproval() {
		return courseGroupDAO.getAdministratorApproval();
	}

	/**
	 * @inheritDoc
	 */
	public boolean getHideGroup() {
		return courseGroupDAO.getHideGroup();
	}
	
	public String getContactEmail() {
		return courseGroupDAO.getContactEmail();
	}
	
	public String getVisibility() {
		return courseGroupDAO.getVisibility();
	}
	
	public String getPrerequisite() {
		return courseGroupDAO.getPrerequisite();
	}
	
	public String getRegulations() {
		return courseGroupDAO.getRegulations();
	}
	
	public String getSource() {
		return courseGroupDAO.getSource();
	}

	public List<CourseComponent> getComponents() {
		if (components == null) {
			setComponents(courseGroupDAO.getComponents());
		}
		return components;
	}

	private void setComponents(Collection<CourseComponentDAO> courseComponentDAOs) {
		components = new ArrayList<CourseComponent>();
		for (CourseComponentDAO component : courseComponentDAOs) {
			components.add(new CourseComponentImpl(component));
		}

		Collections.sort(components, new Comparator<CourseComponent>() {
			public int compare(CourseComponent c1, CourseComponent c2) {
				if (null != c1.getStarts() && null != c2.getStarts()) {
					return c1.getStarts().compareTo(c2.getStarts());
				}
				if (null != c1.getCloses() && null != c2.getCloses()) {
					return c1.getCloses().compareTo(c2.getCloses());
				}
				return c1.getPresentationId().compareTo(c2.getPresentationId());
			}
		});
	}

	public List<Person> getAdministrators() {
		if (administrators == null) {
			administrators = new ArrayList<Person>();
			for (String component:  courseGroupDAO.getAdministrators()) {
				UserProxy user = impl.loadUser(component);
				if (null != user) {
					String departmentName = null;
					if (null != user.getPrimaryOrgUnit()) {
						Department department = impl.findPracDepartment(user.getPrimaryOrgUnit());
						if (null != department) {
							departmentName = department.getName();
						}
					}
					Person person = new PersonImpl(user.getId(), 
							user.getFirstName(), user.getLastName(), user.getDisplayName(), 
							user.getEmail(), Collections.<String>emptyList(), 
							user.getWebauthId(), user.getOssId(), null, null,
							departmentName, user.getType());
					administrators.add(person);
				}
			}
		}
		
		return administrators;
	}
	
	public List<Person> getSuperusers() {
		if (superusers == null) {
			superusers = new ArrayList<Person>();
			for (String component:  courseGroupDAO.getSuperusers()) {
				UserProxy user = impl.loadUser(component);
				if (null != user) {
					String departmentName = null;
					if (null != user.getPrimaryOrgUnit()) {
						Department department = impl.findPracDepartment(user.getPrimaryOrgUnit());
						if (null != department) {
							departmentName = department.getName();
						}
					}
					Person person = new PersonImpl(user.getId(), 
							user.getFirstName(), user.getLastName(), user.getDisplayName(), 
							user.getEmail(), Collections.<String>emptyList(), 
							user.getWebauthId(), user.getOssId(), null, null,
							departmentName, user.getType());
					superusers.add(person);
				}
			}
		}
		
		return superusers;
	}
	
	public List<String> getOtherDepartments() {
		if (otherDepartments == null) {
			otherDepartments = new ArrayList<String>();
			for (String otherDepartment:  courseGroupDAO.getOtherDepartments()) {
				otherDepartments.add(otherDepartment);
			}
		}
		return otherDepartments;
	}
	
	public boolean getIsAdmin() {
		boolean isAdmin = impl.isAdministrator(courseGroupDAO.getAdministrators());
		if (!isAdmin) {
			isAdmin = impl.isAdministrator(courseGroupDAO.getSuperusers());
		}
		return isAdmin;
	}
	
	public boolean getIsSuperuser() {
		return impl.isAdministrator(courseGroupDAO.getSuperusers());
	}

	public List<CourseCategory> getCategories() {
		if (categories == null) {
			categories = new ArrayList<CourseCategory>();
			for(CourseCategoryDAO category:  courseGroupDAO.getCategories()) {
				categories.add(new CourseCategoryImpl(category));
			}
		
			Collections.sort(categories, new Comparator<CourseCategory>() {
				public int compare(CourseCategory c1,CourseCategory c2) {
					return c1.getName().compareTo(c2.getName());
				}
			});
		}
		return categories;
	}
	
	public List<CourseCategory> getCategories(CategoryType categoryType) {
		List<CourseCategory> cats = new ArrayList<CourseCategory>();
		for(CourseCategory category:  getCategories()) {
			if (category.getType().equals(categoryType.name())) {
				cats.add(category);
			}
		}
		
		Collections.sort(cats, new Comparator<CourseCategory>() {
			public int compare(CourseCategory c1,CourseCategory c2) {
				return c1.getName().compareTo(c2.getName());
			}
		});
		return cats;
	}

}
