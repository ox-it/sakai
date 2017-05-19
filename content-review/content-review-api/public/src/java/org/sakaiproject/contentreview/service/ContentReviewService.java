/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.contentreview.service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.site.api.Site;

import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.model.ContentReviewItem;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 *  ContentReview Service manages submission to the Content review queue and retrieving reports from the service
 *  
 *  @author David Jaka, David Horwitz
 */
public interface ContentReviewService {
	
	/**
	 *  Add an item to the Queue for Submission to Turnitin
	 *  
	 *  @param userID if null current user is used
	 *  @param SiteId is null current site is used
	 *  @param assignmentReference reference to the task this is for
	 *  @param content list of content resources to be queued
	 *  @param submissionId reference to the submission this is for
	 *  @param isResubmission true if is a resubmission
	 *  
	 */
	public void queueContent(String userId, String siteId, String taskId, List<ContentResource> content, String submissionId, boolean isResubmission) throws QueueException;
	
	/**
	 *  Retrieve a score for a specific item
	 * @param contentId
	 * @return the origionality score
	 * @throws QueueException
	 * @throws ReportException
	 * @throws Exception
	  */
	public int getReviewScore(String contentId, String assignmentRef, String userId) throws QueueException,
                        ReportException, Exception;

	/**
	 *  Get the URL of the report
	 * @param contentId
	 * @param assignmentRef
	 * @param userId
	 * @return the url
	 * @throws QueueException
	 * @throws ReportException
	 */
	public String getReviewReport(String contentId, String assignmentRef, String userId)
	throws QueueException, ReportException;
	
	/**
	 * Get the URL of a report constructed for a student
	 * @param contentId
	 * @param assignmentRef
	 * @param userId
	 * @return
	 * @throws QueueException
	 * @throws ReportException
	 * */
	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
	throws QueueException, ReportException;
	
	/**
	 * Get the URL for a report constructed for an Instructor
	 * 
	 * @param contentId
	 * @param assignmentRef
	 * @param userId
	 * @return
	 * @throws QueueException
	 * @throws ReportException
	 */
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
	throws QueueException, ReportException;
	
	
	/**
	 * Get the status of a submission
	 * @param contentId
	 * @return
	 * @throws QueueException
	 */
	public Long getReviewStatus(String contentId)
	throws QueueException;
	
	/**
	 * The date an item was queued
	 * @param contextId
	 * @return
	 * @throws QueueException
	 */
	public Date getDateQueued(String contextId)
	throws QueueException;
	
	/**
	 * The date an item was submitted to the queue
	 * @param contextId
	 * @return
	 * @throws QueueException
	 * @throws SubmissionException
	 */
	public Date getDateSubmitted(String contextId)
	throws QueueException, SubmissionException;
	
	/**
	 *  Proccess all pending jobs in the Queue
	 */
	public void processQueue();
	
	/**
	 *  Check for reports for all submitted items that don't have reports yet 
	 */
	public void checkForReports();
	
	
	/**
	 *  Get a list of reports for a task
	 * @param siteId
	 * @param taskId
	 * @return
	 * @throws QueueException
	 * @throws SubmissionException
	 * @throws ReportException
	 */
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
	throws QueueException, SubmissionException, ReportException;
	
	
	/**
	 *  Get a list of reports for all tasks in a site
	 *  
	 * @param siteId
	 * @return
	 * @throws QueueException
	 * @throws SubmissionException
	 * @throws ReportException
	 */
	public List<ContentReviewItem> getReportList(String siteId)
	throws QueueException, SubmissionException, ReportException;
	
	/**
	 * This is a complement to getReportList, except that it returns all
	 * ContentReviewItems for a site and task, rather than just the ones 
	 * whose reports have been completed. 
	 * 
	 * This is the result of running into leaky abstraction problems while
	 * working on Assignments 2, namely that we need to make the pretty
	 * little color coded bars for an entire class for a given assignment,
	 * and if some of them had issues we need to present a fine grained 
	 * error message (such as, your paper was less than 2 paragraphs, or 
	 * your paper was the wrong file type). This requires another property
	 * method, but rather than add a getErrorCode(String contentId) method
	 * it's more efficient to add this so we can get the whole lot in one
	 * DB query, rather than lookup the special case failures.
	 * 
	 * @param siteId
	 * @param taskId
	 * @return
	 */
	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
	throws QueueException, SubmissionException, ReportException;
	
	
	/**
	 * Return the Name of the Service Implementation for Display Purposes
	 * 
	 */
	public String getServiceName();
	
	/**
	 *  Reset the Items for a specific user that where locked because of incomplete user details
	 * @param userId
	 */
	
	public void resetUserDetailsLockedItems(String userId);

	/**
	 * Each content review implementation can either accept all files or reject unsupported file formats.
	 * VeriCite for instance accepts files of any type; if content is in a format that cannot be checked for originality, it returns a score of 0.
	 * However, TurnItIn reports errors when the file format cannot be checked for originality, so we need to block unsupported content.
	 * @return whether all content is accepted by this content review service
	 */
	public boolean allowAllContent();
	
