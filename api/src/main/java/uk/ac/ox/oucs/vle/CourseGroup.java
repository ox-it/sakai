package uk.ac.ox.oucs.vle;

import java.util.List;

public interface CourseGroup {

	public String getId();
	
	public String getTitle();
	
	public String getDescription();
	
	public String getDepartment();
	
	public String getDepartmentCode();
	
	public boolean getPublicView();

	public List<CourseComponent> getComponents();
	
	public List<Person> getAdministrators();
}
