/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.impl.provider.SectionRoleResolver;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;

import org.sakaiproject.component.gradebook.owl.anongrading.AnonAwareEnrollmentRecordImpl;

/**
 * An implementation of Gradebook-specific authorization needs based
 * on the shared Section Awareness API.
 */
public class AuthzSectionsImpl implements Authz {
    private static final Logger log = LoggerFactory.getLogger(AuthzSectionsImpl.class);

    private Authn authn;
    private SectionAwareness sectionAwareness;
    private GradebookPermissionService gradebookPermissionService;

	protected CourseManagementService courseManagementService;
	public void setCourseManagementService(CourseManagementService courseManagementService)
	{
		this.courseManagementService = courseManagementService;
	}

	protected SectionRoleResolver sectionRoleResolver;
	public void setSectionRoleResolver(SectionRoleResolver sectionRoleResolver)
	{
		this.sectionRoleResolver = sectionRoleResolver;
	}

	public boolean isUserAbleToGrade(String gradebookUid) {
		String userUid = authn.getUserUid();
		return isUserAbleToGrade(gradebookUid, userUid);
	}
	
	public boolean isUserAbleToGrade(String gradebookUid, String userUid) {
	    return (getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR) || getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.TA));
    }

	public boolean isUserAbleToGradeAll(String gradebookUid) {
		return isUserAbleToGradeAll(gradebookUid, authn.getUserUid());
	}
	
	public boolean isUserAbleToGradeAll(String gradebookUid, String userUid) {
        return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
    }
	
	public boolean isUserHasGraderPermissions(String gradebookUid) {
		String userUid = authn.getUserUid();
		List permissions = gradebookPermissionService.getGraderPermissionsForUser(gradebookUid, userUid);
		return permissions != null && permissions.size() > 0;
	}
	
	public boolean isUserHasGraderPermissions(Long gradebookId) {
		String userUid = authn.getUserUid();
		List permissions = gradebookPermissionService.getGraderPermissionsForUser(gradebookId, userUid);
		return permissions != null && permissions.size() > 0;
	}
	
	public boolean isUserHasGraderPermissions(String gradebookUid, String userUid) {
		List permissions = gradebookPermissionService.getGraderPermissionsForUser(gradebookUid, userUid);
		return permissions != null && permissions.size() > 0;
	}
	
	public boolean isUserHasGraderPermissions(Long gradebookId, String userUid) {
		List permissions = gradebookPermissionService.getGraderPermissionsForUser(gradebookId, userUid);
		return permissions != null && permissions.size() > 0;
	}
	
	/**
	 * 
	 * @param sectionUid
	 * @return whether user is Role.TA in given section
	 */
	private boolean isUserTAinSection(String sectionUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSectionMemberInRole(sectionUid, userUid, Role.TA);
	}
	
	private boolean isUserTAinSection(String sectionUid, String userUid) {
	    return getSectionAwareness().isSectionMemberInRole(sectionUid, userUid, Role.TA);
	}

	public boolean isUserAbleToEditAssessments(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToViewOwnGrades(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.STUDENT);
	}
	
	public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {
		if (itemId == null || studentUid == null || gradebookUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in AuthzSectionsServiceImpl.isUserAbleToGradeItemForStudent");
		}
		
		if (isUserAbleToGradeAll(gradebookUid)) {
			return GradebookService.gradePermission;
		}
		
		String userUid = authn.getUserUid();
		
		List viewableSections = getViewableSections(gradebookUid);
		List sectionIds = new ArrayList();
		if (viewableSections != null && !viewableSections.isEmpty()) {
			for (Iterator sectionIter = viewableSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIds.add(section.getUuid());
			}
		}
		
		if (isUserHasGraderPermissions(gradebookUid, userUid)) {

			// get the map of authorized item (assignment) ids to grade/view function
			Map itemIdFunctionMap = gradebookPermissionService.getAvailableItemsForStudent(gradebookUid, userUid, studentUid, viewableSections);
			
			if (itemIdFunctionMap == null || itemIdFunctionMap.isEmpty()) {
				return null;  // not authorized to grade/view any items for this student
			}
			
			String functionValueForItem = (String)itemIdFunctionMap.get(itemId);
			String view = GradebookService.viewPermission;
			String grade = GradebookService.gradePermission;
			
			if (functionValueForItem != null) {
				if (functionValueForItem.equalsIgnoreCase(grade))
					return GradebookService.gradePermission;
				
				if (functionValueForItem.equalsIgnoreCase(view))
					return GradebookService.viewPermission;
			}
	
			return null;
			
		} else {
			// use OOTB permissions based upon TA section membership
			for (Iterator iter = sectionIds.iterator(); iter.hasNext(); ) {
				String sectionUuid = (String) iter.next();
				if (isUserTAinSection(sectionUuid) && getSectionAwareness().isSectionMemberInRole(sectionUuid, studentUid, Role.STUDENT)) {
					return GradebookService.gradePermission;
				}
			}
	
			return null;
		}
	}
	
	private boolean isUserAbleToGradeOrViewItemForStudent(String gradebookUid, Long itemId, String studentUid, String function) throws IllegalArgumentException {
		if (itemId == null || studentUid == null || function == null) {
			throw new IllegalArgumentException("Null parameter(s) in AuthzSectionsServiceImpl.isUserAbleToGradeItemForStudent");
		}
		
		if (isUserAbleToGradeAll(gradebookUid)) {
			return true;
		}
		
		String userUid = authn.getUserUid();
		
		List viewableSections = getViewableSections(gradebookUid);
		List sectionIds = new ArrayList();
		if (viewableSections != null && !viewableSections.isEmpty()) {
			for (Iterator sectionIter = viewableSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIds.add(section.getUuid());
			}
		}
		
		if (isUserHasGraderPermissions(gradebookUid, userUid)) {

			// get the map of authorized item (assignment) ids to grade/view function
			Map itemIdFunctionMap = gradebookPermissionService.getAvailableItemsForStudent(gradebookUid, userUid, studentUid, viewableSections);
			
			if (itemIdFunctionMap == null || itemIdFunctionMap.isEmpty()) {
				return false;  // not authorized to grade/view any items for this student
			}
			
			String functionValueForItem = (String)itemIdFunctionMap.get(itemId);
			String view = GradebookService.viewPermission;
			String grade = GradebookService.gradePermission;
			
			if (functionValueForItem != null) {
				if (function.equalsIgnoreCase(grade) && functionValueForItem.equalsIgnoreCase(grade))
					return true;
				
				if (function.equalsIgnoreCase(view) && (functionValueForItem.equalsIgnoreCase(grade) || functionValueForItem.equalsIgnoreCase(view)))
					return true;
			}
	
			return false;
			
		} else {
			// use OOTB permissions based upon TA section membership
			for (Iterator iter = sectionIds.iterator(); iter.hasNext(); ) {
				String sectionUuid = (String) iter.next();
				if (isUserTAinSection(sectionUuid) && getSectionAwareness().isSectionMemberInRole(sectionUuid, studentUid, Role.STUDENT)) {
					return true;
				}
			}
	
			return false;
		}
	}


	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) throws IllegalArgumentException {	
		return isUserAbleToGradeOrViewItemForStudent(gradebookUid, itemId, studentUid, GradebookService.gradePermission);
	}
	
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) throws IllegalArgumentException {
		return isUserAbleToGradeOrViewItemForStudent(gradebookUid, itemId, studentUid, GradebookService.viewPermission);
	}
	
	public List getViewableSections(String gradebookUid) {
		List viewableSections = new ArrayList();
		
		List allSections = getAllSections(gradebookUid);
		if (allSections == null || allSections.isEmpty()) {
			return viewableSections;
		}
		
		if (isUserAbleToGradeAll(gradebookUid)) {
			return allSections;
		}

		Map sectionIdCourseSectionMap = new HashMap();

		for (Iterator sectionIter = allSections.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection) sectionIter.next();
			sectionIdCourseSectionMap.put(section.getUuid(), section);
		}
		
		String userUid = authn.getUserUid();
		
		if (isUserHasGraderPermissions(gradebookUid, userUid)) {	

			List viewableSectionIds =  gradebookPermissionService.getViewableGroupsForUser(gradebookUid, userUid, new ArrayList(sectionIdCourseSectionMap.keySet()));
			if (viewableSectionIds != null && !viewableSectionIds.isEmpty()) {
				for (Iterator idIter = viewableSectionIds.iterator(); idIter.hasNext();) {
					String sectionUuid = (String) idIter.next();
					CourseSection viewableSection = (CourseSection)sectionIdCourseSectionMap.get(sectionUuid);
					if (viewableSection != null)
						viewableSections.add(viewableSection);
				}
			}
		} else {
			// return all sections that the current user is a TA for
			for (Iterator<Map.Entry<String, CourseSection>> iter = sectionIdCourseSectionMap.entrySet().iterator(); iter.hasNext(); ) {
	            Map.Entry<String, CourseSection> entry = iter.next();
	            String sectionUuid = entry.getKey();
				if (isUserTAinSection(sectionUuid)) {
					CourseSection viewableSection = (CourseSection)sectionIdCourseSectionMap.get(sectionUuid);
					if (viewableSection != null)
						viewableSections.add(viewableSection);
				}
			}
		}
		
    	Collections.sort(viewableSections);
		
		return viewableSections;

	}
	
	public List getAllSections(String gradebookUid) {
		SectionAwareness sectionAwareness = getSectionAwareness();
		List sections = sectionAwareness.getSections(gradebookUid);

		return sections;
	}
	
	private List getSectionEnrollmentsTrusted(String sectionUid) {
		return getSectionAwareness().getSectionMembersInRole(sectionUid, Role.STUDENT);
	}
	
	public Map findMatchingEnrollmentsForItem(String gradebookUid, Long categoryId, int gbCategoryType, String optionalSearchString, String optionalSectionUid) {
	    String userUid = authn.getUserUid();
		return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, categoryId, gbCategoryType, optionalSearchString, optionalSectionUid, false);
	}

	// OWL anon
	public Map findMatchingEnrollmentsForItem(String gradebookUid, Long categoryId, int gbCategoryType, String optionalSearchString,
		String optionalSectionUid, boolean itemIsAnon)
	{
		String userUid = authn.getUserUid();
		return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, categoryId, gbCategoryType, optionalSearchString,
			optionalSectionUid, false, itemIsAnon);
	}
	
	public Map findMatchingEnrollmentsForItemForUser(String userUid, String gradebookUid, Long categoryId, int gbCategoryType, String optionalSearchString, String optionalSectionUid) {
	    return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, categoryId, gbCategoryType, optionalSearchString, optionalSectionUid, false);
	}
	
	public Map findMatchingEnrollmentsForViewableCourseGrade(String gradebookUid, int gbCategoryType, String optionalSearchString, String optionalSectionUid) 
	{
		return findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, gbCategoryType, optionalSearchString, optionalSectionUid, false);
	}

	// OWL anon
	public Map findMatchingEnrollmentsForViewableCourseGrade(String gradebookUid, int gbCategoryType, String optionalSearchString, String optionalSectionUid, boolean isAnon)
	{
	    String userUid = authn.getUserUid();
		return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, null, gbCategoryType, optionalSearchString, optionalSectionUid, true, isAnon);
	}
	
	// Original code
	/*public Map findMatchingEnrollmentsForViewableItems(String gradebookUid, List allGbItems, String optionalSearchString, String optionalSectionUid) {
		Map enrollmentMap = new HashMap();
		List<EnrollmentRecord> filteredEnrollments = null;
		if (optionalSearchString != null)
			filteredEnrollments = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
		else
			filteredEnrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		
		if (filteredEnrollments == null || filteredEnrollments.isEmpty()) 
			return enrollmentMap;
		
		// get all the students in the filtered section, if appropriate
		Map<String, EnrollmentRecord> studentsInSectionMap = new HashMap<String, EnrollmentRecord>();
		if (optionalSectionUid !=  null) {
			List<EnrollmentRecord> sectionMembers = getSectionEnrollmentsTrusted(optionalSectionUid);
			if (!sectionMembers.isEmpty()) {
				for(Iterator<EnrollmentRecord> memberIter = sectionMembers.iterator(); memberIter.hasNext();) {
					EnrollmentRecord member = (EnrollmentRecord) memberIter.next();
					studentsInSectionMap.put(member.getUser().getUserUid(), member);
				}
			}
		}
		
		Map<String, EnrollmentRecord> studentIdEnrRecMap = new HashMap<String, EnrollmentRecord>();
		for (Iterator<EnrollmentRecord> enrIter = filteredEnrollments.iterator(); enrIter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
			String studentId = enr.getUser().getUserUid();
			if (optionalSectionUid != null) {
				if (studentsInSectionMap.containsKey(studentId)) {
					studentIdEnrRecMap.put(studentId, enr);
				}
			} else {
				studentIdEnrRecMap.put(studentId, enr);
			}
		}			
			
		if (isUserAbleToGradeAll(gradebookUid)) {
			List enrollments = new ArrayList(studentIdEnrRecMap.values());
			
			HashMap assignFunctionMap = new HashMap();
			if (allGbItems != null && !allGbItems.isEmpty()) {
				for (Iterator assignIter = allGbItems.iterator(); assignIter.hasNext();) {
					Object assign = assignIter.next();
					Long assignId = null;
					if (assign instanceof org.sakaiproject.service.gradebook.shared.Assignment) {
						assignId = ((org.sakaiproject.service.gradebook.shared.Assignment)assign).getId();
					} else if (assign instanceof org.sakaiproject.tool.gradebook.Assignment) {
						assignId = ((org.sakaiproject.tool.gradebook.Assignment)assign).getId();
					}

					if (assignId != null)
						assignFunctionMap.put(assignId, GradebookService.gradePermission);
				}
			}
			
			for (Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
				enrollmentMap.put(enr, assignFunctionMap);
			}
			
		} else {
			String userId = authn.getUserUid();
			
			Map sectionIdCourseSectionMap = new HashMap();
			List viewableSections = getViewableSections(gradebookUid);
			for (Iterator sectionIter = viewableSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIdCourseSectionMap.put(section.getUuid(), section);
			}
			
			if (isUserHasGraderPermissions(gradebookUid)) {
				// user has special grader permissions that override default perms
				
				List myStudentIds = new ArrayList(studentIdEnrRecMap.keySet());
				
				List selSections = new ArrayList();
				if (optionalSectionUid == null) {  
					// pass all sections
					selSections = new ArrayList(sectionIdCourseSectionMap.values());
				} else {
					// only pass the selected section
					CourseSection section = (CourseSection) sectionIdCourseSectionMap.get(optionalSectionUid);
					if (section != null)
						selSections.add(section);
				}
				
				// we need to get the viewable students, so first create section id --> student ids map
				myStudentIds = getGradebookPermissionService().getViewableStudentsForUser(gradebookUid, userId, myStudentIds, selSections);
				Map viewableStudentIdItemsMap = new HashMap();
				if (allGbItems == null || allGbItems.isEmpty()) {
					if (myStudentIds != null) {
						for (Iterator stIter = myStudentIds.iterator(); stIter.hasNext();) {
							String stId = (String) stIter.next();
							if (stId != null)
								viewableStudentIdItemsMap.put(stId, null);
						}
					}
				} else {
					viewableStudentIdItemsMap = gradebookPermissionService.getAvailableItemsForStudents(gradebookUid, userId, myStudentIds, selSections);
				}
				
				if (!viewableStudentIdItemsMap.isEmpty()) {
					for (Iterator<Map.Entry<String, EnrollmentRecord>> enrIter = viewableStudentIdItemsMap.entrySet().iterator(); enrIter.hasNext();) {
		                Map.Entry<String, EnrollmentRecord> entry = enrIter.next();
						String studentId = entry.getKey();
						EnrollmentRecord enrRec = (EnrollmentRecord)studentIdEnrRecMap.get(studentId);
						if (enrRec != null) {	
							Map itemIdFunctionMap = (Map)viewableStudentIdItemsMap.get(studentId);
							//if (!itemIdFunctionMap.isEmpty()) {
								enrollmentMap.put(enrRec, itemIdFunctionMap);
							//}
						}
					}
				}

			} else { 
				// use default section-based permissions
				
				// Determine the current user's section memberships
				List availableSections = new ArrayList();
				if (optionalSectionUid != null && isUserTAinSection(optionalSectionUid)) {
					if (sectionIdCourseSectionMap.containsKey(optionalSectionUid))
						availableSections.add(optionalSectionUid);
				} else {
					for (Iterator iter = sectionIdCourseSectionMap.keySet().iterator(); iter.hasNext(); ) {
						String sectionUuid = (String)iter.next();
						if (isUserTAinSection(sectionUuid)) {
							availableSections.add(sectionUuid);
						}
					}
				}
				
				// Determine which enrollees are in these sections
				Map uniqueEnrollees = new HashMap();
				for (Iterator iter = availableSections.iterator(); iter.hasNext(); ) {
					String sectionUuid = (String)iter.next();
					List sectionEnrollments = getSectionEnrollmentsTrusted(sectionUuid);
					for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
						EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
						uniqueEnrollees.put(enr.getUser().getUserUid(), enr);
					}
				}
				
				// Filter out based upon the original filtered students
				for (Iterator iter = studentIdEnrRecMap.keySet().iterator(); iter.hasNext(); ) {
					String enrId = (String)iter.next();
					if (uniqueEnrollees.containsKey(enrId)) {
						// iterate through the assignments
						Map itemFunctionMap = new HashMap();
						if (allGbItems != null && !allGbItems.isEmpty()) {
							for (Iterator itemIter = allGbItems.iterator(); itemIter.hasNext();) {
								Object assign = itemIter.next();
								Long assignId = null;
								if (assign instanceof org.sakaiproject.service.gradebook.shared.Assignment) {
									assignId = ((org.sakaiproject.service.gradebook.shared.Assignment)assign).getId();
								} else if (assign instanceof org.sakaiproject.tool.gradebook.Assignment) {
									assignId = ((org.sakaiproject.tool.gradebook.Assignment)assign).getId();
								}

								if (assignId != null) {
									itemFunctionMap.put(assignId, GradebookService.gradePermission);
								}
							}
						}
						enrollmentMap.put(studentIdEnrRecMap.get(enrId), itemFunctionMap);
					}
				}
			}
		}

		return enrollmentMap;
	}*/

	public Map findMatchingEnrollmentsForViewableItems(String gradebookUid, List allGbItems, String optionalSearchString, String optionalSecitonUid)
	{
		return findMatchingEnrollmentsForViewableItems(gradebookUid, allGbItems, optionalSearchString, optionalSecitonUid, false, true);
	}

	// OWL-883 anonymous grading
	// if isAnon, EnrollmentRecord is really AnonAwareEnrollmentRecord
	public Map findMatchingEnrollmentsForViewableItems(String gradebookUid, List allGbItems, String optionalSearchString, String optionalSectionUid, boolean isAnon)
	{
		return findMatchingEnrollmentsForViewableItems(gradebookUid, allGbItems, optionalSearchString, optionalSectionUid, isAnon, true);
	}

	// bbailla2 - need to control whether students get filtered out when they don't have anon grading IDs
	public Map findMatchingEnrollmentsForViewableItems(String gradebookUid, List allGbItems, String optionalSearchString, String optionalSectionUid, boolean isAnon, boolean filterStudents)
	{
		Map enrollmentMap = new HashMap();
		List<EnrollmentRecord> filteredEnrollments = null;

		// 1. get EnrollmentRecords for all students in the site, filtered by search string (user id/name) if appropriate
		if (optionalSearchString != null)
		{
			filteredEnrollments = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
		}
		else
		{
			filteredEnrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		}

		if (CollectionUtils.isEmpty(filteredEnrollments))
		{
			return enrollmentMap;
		}

		// 2. get all the students in the filtered section, if appropriate
		Map<String, EnrollmentRecord> studentsInSectionMap = new HashMap<>();
		if (optionalSectionUid != null)
		{
			List<EnrollmentRecord> sectionMembers = getSectionEnrollmentsTrusted(optionalSectionUid);
			if (!sectionMembers.isEmpty())
			{
				for (Iterator<EnrollmentRecord> memberIter = sectionMembers.iterator(); memberIter.hasNext();)
				{
					EnrollmentRecord member = (EnrollmentRecord) memberIter.next();
					studentsInSectionMap.put(member.getUser().getUserUid(), member);
				}
			}
		}

		// 3. map student uid to enrollment record, filtered by specific section, if appropriate
		Map<String, EnrollmentRecord> studentIdEnrRecMap = new HashMap<>();
		for (Iterator<EnrollmentRecord> enrIter = filteredEnrollments.iterator(); enrIter.hasNext();)
		{
			EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
			String studentId = enr.getUser().getUserUid();
			if (optionalSectionUid != null)
			{
				if (studentsInSectionMap.containsKey(studentId))
				{
					studentIdEnrRecMap.put(studentId, enr);
				}
			}
			else
			{
				studentIdEnrRecMap.put(studentId, enr);
			}
		}

		// 4. if user is instructor, map all the filtered enrollment records to map of assignment to permission "grade"
		// at the end we will have Map<EnrollmentRecord, Map<Long, String>> where Long is the assignment id and String is always "grade"
		if (isUserAbleToGradeAll(gradebookUid))
		{
			List enrollments = new ArrayList(studentIdEnrRecMap.values());

			HashMap assignFunctionMap = new HashMap();
			if (allGbItems != null && !allGbItems.isEmpty())
			{
				for (Iterator assignIter = allGbItems.iterator(); assignIter.hasNext();)
				{
					Object assign = assignIter.next();
					Long assignId = null;
					if (assign instanceof org.sakaiproject.service.gradebook.shared.Assignment)
					{
						assignId = ((org.sakaiproject.service.gradebook.shared.Assignment)assign).getId();
					}
					else if (assign instanceof org.sakaiproject.tool.gradebook.Assignment)
					{
						assignId = ((org.sakaiproject.tool.gradebook.Assignment)assign).getId();
					}

					if (assignId != null)
					{
						assignFunctionMap.put(assignId, GradebookService.gradePermission);
					}
				}
			}

			for (Iterator enrIter = enrollments.iterator(); enrIter.hasNext();)
			{
				EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
				enrollmentMap.put(enr, assignFunctionMap);
			}
		}
		else
		{
			// 5. if the user is not the instructor, do this other stuff....
			String userId = authn.getUserUid();

			Map sectionIdCourseSectionMap = new HashMap();
			List viewableSections = getViewableSections(gradebookUid);
			for (Iterator sectionIter = viewableSections.iterator(); sectionIter.hasNext();)
			{
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIdCourseSectionMap.put(section.getUuid(), section);
			}

			if (isUserHasGraderPermissions(gradebookUid))
			{
				// user has special grader permissions that override default perms

				List myStudentIds = new ArrayList(studentIdEnrRecMap.keySet());

				List selSections = new ArrayList();
				if (optionalSectionUid == null)
				{
					// pass all sections
					selSections = new ArrayList(sectionIdCourseSectionMap.values());
				}
				else
				{
					// only pass the selected section
					CourseSection section = (CourseSection) sectionIdCourseSectionMap.get(optionalSectionUid);
					if (section != null)
					{
						selSections.add(section);
					}
				}

				// we need to get the viewable students, so first create section id --> student ids map
				myStudentIds = getGradebookPermissionService().getViewableStudentsForUser(gradebookUid, userId, myStudentIds, selSections);
				Map viewableStudentIdItemsMap = new HashMap();
				if (CollectionUtils.isEmpty(allGbItems))
				{
					if (myStudentIds != null)
					{
						for (Iterator stIter = myStudentIds.iterator(); stIter.hasNext();)
						{
							String stId = (String) stIter.next();
							if (stId != null)
							{
								viewableStudentIdItemsMap.put(stId, null);
							}
						}
					}
				}
				else
				{
					viewableStudentIdItemsMap = gradebookPermissionService.getAvailableItemsForStudents(gradebookUid, userId, myStudentIds, selSections);
				}

				if (!viewableStudentIdItemsMap.isEmpty())
				{
					for (Iterator<Map.Entry<String, EnrollmentRecord>> enrIter = viewableStudentIdItemsMap.entrySet().iterator(); enrIter.hasNext();)
					{
						Map.Entry<String, EnrollmentRecord> entry = enrIter.next();
						String studentId = entry.getKey();
						EnrollmentRecord enrRec = (EnrollmentRecord)studentIdEnrRecMap.get(studentId);
						if (enrRec != null)
						{
							Map itemIdFunctionMap = (Map) viewableStudentIdItemsMap.get(studentId);
							enrollmentMap.put(enrRec, itemIdFunctionMap);
						}
					}
				}
			}
			else
			{
				// use default section-based permissions

				// Determine the current user's section memberships
				List availableSections = new ArrayList();
				if (optionalSectionUid != null && isUserTAinSection(optionalSectionUid))
				{
					if (sectionIdCourseSectionMap.containsKey(optionalSectionUid))
					{
						availableSections.add(optionalSectionUid);
					}
				}
				else
				{
					for (Iterator iter = sectionIdCourseSectionMap.keySet().iterator(); iter.hasNext(); )
					{
						String sectionUuid = (String)iter.next();
						if (isUserTAinSection(sectionUuid))
						{
							availableSections.add(sectionUuid);
						}
					}
				}

				// Determine which enrollees are in these sections
				Map uniqueEnrollees = new HashMap();
				for (Iterator iter = availableSections.iterator(); iter.hasNext(); )
				{
					String sectionUuid = (String) iter.next();
					List sectionEnrollments = getSectionEnrollmentsTrusted(sectionUuid);
					for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext();)
					{
						EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
						uniqueEnrollees.put(enr.getUser().getUserUid(), enr);
					}
				}

				// Filter out based upon the original filtered students
				for (Iterator iter = studentIdEnrRecMap.keySet().iterator(); iter.hasNext(); )
				{
					String enrId = (String) iter.next();
					if (uniqueEnrollees.containsKey(enrId))
					{
						// iterate through the assignments
						Map itemFunctionMap = new HashMap();
						if (allGbItems != null && !allGbItems.isEmpty())
						{
							for (Iterator itemIter = allGbItems.iterator(); itemIter.hasNext();)
							{
								Object assign = itemIter.next();
								Long assignId = null;
								if (assign instanceof org.sakaiproject.service.gradebook.shared.Assignment)
								{
									assignId = ((org.sakaiproject.service.gradebook.shared.Assignment)assign).getId();
								}
								else if (assign instanceof org.sakaiproject.tool.gradebook.Assignment)
								{
									assignId = ((org.sakaiproject.tool.gradebook.Assignment)assign).getId();
								}

								if (assignId != null)
								{
									itemFunctionMap.put(assignId, GradebookService.gradePermission);
								}
							}
						}
						enrollmentMap.put(studentIdEnrRecMap.get(enrId), itemFunctionMap);
					}
				}
			}
		}

		// OWL anon grading --plukasew

		// at this point we have enrollmentMap, which is a map of all the viewable-by-current-user students to
		// a map of their assignments to their particular grading
		// permission (again for the current user) of "view" or "grade"
		// we are interested in returning only the students in this map that have anonIds
		// perhaps it would be best to avoid this filtering here and have it done at the same time as the retrieval of
		// the enrollment records. However, there is not a 1-to-1 mapping of enrollment record to anonId because anonId
		// can vary by section

		// filter out users that do not have anon grading ids
		// and transform EnrollmentRecord into AnonAwareEnrollmentRecord
		if (isAnon)
		{
			// because this is called by All Grades page, we need to return all enrollments even if they don't have anonIds???
			return getAnonymizedEnrollmentMap(enrollmentMap, gradebookUid, optionalSectionUid, filterStudents);
		}

		return enrollmentMap;
	}

	/**
	 * Takes an enrollmentMap, which is a map of all the viewable-by-current-user students to their particular grading
	 * permission (again for the current user) of "view" or "grade".
	 * Returns only the students in this map that have anonIds
	 * Perhaps it would be best to avoid this filtering here and have it done at the same time as the retrieval of
	 * the enrollment records. However, there is not a 1-to-1 mapping of enrollment record to anonId because anonId
	 * can vary by section
	 *  --plukasew, bbailla2
	 */
	private Map getAnonymizedEnrollmentMap(Map enrollmentMap, String gradebookUid, String optionalSectionUid)
	{
		return getAnonymizedEnrollmentMap(enrollmentMap, gradebookUid, optionalSectionUid, true);
	}

	/**
	 * Takes an enrollmentMap, which is a map of all the viewable-by-current-user students to their particular grading
	 * permissions (again for the current user) of "view" or "grade".
	 * Returns only the students in this map that have anonIds
	 * Perhaps it would be best to avoid this filtering here and have it done at the same time as the retrieval of
	 * the enrollment records. However, there is not a 1-to-1 mapping of enrollment record to anonId because anonId
	 * can vary by section
	 * @param filterStudents - if true, the students without anonymous grading IDs are filtered out. Otherwise, all students are included
	 *  --plukasew, bbailla2
	 */
	private Map getAnonymizedEnrollmentMap(Map enrollmentMap, String gradebookUid, String optionalSectionUid, boolean filterStudents)
	{
		// filter out users that do not have anon grading ids
		// and transform EnrollmentRecord into AnonAwareEnrollmentRecord

		// which section are we looking at here?
		Set<String> sectionEidSet;
		if (optionalSectionUid != null)
		{
			// we have a specific section so we can load just those anonIds
			CourseSection selectedSection = getSectionAwareness().getSection(optionalSectionUid);
			sectionEidSet = Collections.singleton(selectedSection.getEid());
		}
		else
		{
			// we don't have a specific section, so we need to find all sections
			// in the site. We don't care too much about section permissions but the
			// enrollment record retrieval code will have already taken care of it
			sectionEidSet = new HashSet<String>();
			Iterator secIter = getAllSections(gradebookUid).iterator();
			while (secIter.hasNext())
			{
				CourseSection section = (CourseSection) secIter.next();
				sectionEidSet.add(section.getEid());
			}
		}

		// transform to anonId Map for easy lookup
		Map<String, Map<String, String>> anonIdMap = getGradebookService().getAnonGradingIdMapBySectionEids(sectionEidSet);

		Map anonymizedMap = new HashMap();
		Iterator keyIter = enrollmentMap.keySet().iterator();
		while (keyIter.hasNext())
		{
			EnrollmentRecord rec = (EnrollmentRecord) keyIter.next();
			String userEid = rec.getUser().getDisplayId();

			// if user has anonIds, put them in a map
			Map<String, String> anonIds = anonIdMap.get(userEid);
			if (anonIds != null && !anonIds.isEmpty())
			{
				anonymizedMap.put(new AnonAwareEnrollmentRecordImpl(anonIds, rec), enrollmentMap.get(rec));
			}
			else if (!filterStudents)
			{
				anonymizedMap.put(rec, enrollmentMap.get(rec));
			}
		}

		return anonymizedMap;
	}

	
	/**
	 * @param userUid
	 * @param gradebookUid
	 * @param categoryId
	 * @param optionalSearchString
	 * @param optionalSectionUid
	 * @param itemIsCourseGrade
	 * @return Map of EnrollmentRecord --> View or Grade 
	 */
	// Original code commented out for anon-grading  --plukasew
	/*
	private Map findMatchingEnrollmentsForItemOrCourseGrade(String userUid, String gradebookUid, Long categoryId, int gbCategoryType, String optionalSearchString, String optionalSectionUid, boolean itemIsCourseGrade) {
		Map enrollmentMap = new HashMap();
		List filteredEnrollments = new ArrayList();
		
		if (optionalSearchString != null)
			filteredEnrollments = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
		else
			filteredEnrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		
		if (filteredEnrollments.isEmpty()) 
			return enrollmentMap;
		
		// get all the students in the filtered section, if appropriate
		Map studentsInSectionMap = new HashMap();
		if (optionalSectionUid !=  null) {
			List sectionMembers = getSectionAwareness().getSectionMembersInRole(optionalSectionUid, Role.STUDENT);
			if (!sectionMembers.isEmpty()) {
				for(Iterator memberIter = sectionMembers.iterator(); memberIter.hasNext();) {
					EnrollmentRecord member = (EnrollmentRecord) memberIter.next();
					studentsInSectionMap.put(member.getUser().getUserUid(), member);
				}
			}
		}
		
		Map studentIdEnrRecMap = new HashMap();
		for (Iterator enrIter = filteredEnrollments.iterator(); enrIter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
			String studentId = enr.getUser().getUserUid();
			if (optionalSectionUid != null) {
				if (studentsInSectionMap.containsKey(studentId)) {
					studentIdEnrRecMap.put(studentId, enr);
				}
			} else {
				studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
			}
		}
			
		if (isUserAbleToGradeAll(gradebookUid, userUid)) {
			List enrollments = new ArrayList(studentIdEnrRecMap.values());
			
			for (Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
				enrollmentMap.put(enr, GradebookService.gradePermission);
			}

		} else {
			Map sectionIdCourseSectionMap = new HashMap();
			List allSections = getAllSections(gradebookUid);
			for (Iterator sectionIter = allSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIdCourseSectionMap.put(section.getUuid(), section);
			}

			if (isUserHasGraderPermissions(gradebookUid, userUid)) {
				// user has special grader permissions that override default perms
				
				List myStudentIds = new ArrayList(studentIdEnrRecMap.keySet());
				
				List selSections = new ArrayList();
				if (optionalSectionUid == null) {  
					// pass all sections
					selSections = new ArrayList(sectionIdCourseSectionMap.values());
				} else {
					// only pass the selected section
					CourseSection section = (CourseSection) sectionIdCourseSectionMap.get(optionalSectionUid);
					if (section != null)
						selSections.add(section);
				}
				
				Map viewableEnrollees = new HashMap();
				if (itemIsCourseGrade) {
					viewableEnrollees = gradebookPermissionService.getCourseGradePermission(gradebookUid, userUid, myStudentIds, selSections);
				} else {
					viewableEnrollees = gradebookPermissionService.getStudentsForItem(gradebookUid, userUid, myStudentIds, gbCategoryType, categoryId, selSections);
				}
				
				if (!viewableEnrollees.isEmpty()) {
					for (Iterator<Map.Entry<String, EnrollmentRecord>> enrIter = viewableEnrollees.entrySet().iterator(); enrIter.hasNext();) {
                        Map.Entry<String, EnrollmentRecord> entry = enrIter.next();
						String studentId = entry.getKey();
						EnrollmentRecord enrRec = (EnrollmentRecord)studentIdEnrRecMap.get(studentId);
						if (enrRec != null) {
							enrollmentMap.put(enrRec, (String)viewableEnrollees.get(studentId));
						}
					}
				}

			} else { 
				// use default section-based permissions
				enrollmentMap = getEnrollmentMapUsingDefaultPermissions(userUid, studentIdEnrRecMap, sectionIdCourseSectionMap, optionalSectionUid);
			}
		}

		return enrollmentMap;
	}*/

	/**
	 * @param userUid
	 * @param gradebookUid
	 * @param categoryId
	 * @param optionalSearchString
	 * @param optionalSectionUid
	 * @param itemIsCourseGrade
	 * @return Map of EnrollmentRecord --> View or Grade (permission of current user with regard to each enrollment record)
	 */
	private Map findMatchingEnrollmentsForItemOrCourseGrade(String userUid, String gradebookUid, Long categoryId, int gbCategoryType, String optionalSearchString, String optionalSectionUid, boolean itemIsCourseGrade)
	{
		return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, categoryId, gbCategoryType,
			optionalSearchString, optionalSectionUid, itemIsCourseGrade, false);
	}

	// returns map of EnrollmentRecord -> View or Grade permission. If itemIsAnon, EnrollmentRecord is really an AnonAwareEnrollmentRecord
	private Map findMatchingEnrollmentsForItemOrCourseGrade(String userUid, String gradebookUid, Long categoryId, int gbCategoryType,
		String optionalSearchString, String optionalSectionUid, boolean itemIsCourseGrade, boolean itemIsAnon)
	{
		Map enrollmentMap = new HashMap();
		List filteredEnrollments = new ArrayList();

		// 1. get EnrollmentRecords for all the students in the site, filtered by search string (user id/name) if appropriate
		if (optionalSearchString != null)
		{
			filteredEnrollments = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
		}
		else
		{
			filteredEnrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		}

		if (filteredEnrollments.isEmpty())
		{
			return enrollmentMap;
		}

		// 2. get all the students in the filtered section, if appropriate
		Map studentsInSectionMap = new HashMap();
		if (optionalSectionUid != null)
		{
			List sectionMembers = getSectionAwareness().getSectionMembersInRole(optionalSectionUid, Role.STUDENT);
			if (!sectionMembers.isEmpty())
			{
				for (Iterator memberIter = sectionMembers.iterator(); memberIter.hasNext();)
				{
					EnrollmentRecord member = (EnrollmentRecord) memberIter.next();
					studentsInSectionMap.put(member.getUser().getUserUid(), member);
				}
			}
		}

		// 3. map student uid to enrollment record, filtered to a specific section, if appropriate
		Map studentIdEnrRecMap = new HashMap();
		for (Iterator enrIter = filteredEnrollments.iterator(); enrIter.hasNext();)
		{
			EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
			String studentId = enr.getUser().getUserUid();
			if (optionalSectionUid != null)
			{
				if (studentsInSectionMap.containsKey(studentId))
				{
					studentIdEnrRecMap.put(studentId, enr);
				}
			}
			else
			{
				studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
			}
		}

		// 4. if user is the instructor, map all filtered enrollment records to the permission "grade"
		if (isUserAbleToGradeAll(gradebookUid, userUid))
		{
			List enrollments = new ArrayList(studentIdEnrRecMap.values());

			for (Iterator enrIter = enrollments.iterator(); enrIter.hasNext();)
			{
				EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
				enrollmentMap.put(enr, GradebookService.gradePermission);
			}
		}
		else
		{
			// 5. if user is not the instructor, do this other stuff....

			// 5a. map section uuids to section objects
			Map sectionIdCourseSectionMap = new HashMap();
			List allSections = getAllSections(gradebookUid);
			for (Iterator sectionIter = allSections.iterator(); sectionIter.hasNext();)
			{
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIdCourseSectionMap.put(section.getUuid(), section);
			}

			if (isUserHasGraderPermissions(gradebookUid, userUid))
			{
				// user has special grader permissions that override default perms

				List myStudentIds = new ArrayList(studentIdEnrRecMap.keySet());

				List selSections = new ArrayList();
				if (optionalSectionUid == null)
				{
					// pass all sections
					selSections = new ArrayList(sectionIdCourseSectionMap.values());
				}
				else
				{
					// only pass the selected section
					CourseSection section = (CourseSection) sectionIdCourseSectionMap.get(optionalSectionUid);
					if (section != null)
					{
						selSections.add(section);
					}
				}

				// get a map of student uid -> grading permission for the current user (view or grade)
				Map viewableEnrollees = new HashMap();
				if (itemIsCourseGrade)
				{
					viewableEnrollees = gradebookPermissionService.getCourseGradePermission(gradebookUid, userUid, myStudentIds, selSections);
				}
				else
				{
					viewableEnrollees = gradebookPermissionService.getStudentsForItem(gradebookUid, userUid, myStudentIds, gbCategoryType, categoryId, selSections);
				}

				// map EnrollmentRecord -> grading permission
				if (!viewableEnrollees.isEmpty())
				{
					for (Iterator<Map.Entry<String, EnrollmentRecord>> enrIter = viewableEnrollees.entrySet().iterator(); enrIter.hasNext();)
					{
						Map.Entry<String, EnrollmentRecord> entry = enrIter.next();
						String studentId = entry.getKey();
						EnrollmentRecord enrRec = (EnrollmentRecord) studentIdEnrRecMap.get(studentId);
						if (enrRec != null)
						{
							enrollmentMap.put(enrRec, (String)viewableEnrollees.get(studentId));
						}
					}
				}
			}
			else
			{
				// use default section-based permissions
				enrollmentMap = getEnrollmentMapUsingDefaultPermissions(userUid, studentIdEnrRecMap, sectionIdCourseSectionMap, optionalSectionUid);
			}
		}

		// Owl anon grading  --plukasew

		// filter out users that do not have anon grading ids
		// and transform EnrollmentRecord into AnonAwareEnrolmentRecord
		if (itemIsAnon)
		{
			// refactored this into a method  --bbailla2
			return getAnonymizedEnrollmentMap(enrollmentMap, gradebookUid, optionalSectionUid);
		}

		return enrollmentMap;
	}


	/**
	 * 
	 * @param userUid
	 * @param studentIdEnrRecMap
	 * @param sectionIdCourseSectionMap
	 * @param optionalSectionUid
	 * @return Map of EnrollmentRecord to function view/grade using the default permissions (based on TA section membership)
	 */
	private Map getEnrollmentMapUsingDefaultPermissions(String userUid, Map studentIdEnrRecMap, Map sectionIdCourseSectionMap, String optionalSectionUid) {
		// Determine the current user's section memberships
		Map enrollmentMap = new HashMap();
		List availableSections = new ArrayList();
		if (optionalSectionUid != null && isUserTAinSection(optionalSectionUid, userUid)) {
			if (sectionIdCourseSectionMap.containsKey(optionalSectionUid))
				availableSections.add(optionalSectionUid);
		} else {
			for (Iterator iter = sectionIdCourseSectionMap.keySet().iterator(); iter.hasNext(); ) {
				String sectionUuid = (String)iter.next();
				if (isUserTAinSection(sectionUuid, userUid)) {
					availableSections.add(sectionUuid);
				}
			}
		}
		
		// Determine which enrollees are in these sections
		Map uniqueEnrollees = new HashMap();
		for (Iterator iter = availableSections.iterator(); iter.hasNext(); ) {
			String sectionUuid = (String)iter.next();
			List sectionEnrollments = getSectionEnrollmentsTrusted(sectionUuid);
			for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
				EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
				uniqueEnrollees.put(enr.getUser().getUserUid(), enr);
			}
		}
		
		// Filter out based upon the original filtered students
		for (Iterator iter = studentIdEnrRecMap.keySet().iterator(); iter.hasNext(); ) {
			String enrId = (String)iter.next();
			if (uniqueEnrollees.containsKey(enrId)) {
				enrollmentMap.put(studentIdEnrRecMap.get(enrId), GradebookService.gradePermission);
			}
		}
		
		return enrollmentMap;
	}
	
	
	public List findStudentSectionMemberships(String gradebookUid, String studentUid) {
		List sectionMemberships = new ArrayList();
		try {
			sectionMemberships = (List)org.sakaiproject.site.cover.SiteService.getSite(gradebookUid).getGroupsWithMember(studentUid);
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + gradebookUid);
    	}
    	
    	return sectionMemberships;
	}
	
	public List getStudentSectionMembershipNames(String gradebookUid, String studentUid) {
		List sectionNames = new ArrayList();
		List sections = findStudentSectionMemberships(gradebookUid, studentUid);
		if (sections != null && !sections.isEmpty()) {
			Iterator sectionIter = sections.iterator();
			while (sectionIter.hasNext()) {
				Group myGroup = (Group) sectionIter.next();
				sectionNames.add(myGroup.getTitle());
			}
		}
		
		return sectionNames;
	}

	public Authn getAuthn() {
		return authn;
	}
	public void setAuthn(Authn authn) {
		this.authn = authn;
	}
	public SectionAwareness getSectionAwareness() {
		return sectionAwareness;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}
	public GradebookPermissionService getGradebookPermissionService() {
		return gradebookPermissionService;
	}
	public void setGradebookPermissionService(GradebookPermissionService gradebookPermissionService) {
		this.gradebookPermissionService = gradebookPermissionService;
	}

	public GradebookService getGradebookService()
	{
		// this is a hack because I can't get Spring to give me this component
		return (GradebookService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
	}
	
	/********************* Begin OWL custom permission methods   --plukasew ********************/
	// working implementations exist in subclass AuthzSakai2Impl, providing stubs here
	// to meet Authz interface requirements

	@Override
	public boolean isUserAbleToViewExtraUserProperties(String userUid, String siteId)
	{
		return false;
	}

	@Override
	public boolean isUserAbleToSubmitCourseGrades(String userUid, String sectionEID, String siteID)
	{
		return false;
	}

	@Override
	public boolean isUserAbleToApproveCourseGrades(String userUid, String sectionEID, String siteID)
	{
		return false;
	}
	
	@Override
	public boolean isUserAbleToSubmitCourseGrades(String userEid, Set<org.sakaiproject.authz.api.Role> siteRoles, Map<String, String> userRoleMap)
	{
		return false;
	}
	
	@Override
	public boolean isUserAbleToApproveCourseGrades(String userEid, Set<org.sakaiproject.authz.api.Role> siteRoles, Map<String, String> userRoleMap)
	{
		return false;
	}
	
	@Override
	public Map<String, String> getSectionUserRoleMap(String sectionEid)
	{
		return Collections.EMPTY_MAP;
	}
	
	@Override
	public Set<org.sakaiproject.authz.api.Role> getSiteRolesForGradebook(String gradebookUid)
	{
		return Collections.EMPTY_SET;
	}

	/********************* End OWL custom permission methods ********************/

}
