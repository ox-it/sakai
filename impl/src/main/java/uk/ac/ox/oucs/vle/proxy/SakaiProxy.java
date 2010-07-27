package uk.ac.ox.oucs.vle.proxy;

public interface SakaiProxy {

	public User getCurrentUser();

	public User findUserById(String id);

	public User findUserByEmail(String email);
	
	public User findUserByEid(String eid);
	
	public void sendEmail(String to, String subject, String body);

}