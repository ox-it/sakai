package uk.ac.ox.oucs.vle;

import org.sakaiproject.user.api.User;

import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public interface CourseSignup {

	public String getId();
	
	public User getUser();
	
	public User getSupervisor();
	
	public String getNotes();
	
	public Status getStatus();

}
