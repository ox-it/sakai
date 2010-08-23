package uk.ac.ox.oucs.vle;


public interface SakaiProxy {

	public UserProxy getCurrentUser();

	public UserProxy findUserById(String id);

	public UserProxy findUserByEmail(String email);
	
	public UserProxy findUserByEid(String eid);
	
	public void sendEmail(String to, String subject, String body);
	
	/**
	 * Get a URL that a user can click on to go to approve/reject a signup.
	 * @param signupId
	 * @return
	 */
	public String getConfirmUrl(String signupId);

}