/*
 * #%L
 * Course Signup API
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.Placement;

import java.util.Properties;

public interface SakaiProxy {

	/**
	 * Get the current user. If there isn't anyone logged in it returns the anonymous user.
	 * @return The current user, never <code>null</code>.
	 */
	public UserProxy getCurrentUser();

	/**
	 * Check if the current user is not logged in.
	 * @return <code>true</code> if the current user isn't logged in.
	 */
	public boolean isAnonymousUser();

	public UserProxy findUserById(String id);
	
	public UserProxy findStudentById(String id);

	public UserProxy findUserByEmail(String email);
	
	public UserProxy findUserByEid(String eid);

	/**
	 * Create a new user so that someone who doesn't have an account can be placed on a module.
	 * @param name The name of the new user, cannot be <code>null</code>.
	 * @param email The email address of the new user, cannot be <code>null</code>.
	 * @return The newly created user.
	 */
	public UserProxy newUser(String name, String email);
	
	/**
	 * Log an event happening. It is assumed that the person perfoming the 
	 * event is the current user.
	 * @param resource The resources to which the event is happening.
	 * @param eventType The type of event.
	 */
	public void logEvent(String resource, String eventType, String placementId);
	
	/**
	 * Send an email out.
	 * @param to The address of the reciptient.
	 * @param subject The subject of the email.
	 * @param body The body of the email.
	 */
	public void sendEmail(String to, String subject, String body);
	
	public Placement getPlacement(String placementId);
	
	/**
	 * Get a URL that a user can click on to go to approve/reject a signup.
	 * @param signupId
	 * @return
	 */
	public String getAdminUrl();
	
	public String getApproveUrl(String signupId);
	
	public String getApproveUrl(String signupId, String placementId);
	
	public String getConfirmUrl(String signupId);
	
	public String getConfirmUrl(String signupId, String placementId);
	
	public String getDirectUrl(String courseId);
	
	public String getAdvanceUrl(String signupId, String status, String placementId);
	
	public String encode(String uncoded);
	
	public String uncode(String encoded);
	
	/**
	 * Gets a URL to the page which shows a users signups.
	 */
	public String getMyUrl();
	
	public String getMyUrl(String placementId);
	
	/**
	 * Get a configuration parameter as an Integer
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public Integer getConfigParam(String param, int dflt);
	
	/**
	 * Get a configuration parameter as a String
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public String getConfigParam(String param, String dflt); 
	
	public void writeLog(String contentId, String contentDisplayName, byte[] bytes);
	
	public void prependLog(String contentId, String contentDisplayName, byte[] bytes);

	/**
	 * This gets a properties object that reflects the current mapping of additional course
	 * categories.
	 * @return A properties object.
	 */
	public Properties getCategoryMapping();


}
