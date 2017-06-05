/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.cover.SiteService;

import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Site;

import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.sections.AuthzSectionsImpl;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * An implementation of Gradebook-specific authorization needs based
 * on a combination of fine-grained site-scoped Sakai permissions and the
 * shared Section Awareness API. This is a transtional stage between
 * coarse-grained site-and-role-based authz and our hoped-for fine-grained
 * role-determined group-scoped authz.
 */
public class AuthzSakai2Impl extends AuthzSectionsImpl implements Authz {
    private static final Logger log = LoggerFactory.getLogger(AuthzSakai2Impl.class);

    public static final String
    	PERMISSION_GRADE_ALL = "gradebook.gradeAll",
    	PERMISSION_GRADE_SECTION = "gradebook.gradeSection",
    	PERMISSION_EDIT_ASSIGNMENTS = "gradebook.editAssignments",
    	PERMISSION_VIEW_OWN_GRADES = "gradebook.viewOwnGrades";
	
	// OWL custom permissions  --plukasew
	public static final String PERMISSION_VIEW_EXTRA_USER_PROPERTIES = "gradebook.viewExtraUserProperties"; // OWLTODO: switch to community permission name if we contribute
	public static final String PERMISSION_SUBMIT_COURSE_GRADES = "gradebook.submitCourseGrades";
	public static final String PERMISSION_APPROVE_COURSE_GRADES = "gradebook.approveCourseGrades";
		
	// bjones86 - course grade submission email stuff
	private static final String ADMIN_ID				= "admin";		// The login ID for the admin user
	private static final String TMPLT_ELMNT_SUBJECT		= "subject";	// The name of the subject element
	private static final String TMPLT_ELMNT_MESSAGE		= "message";	// The name of the message element
	private static final String TMPLT_ELMNT_HTML		= "messagehtml";	// The name of the message html element
	private static final String TMPLT_ELMNT_LOCALE		= "locale";	// The name of the locale element
	private static final String TMPLT_ELMNT_VERSION		= "version";	// The name of the version elemnt
	private static final String TMPLT_KEY_SFTP_ERROR	= "gradebook.courseGradeSubmission.sftpError";	// The name (key) of the sftp error template
	private static final String TMPLT_KEY_SUB_NOTICE	= "gradebook.courseGradeSubmission.submissionNotice";	// the name (key) of the submission notice tmeplate
	private static final String TMPLT_KEY_SUB_RECEIPT	= "gradebook.courseGradeSubmission.submissionReceipt";	// The name (key) of the submission receipt template

	// OWL-797 (mweston4) Email instructor(s) when grades are approved for their section
	private static final String TMPLT_KEY_APPROVE_NOTICE = "gradebook.courseGradeSubmission.approvalNotice"; // The name (key) of the approval notice template
	private static final String TMPLT_KEY_APPROVE_NOTICE_NO_CHANGE = "gradebook.courseGradeSubmission.approvalNotice.noChange"; // The name (key) of the approval notice template when grades approval is repeated

    /**
     * Perform authorization-specific framework initializations for the Gradebook.
     */
    public void init() {
        Collection registered = FunctionManager.getInstance().getRegisteredFunctions("gradebook");
        if(!registered.contains(PERMISSION_GRADE_ALL)) {
            FunctionManager.registerFunction(PERMISSION_GRADE_ALL);
        }

        if(!registered.contains(PERMISSION_GRADE_SECTION)) {
            FunctionManager.registerFunction(PERMISSION_GRADE_SECTION);
        }

        if(!registered.contains(PERMISSION_EDIT_ASSIGNMENTS)) {
            FunctionManager.registerFunction(PERMISSION_EDIT_ASSIGNMENTS);
        }

        if(!registered.contains(PERMISSION_VIEW_OWN_GRADES)) {
            FunctionManager.registerFunction(PERMISSION_VIEW_OWN_GRADES);
        }
		
		// register OWL custom permission  //plukasew
        String[] owlPerms = {PERMISSION_VIEW_EXTRA_USER_PROPERTIES, PERMISSION_SUBMIT_COURSE_GRADES, PERMISSION_APPROVE_COURSE_GRADES};
		List<String> owlPermList = Arrays.asList(owlPerms);
		if (!registered.containsAll(owlPermList)) // OWLTODO: what if one permission is already registered? does it cause duplicate permissions? fail due to db constraints?
		{
			for (String perm : owlPermList)
			{
					FunctionManager.registerFunction(perm);
			}
		}
		
		// bjones86 - course grade submission email templates
		EmailTemplateService ets = (EmailTemplateService) ComponentManager.get(EmailTemplateService.class);
		ClassLoader cl = AuthzSakai2Impl.class.getClassLoader();
		ets.importTemplateFromXmlFile(cl.getResourceAsStream("courseGradeSubmission_SftpError.xml"), TMPLT_KEY_SFTP_ERROR);
		ets.importTemplateFromXmlFile(cl.getResourceAsStream("courseGradeSubmission_SubmissionNotice.xml"), TMPLT_KEY_SUB_NOTICE);
		ets.importTemplateFromXmlFile(cl.getResourceAsStream("courseGradeSubmission_SubmissionReceipt.xml"), TMPLT_KEY_SUB_RECEIPT);

		// OWL-697 (mweston4) Email instructor(s) when grades are approved for their section
		ets.importTemplateFromXmlFile(cl.getResourceAsStream("courseGradeSubmission_ApprovalNotice.xml"), TMPLT_KEY_APPROVE_NOTICE);
		ets.importTemplateFromXmlFile(cl.getResourceAsStream("courseGradeSubmission_NoChange.xml"), TMPLT_KEY_APPROVE_NOTICE_NO_CHANGE);
    }

