package uk.ac.ox.oucs.vle;

import java.util.List;


/**
 * Simple representation of a person.
 * These don't necessarily have accounts in the system.
 * @author buckett
 *
 */
public interface Person {

	public String getId();
	
	public String getName();
	
	public String getEmail();
	
	public List<String> getUnits();
	
	public String getYearOfStudy();
	
	public String getType();
	
}
