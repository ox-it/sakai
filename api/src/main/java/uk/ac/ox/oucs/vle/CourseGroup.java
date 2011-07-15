package uk.ac.ox.oucs.vle;

import java.util.List;

public interface CourseGroup {

	public String getId();
	
	public String getTitle();
	
	public String getDescription();
	
	public String getDepartment();
	
	public String getDepartmentCode();
	
	public String getSubUnit();
	
	public String getSubUnitCode();
	
	public boolean getPublicView();
	
	public String getContactEmail();

	public List<CourseComponent> getComponents();
	
	public List<Person> getAdministrators();
	
	public List<Person> getSuperusers();
	
	public boolean getIsAdmin();
}