	public boolean isUserAbleToGrade(String gradebookUid) {
		return (hasPermission(gradebookUid, PERMISSION_GRADE_ALL) || hasPermission(gradebookUid, PERMISSION_GRADE_SECTION));
	}
	
	public boolean isUserAbleToGrade(String gradebookUid, String userUid) {
	    try {
	        User user = UserDirectoryService.getUser(userUid);
	        return (hasPermission(user, gradebookUid, PERMISSION_GRADE_ALL) || hasPermission(user, gradebookUid, PERMISSION_GRADE_SECTION));
	    } catch (UserNotDefinedException unde) {
	        log.warn("User not found for userUid: " + userUid);
	        return false;
	    }

	}

	public boolean isUserAbleToGradeAll(String gradebookUid) {
		return hasPermission(gradebookUid, PERMISSION_GRADE_ALL);
	}
	
	public boolean isUserAbleToGradeAll(String gradebookUid, String userUid) {
	    try {
	        User user = UserDirectoryService.getUser(userUid);
	        return hasPermission(user, gradebookUid, PERMISSION_GRADE_ALL);
	    } catch (UserNotDefinedException unde) {
	        log.warn("User not found for userUid: " + userUid);
	        return false;
	    }
	}

	/**
	 * When group-scoped permissions are available, this is where
	 * they will go. My current assumption is that the call will look like:
	 *
	 *   return hasPermission(sectionUid, PERMISSION_GRADE_ALL);
	 */
	public boolean isUserAbleToGradeSection(String sectionUid) {
		return getSectionAwareness().isSectionMemberInRole(sectionUid, getAuthn().getUserUid(), Role.TA);
	}

	public boolean isUserAbleToEditAssessments(String gradebookUid) {
		return hasPermission(gradebookUid, PERMISSION_EDIT_ASSIGNMENTS);
	}

	public boolean isUserAbleToViewOwnGrades(String gradebookUid) {
		return hasPermission(gradebookUid, PERMISSION_VIEW_OWN_GRADES);
	}

	private boolean hasPermission(String gradebookUid, String permission) {
		return SecurityService.unlock(permission, SiteService.siteReference(gradebookUid));
	}
	
	private boolean hasPermission(User user, String gradebookUid, String permission) {
	    return SecurityService.unlock(user, permission, SiteService.siteReference(gradebookUid));
	}

	/********************* Begin OWL custom permission methods  --plukasew ********************/

	@Override
	public boolean isUserAbleToViewExtraUserProperties(String userUid, String gradebookUid)
	{
		boolean allowed = false;
		try
		{
			allowed = hasPermission(UserDirectoryService.getUser(userUid), gradebookUid, PERMISSION_VIEW_EXTRA_USER_PROPERTIES);
		}
		catch (UserNotDefinedException unde)
		{
			log.warn("OWL: Extra User Properties: User not found for userUid: " + userUid);
		}

		return allowed;
	}

	/**
	 * This method is slow and should not be used in loops!
	 * @param userEID
	 * @param sectionEID
	 * @param siteID
	 * @return 
	 */
	@Override
	public boolean isUserAbleToSubmitCourseGrades(String userEID, String sectionEID, String siteID)
	{
		return checkUsersSectionRoleForPermission(userEID, sectionEID, siteID, PERMISSION_SUBMIT_COURSE_GRADES);
	}
	