	/**
	 * Is the content resource of a type that can be accepted by the service implementation
	 * @param resource
	 * @return
	 */
	public boolean isAcceptableContent(ContentResource resource);
	
	/**
	 * Is the content resource of a size that can be accepted by the service implementation
	 * @param resource
	 * @return
	 */
	public boolean isAcceptableSize(ContentResource resource);
	
	/**                                                                                                                                                                                                    
	 * Gets a map of acceptable file extensions for this content-review service to their associated mime types (ie. ".rtf" -> ["text/rtf", "application,rtf"])                                             
	 */                                                                                                                                                                                                    
	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes();                                                                                                                                 
																																																		  
	/**                                                                                                                                                                                                    
	 * Gets a map of acceptable file types for this content-review service (as UI presentable names) to their associated file extensions (ie. "PowerPoint" -> [".ppt", ".pptx", ".pps", ".ppsx"])          
	 * NB: This must always be implemented as a LinkedHashMap or equivalent; the order is expected to be preserved                                                                                         
	 */                                                                                                                                                                                                    
	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions();

	/**
	 *  Can this site make use of the content review service
	 * 
	 * @param site
	 * @return
	 * 
	 */
	public boolean isSiteAcceptable(Site site);
	
	/**
	 *  Can this site make use of the direct TII submission mode
	 * 
	 * @param site
	 * @return
	 * 
	 */
	public boolean isDirectAccess(Site s);
	
	/**
	 * Version of the above method compatible with date-aware site advisors. This is a transitional
	 * method that should be removed when TII legacy api support ends
	 * @param s
	 * @param assignmentCreationDate
	 * @return 
	 */
	@Deprecated
	public boolean isDirectAccess(Site s, Date assignmentCreationDate);
	
	/**
	 *  Get a icon URL that for a specific score
	 * @param score
	 * @return
	 */
	public String getIconUrlforScore(Long score);
	
 	/**
	 *  Get a icon colour for a specific score
	 * @param score
	 * @return
	 */
	public String getIconColorforScore(Long score);

	/**
	 *  Does the service support multiple attachments on a single submission?
	 * @return
	 */
	public boolean allowMultipleAttachments();
	
	/**
	 *  Does the service support resubmissions?
	 * @return
	 */
	public boolean allowResubmission();
	
	/**
	 *  Remove an item from the review Queue
	 * @param ContentId
	 */
	public void removeFromQueue(String ContentId);
	
	/**
	 * Get a status message for a submission in the locale of the specified user
	 * @param messageCode
	 * @param userRef
	 * @return
	 */
	public String getLocalizedStatusMessage(String messageCode, String userRef);
	
	/**
	 * Get a status message for a submission in the locale of the current user
	 * @param messageCode
	 * @return
	 */
	public String getLocalizedStatusMessage(String messageCode);
	
	/**
	 * Get a error report for a Specific method
	 * @param contentId
	 * @return
	 * @deprecated use {@link #getLocalizedStatusMessage(String)}
	 */
	public String getReviewError(String contentId);
	/**
	 * Get a status message for a submission in the locale specified
	 * @param messageCode
	 * @param locale
	 * @return
	 */
	public String getLocalizedStatusMessage(String messageCode, Locale locale);
	
	/**
	 * This is a vendor specific method to allow getting information about
	 * a particular assignment in an external plagiarism checking system.
	 * The method returns a Map of keys and properties since they may differ
	 * between implementations.
	 * 
	 * In the Turnitin implementation this provides all the return information
	 * that comes over the wire from their Fid4 Fcmd7 function which can 
	 * be referenced from their API Documentation.
	 * 
	 * This method may be necessary for deeper integrations (A2), but could
	 * tie your code to a particular implementation.
	 * 
	 * @param siteId
	 * @param taskId
	 * @return
	 * @throws SubmissionException
         * @throws TransientSubmissionException
	 */
	public Map getAssignment(String siteId, String taskId)
	throws SubmissionException, TransientSubmissionException;
	
	/**
	 * This is a vendor specific method needed for some deep integrations
	 * (such as A2) to pre provision assignments on an external content
	 * checking system.  The method takes in a Map which can take varying
	 * keys and values depending on implementation.
	 * 
	 * For the Turnitin implementation these keys map to some input 
	 * parameters for Fid4 Fcmd 2/3. These can be seen in Turnitin's API
	 * documentation.
	 * 
	 * Using this method will likely tie you to a particular Content Review
	 * implementation.
	 * 
	 * @param siteId
	 * @param taskId
	 * @param extraAsnnOpts
	 * @throws SubmissionException
	 * @throws TransientSubmissionException
	 */
	public void createAssignment(String siteId, String taskId, Map<String, Object> extraAsnnOpts)
	throws SubmissionException, TransientSubmissionException;
	
	/**
	 * Syncs the assignment with consideration for a student's 'resubmit accept until' date. Otherwise, behavior is identical to createAssignment()
	 * @param extensionDate date of the extension
	 */
	public void offerIndividualExtension(String siteId, String asnId, Map<String, Object> extraAsnOpts, Date extensionDate)
 	throws SubmissionException, TransientSubmissionException;
	
