package uk.ac.ox.oucs.vle;

import java.util.List;

public interface CourseGroup {

	public String getId();
	
	public String getTitle();
	
	public String getDescription();
	
	public String getDepartmentCode();

	public List<CourseComponent> getComponents();
}
