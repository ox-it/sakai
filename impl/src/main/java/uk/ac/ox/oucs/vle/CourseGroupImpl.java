package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CourseGroupImpl implements CourseGroup {

	private CourseGroupDAO courseGroupDAO;
	private CourseSignupServiceImpl impl;
	private List<CourseComponent> components;
	private List<Person> administrators;
	private List<Person> superusers;
	private List<String> otherDepartments;
	
	public CourseGroupImpl(CourseGroupDAO courseGroupDAO, CourseSignupServiceImpl impl) {
		this.courseGroupDAO = courseGroupDAO;
		this.impl = impl;
	}

	public String getDescription() {
		return courseGroupDAO.getDescription();
	}

	public String getId() {
		return courseGroupDAO.getId();
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
	
	public boolean getPublicView() {
		return courseGroupDAO.getPublicView();
	}
	
	public boolean getSupervisorApproval() {
		return courseGroupDAO.getSupervisorApproval();
	}
	
	public boolean getAdministratorApproval() {
		return courseGroupDAO.getAdministratorApproval();
	}
	
	public boolean getHomeApproval() {
		return courseGroupDAO.getHomeApproval();
	}
	
	public String getContactEmail() {
		return courseGroupDAO.getContactEmail();
	}

	public List<CourseComponent> getComponents() {
		if (components == null) {
			components = new ArrayList<CourseComponent>();
			for(CourseComponentDAO component:  courseGroupDAO.getComponents()) {
				components.add(new CourseComponentImpl(component, impl));
			}
		}
		
		Collections.sort(components, new Comparator<CourseComponent>() {
			public int compare(CourseComponent c1,CourseComponent c2) {
				return c1.getCloses().compareTo(c2.getCloses());
			}
		});
		
		return components;
	}

	public List<Person> getAdministrators() {
		if (administrators == null) {
			administrators = new ArrayList<Person>();
			for (String component:  courseGroupDAO.getAdministrators()) {
				UserProxy user = impl.loadUser(component);
				if (null != user) {
					Person person = new PersonImpl(user.getId(), user.getName(), user.getEmail(), Collections.EMPTY_LIST, null, user.getType());
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
					Person person = new PersonImpl(user.getId(), user.getName(), user.getEmail(), Collections.EMPTY_LIST, null, user.getType());
					superusers.add(person);
				}
			}
		}
		
		return administrators;
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

}
