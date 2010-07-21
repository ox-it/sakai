package uk.ac.ox.oucs.vle.proxy;

public interface SakaiProxy {

	public abstract User getCurrentUser();

	public abstract User findUserById(String id);

	public abstract User findUserByEmail(String email);
	
	public void sendEmail(String to, String subject, String body);

}