package uk.ac.ox.oucs.vle;

import java.util.Date;

public interface CourseComponentSession {

	public int getCourceComponentMuid();
	
	public String getSessionId();
	
	public Date getSessionStart();
	
	public String getSessionStartText();
	
	public Date getSessionEnd();
	
	public String getSessionEndText();

}
