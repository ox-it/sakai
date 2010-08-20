package uk.ac.ox.oucs.vle;


public interface SakaiProxy {

	public UserProxy getCurrentUser();

	public UserProxy findUserById(String id);

	public UserProxy findUserByEmail(String email);
	
	public UserProxy findUserByEid(String eid);
	
	public void sendEmail(String to, String subject, String body);

}