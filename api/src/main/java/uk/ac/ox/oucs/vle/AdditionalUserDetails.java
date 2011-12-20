package uk.ac.ox.oucs.vle;

public interface AdditionalUserDetails {

	public void init();
	
	public String getDegreeProgram(String userId);
	
}
