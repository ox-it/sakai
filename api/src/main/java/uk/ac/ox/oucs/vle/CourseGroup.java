package uk.ac.ox.oucs.vle;

import java.util.List;

public interface CourseGroup {

	public String getId();
	
	public String getTitle();
	
	public String getDescription();
	
	public String getDepartment();
	
	public String getDepartmentCode();
	
	public boolean getHideExternal();
	
	public Person getAdministrator();

	public List<CourseComponent> getComponents();
}