	/**
	 * Get the URL to access the LTI tool associated with the task
	 *
	 * @param taskId
	 * @param siteId
	 * @return
	 * @throws QueueException
	 * @throws ReportException
	 */
	public String getLTIAccess(String taskId, String siteId);

	/**
	 * Delete the LTI tool associated with the task
	 *
	 * @param taskId
	 * @param siteId
	 * @return
	 * @throws QueueException
	 * @throws ReportException
	 */
	public boolean deleteLTITool(String taskId, String siteId);

	/**
	 * Get the ContentReviewItem that matches the id
	 *
	 * @param id
	 * @return
	 */
	public ContentReviewItem getItemById(String id);

	/**
	 * Get the first ContentReviewItem that matches the param
	 *
	 * @param id
	 * @return
	 */
	public ContentReviewItem getFirstItemByContentId(String contentId);

	/**
	 * Get the first ContentReviewItem that matches the param
	 *
	 * @param id
	 * @return
	 */
	public ContentReviewItem getFirstItemByExternalId(String externalId);

	/**
	 * Sets the url as accessed for a submission content
	 *
	 * @param contentId
	 * @return
	 */
	public boolean updateItemAccess(String contentId);

	/**
	 * Updates the externalId field of the contentreview_item with the specified contentId
	 * @param contentId the contentId of the contentreview_item to be updated
	 * @param externalId the ID supplied remotely by the content review servie for this item
	 * @return whether the update was successful
	 */
	public boolean updateExternalId(String contentId, String externalId);

	/**
	 * Sets the grade for a submission content
	 *
	 * @param contentId
	 * @return
	 */
	public boolean updateExternalGrade(String contentId, String score);

	/**
	 * Gets the grade for a submission content
	 *
	 * @param contentId
	 * @return
	 */
	public String getExternalGradeForContentId(String contentId);
	
	/**
	 * Returns the value set for the named configuration setting, or empty string if not set
	 * @param name the name (key) of the config setting
	 * @param activityId the internal identifier of the particular activity within the tool (ex: a particular assignmentId)
	 * @param toolId the Sakai tool id (ex: sakai.assignment.grades)
	 * @param providerId the unique identifier for the content review provider
	 * @return the configuration value, or empty string
	 */
	public String getActivityConfigValue(String name, String activityId, String toolId, int providerId);

	/**
	 * Sets the activity config attribute to the given value
	 * @param name the config attribute name
	 * @param value the config attribute value
	 * @param activityId activityId the unique identifier for the activity (ex: an assignment id)
	 * @param toolId the Sakai tool id (ex: sakai.assignment.grades)
	 * @param providerId the unique identifier of the content review service provider this configuration applies to
	 * @param overrideIfSet if true, will override the attribute value if it is already set
	 * @return true if the config attribute value was added or changed
	 */
	public boolean saveOrUpdateActivityConfigEntry(String name, String value, String activityId, String toolId, int providerId, boolean overrideIfSet);
	
	/**
	 * Migrates the original LTI XML settings from the assignments table into the new activity config table.
	 * Also moves the external value from the content resource binary entity back into the contentreviewitem table.
	 * You need to run this ONLY if you have previously deployed the LTI integration prior to the introduction of TII-219 and TII-221.
	 */
	public void migrateLtiXml();

	/**
	 * Returns the implementation-specific localized error message for an invalid assignment configuration
	 * @return the localized message indicating the problem with the content review configuration for this assignment
	 */
	public String getLocalizedInvalidAsnConfigError();

	/**
	 * Returns true if the Sakai activity is configured correctly for this content review service
	 * @param toolId the Sakai tool id that the activity belongs to (ex: sakai.assignment.grades)
	 * @param activityId the unique identifier for the activity (ex: an assignment id)
	 * @return true if the given activity is configured correctly for use with the review service
	 */
	public boolean validateActivityConfiguration(String toolId, String activityId);

	/**
	 * Get the effective due date for the given ContentReviewItem. Takes into account assignment due date,
	 * assignment resubmit due date, any manually set student-specific resubmit date if present, and the due
	 * date buffer controlled by the "contentreview.due.date.queue.job.buffer.minutes" sakai.property
	 *
	 * @param assignmentID the ID of the assignment in question
	 * @param assignmentDueDate the due date of the assignment
	 * @param assignmentProperties the ResourceProperties object for the assignment
	 * @param dueDateBuffer the due date buffer in minutes, from sakai.properties
	 * @return the effective due date in milliseconds (long)
	 */
	public long getEffectiveDueDate(String assignmentID, long assignmentDueDate, ResourceProperties assignmentProperties, int dueDateBuffer);

	/**
	 * Updates the status of all ContentReviewItems for the given assignment. If the assignment is being changed to generate reports immediately,
	 * all items in status 10 (pending, due date) need to be flipped to status 2 (pending). Vice versa for an assignment being changed to generate
	 * reports on the due date.
	 *
	 * @param assignmentRef the ref of the assignment in question
	 * @param generateReportsSetting the assignment's new setting for generating originality reports (2 = on due date)
	 */
	public void updatePendingStatusForAssignment(String assignmentRef, String generateReportsSetting);
}