	/**
	 * Faster version of the above method, but it requires more pre-requisites
	 * @param userEid
	 * @param siteRoles
	 * @param userRoleMap
	 * @return 
	 */
	@Override
	public boolean isUserAbleToSubmitCourseGrades(String userEid, Set<org.sakaiproject.authz.api.Role> siteRoles, Map<String, String> userRoleMap)
	{
		return checkUsersSectionRoleForPermission(userEid, siteRoles, userRoleMap, PERMISSION_SUBMIT_COURSE_GRADES);
	}

	/**
	 * This method is slow and should not be used in loops!
	 * @param userEID
	 * @param sectionEID
	 * @param siteID
	 * @return 
	 */
	@Override
	public boolean isUserAbleToApproveCourseGrades(String userEID, String sectionEID, String siteID)
	{
		return checkUsersSectionRoleForPermission(userEID, sectionEID, siteID, PERMISSION_APPROVE_COURSE_GRADES);
	}
	
	/**
	 * Faster version of the above method, but it requires more pre-requisites
	 * @param userEid
	 * @param siteRoles
	 * @param userRoleMap
	 * @return 
	 */
	@Override
	public boolean isUserAbleToApproveCourseGrades(String userEid, Set<org.sakaiproject.authz.api.Role> siteRoles, Map<String, String> userRoleMap)
	{
		return checkUsersSectionRoleForPermission(userEid, siteRoles, userRoleMap, PERMISSION_APPROVE_COURSE_GRADES);
	}

	/**
	 * Utility method to check the given user's section role for the given permission
	 * @author bjones86 - OWL-873
	 * @param userEID the user in question
	 * @param sectionEID the section the user belongs to
	 * @param siteID the site the section belongs to
	 * @param permission the permission to check for
	 * @return true/false
	 */
	@SuppressWarnings("deprecation")
	private boolean checkUsersSectionRoleForPermission(String userEID, String sectionEID, String siteID, String permission)
	{
		boolean allowed = false;
		try
		{
			// Get the section, user-role map for the section, all site roles for the site
			Section section = courseManagementService.getSection(sectionEID);
			Map<String, String> userRoleMap = sectionRoleResolver.getUserRoles(courseManagementService, section);
			Site site = SiteService.getSite(siteID);
			Set<org.sakaiproject.authz.api.Role> siteRoles = site.getRoles();

			allowed = checkUsersSectionRoleForPermission(userEID, siteRoles, userRoleMap, permission);
		}
		catch (IdNotFoundException ex)
		{
			log.error("OWL: section ID not found: " + sectionEID);
		}
		catch (IdUnusedException ex)
		{
			log.error("OWL: site ID unused: " + siteID);
		}

		return allowed;
	}
	
	// faster version of the above method that requires objects up front to avoid costly repeated lookups of CM data
	private boolean checkUsersSectionRoleForPermission(String userEID, Set<org.sakaiproject.authz.api.Role> siteRoles, Map<String, String> userRoleMap, String permission)
	{
		boolean allowed = false;

		org.sakaiproject.authz.api.Role sectionRole = null;
		String sectionRoleId = userRoleMap.get(userEID);
		if (sectionRoleId != null)
		{
			sectionRole = findMatchingRole(siteRoles, sectionRoleId);
		}

		// If the section role has the permission, allowed = true
		if (sectionRole != null && sectionRole.getAllowedFunctions().contains(permission))
		{
			allowed = true;
		}

		return allowed;
	}

	/**
	 * Utility method to find the Role object that matches the given roleID from the provided set of Roles
	 * @author bjones86 - OWL-873
	 * @param allRoles set of unique role Objects
	 * @param roleID the ID of the Role object to return (if found in the set)
	 * @return the matching Role object if found, or null
	 */
	private org.sakaiproject.authz.api.Role findMatchingRole(Set<org.sakaiproject.authz.api.Role> allRoles, String roleID)
	{
		org.sakaiproject.authz.api.Role match = null;
		for (org.sakaiproject.authz.api.Role role : allRoles)
		{
			if (role.getId().equals(roleID))
			{
				match = role;
				break;
			}
		}

		return match;
	}
	
	public Map<String, String> getSectionUserRoleMap(String sectionEid)
	{
		Section section = courseManagementService.getSection(sectionEid);
		return sectionRoleResolver.getUserRoles(courseManagementService, section);
	}
	
	public Set<org.sakaiproject.authz.api.Role> getSiteRolesForGradebook(String gradebookUid)
	{
		try
		{
			return SiteService.getSite(gradebookUid).getRoles();
		}
		catch (IdUnusedException e)
		{
			return Collections.EMPTY_SET;
		}
	}

	/********************* End OWL custom permission methods ********************/
}
