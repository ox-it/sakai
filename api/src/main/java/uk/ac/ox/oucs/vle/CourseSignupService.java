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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Generally most methods which are getters will attempts to get the thing you are
 * after or will return null if it can't be found. If you don't have permission to
 * see the item then you may get a {@link PermissionDeniedException}. Most of the 
 * actions are void and will throw a {@link NotFoundException} when you supply an 
 * invalid id. If you attempt todo something which isn't allowed due to business 
 * rules then you will get an {@link IllegalStateException}.
 * 
 * @author buckett
 *
 */
public interface CourseSignupService {
	
	public static enum Status {
		PENDING(false),
		WITHDRAWN(false),
		APPROVED(true),
		ACCEPTED(true),
		CONFIRMED(true),
		REJECTED(false),
		WAITING(false);
		
		private final boolean takeSpace;
		
		Status(boolean takeSpace) {
			this.takeSpace = takeSpace;
		}
		
		public boolean isTakingSpace() {
			return takeSpace;
		}
		
		public int getSpacesTaken() {
			return (takeSpace)?1:0;
		}
	};
	
	public static enum Range {ALL, UPCOMING, PREVIOUS, NOTSTARTED};
	
	// List of events
	public static final String EVENT_SIGNUP = "coursesignup.signup";
	public static final String EVENT_ACCEPT = "coursesignup.accept";
	public static final String EVENT_WITHDRAW = "coursesignup.withdraw";
	public static final String EVENT_APPROVE = "coursesignup.approve";
	public static final String EVENT_CONFIRM = "coursesignup.confirm";
	public static final String EVENT_REJECT = "coursesignup.reject";
	public static final String EVENT_WAITING = "coursesignup.waiting";
	

	public CourseGroup getCourseGroup(String courseId, Range range);
	
	/**
	 * This loads a course group with only the data that is available at the moment.
	 * @param courseId
	 * @return
	 */
	public CourseGroup getAvailableCourseGroup(String courseId);
	
	/**
	 * Finds all course groups 
	 * @param deptId
	 * @param range
	 * @return
	 */
	public List<CourseGroup> getCourseGroupsByDept(String deptId, Range range, boolean externalUser);
	
	public List<CourseGroup> getCourseGroupsBySubUnit(String subunitId, Range range, boolean externalUser);
	
	public List<CourseGroup> getCourseGroupsByComponent(String componentId);
	
	public List<SubUnit> getSubUnitsByDept(String deptId);
	
	public String findSupervisor(String search);
	
	public CourseSignup signup(String courseId, Set<String> components, String supervisorEmail, String message);
	
	/**
	 * A signup made
	 * @param userId
	 * @param courseId
	 * @param components
	 */
	public CourseSignup signup(String userId, String courseId, Set<String> components, String supervisorId);
	
	public List<CourseSignup> getCourseSignups(String courseId, Set<Status> statuses);
	
	public Integer getCountCourseSignups(String courseId, Set<Status> statuses);
	
	public CourseComponent getCourseComponent(String componentId);
	
	public List<CourseComponent> getAllComponents();
	
	public List<CourseSignup> getComponentSignups(String componentId, Set<Status> statuses)
		throws NotFoundException;
	
	/**
	 * Find signups made to component by componentId.
	 * If statuses/year supplied only signups matching these are returned.
	 * 
	 * @param componentId
	 * 		the component ID find signups for.
	 * @param statuses
	 * 		filter by list of statuses, if <code>null</code> no filtering on status is done.
	 * @param year
	 * 		academic year to include (eq 2012 for year 1st Sep 2012 to 31st Aug 2013), if <code>null</code>
	 * 		no filtering on year is done.
	 * @return A list of signups matching, never <code>null</code>.
	 * @throws NotFoundException If there isn't a component matching componentId.
	 */
	public List<CourseSignup> getComponentSignups(String componentId, Set<Status> statuses, Integer year)
			throws NotFoundException;
	
	public List<CourseSignup> getApprovals();
	
	public List<CourseSignup> getPendings();
	
	public void setSupervisor(String signupId, String supervisorId);
	
	public void setHideCourse(String courseId, boolean hideCourse);
	
	public void approve(String signupId);
	
	public void approve(String signupId, boolean skipAuth, String placementId);
	
	public void accept(String signupId);
	
	public void accept(String signupId, boolean skipAuth, String placementId);
	
	public void confirm(String signupId);
	
	public void confirm(String signupId, boolean skipAuth, String placementId);
	
	public void reject(String signupId);
	
	public void reject(String signupId, boolean skipAuth, String placementId);

	public void waiting(String signupId);
	
	public void waiting(String signupId, boolean skipAuth, String placementId);
	
	public void withdraw(String signupId);
	
	/**
	 * Gets all the CourseGroups that the current user can administer.
	 */
	public List<CourseGroup> getAdministering();
	
	/**
	 * Gets all the CourseGroups that the current user is Lecturing
	 */
	public List<CourseGroup> getLecturing();
	
	public boolean isAdministrator(Set<String> administrators);
	
	public void setSignupStatus(String signupId, Status status);
	
	public List<CourseGroup> search(String search, Range range, boolean external);
	
	/**
	 * Get upcoming course groups by start date
	 * @return
	 */
	public List<CourseGroup> getCourseCalendar(boolean external, String providerId);
	
	/**
	 * Get upcoming course groups with no start date
	 * @return
	 */
	public List<CourseGroup> getCourseNoDates(boolean external, String providerId);
	
	public Map<String, String> getDepartments();
	
	public Department findPracDepartment(String primaryOrgUnit);
	
	public boolean isDepartmentCode(String code);
	
	/**
	 * This is what the service should use when determining the current time.
	 * This is to support testing the data against different times.
	 * @return
	 */
	public Date getNow();
	
	public List<CourseSignup> getMySignups(Set<Status> statuses);
	
	public List<CourseSignup> getUserComponentSignups(String userId, Set<Status> statuses);
	
	/**
	 * Find a particular signup.
	 * @param signupId The signup to load.
	 * @return The signup or null if it couldn't be found.
	 */
	public CourseSignup getCourseSignup(String signupId);
	
	public CourseSignup getCourseSignupAnyway(String signupId);
	
	public String[] getCourseSignupFromEncrypted(String encrypted);
	
	public String getDirectUrl(String courseId);
	
	public Integer getRecentDays();

}
