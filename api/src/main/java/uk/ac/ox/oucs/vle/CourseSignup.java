package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.Set;

import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public interface CourseSignup {

	public String getId();
	
	public Person getUser();
	
	public Person getSupervisor();
	
	public String getNotes();
	
	public Status getStatus();
	
	public Date getCreated();
	
	public Set<CourseComponent> getComponents();
	
	public CourseGroup getGroup();

}
