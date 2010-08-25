package uk.ac.ox.oucs.vle;

import java.util.List;



public interface SakaiProxy {

	/**
	 * Just used for testing, afterwards it can be dropped.
	 * @return
	 */
	public List<Email> getEmails();
	
	public UserProxy getCurrentUser();

	public UserProxy findUserById(String id);

	public UserProxy findUserByEmail(String email);
	
	public UserProxy findUserByEid(String eid);
	
	/**
	 * Log an event happening. It is assumed that the person perfoming the 
	 * event is the current user.
	 * @param resource The resources to which the event is happening.
	 * @param eventType The type of event.
	 */
	public void logEvent(String resource, String eventType);
	
	/**
	 * Send an email out.
	 * @param to The address of the reciptient.
	 * @param subject The subject of the email.
	 * @param body The body of the email.
	 */
	public void sendEmail(String to, String subject, String body);
	
	/**
	 * Get a URL that a user can click on to go to approve/reject a signup.
	 * @param signupId
	 * @return
	 */
	public String getConfirmUrl(String signupId);
	
	/**
	 * Gets a URL to the page which shows a users signups.
	 */
	public String getMyUrl();

}