package org.sakaiproject.lti.api;

/**
 * Functional interface; getLTIRole() retrieves the LTI role (admin / instructor / learner) for the specified context (Ie. siteId of the site hosting this LTI instance), and ltiSiteId (Ie. as specified in External Tools).
 * This is primarily intended for deep integrations like assignments' LTI integration with Turnitin
 *
 * @author bbailla2
 */
public interface LTIRoleAdvisor
{
	/**
	 * Resolves the role for the specified userId, context (Ie. site ID of the site hosting this LTI instance), and LTI Site Id as specified in External Tools
	 * @param userId the user whose LTI role we are retrieving
	 * @param context the siteId of the site hosting this LTI instance
	 * @param ltiSiteId the site ID of the LTI tool as specified in External Tools
	 * Note: This is intended for deep integrations - the LTI Site ID doubles as an identifier for such integrations
	 * @return Admin / Instructor / Learner as decided by the implementation, or null if this advisor cannot decide
	 */
	 public String getLTIRole(String userId, String context, String ltiSiteId);
}
