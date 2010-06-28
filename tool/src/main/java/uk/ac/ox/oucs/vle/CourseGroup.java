package uk.ac.ox.oucs.vle;

import java.util.Date;

public interface CourseGroup {

	public String getId();
	
	public String getTitle();
	
	public String getDescription();
	
	public int getSize();
	
	public int getPlaces();
	
	public Date getSignupOpens();
	
	public Date getSignupCloses();

}
