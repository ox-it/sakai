/**
 * Copyright 2014 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.etudes.mneme.entityproviders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.tool.AssessmentsView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.service.gradebook.shared.GradebookService; 
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;

public class MnemeEntityProvider extends AbstractEntityProvider
        implements Inputable, Outputable, Describeable, ActionsExecutable{

    private static Log log = LogFactory.getLog(MnemeEntityProvider.class);

    public final static String ENTITY_PREFIX = "mneme";

    private AssessmentService assessmentService;
    private SessionManager sessionManager;
    private SecurityService securityService;
    private GradebookService gradebookService;

    /** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("mneme");

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix
     * ()
     */
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    /**
     * Retrieves a list of assessments for a given site.
     * @param view
     * @return
     */
    @EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
    public List<SimpleAssessment> getSiteAssessments(EntityView view){

        log.debug("Inside getSiteAssessments()");
        // Get site id from request url.
        String context = view.getPathSegment(2);
        if (context == null){
            throw new IllegalArgumentException(rb.getString("mneme.entityprovider.invalidurl"));
        }
        // User must be logged in to retrieve assessments.
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(rb.getString("mneme.entityprovider.mustlogin"));
        }
        if (!securityService.unlock(MnemeService.MANAGE_PERMISSION, SiteService.siteReference(context))){
            log.info("You are not authorized to perform this operation");
            throw new SecurityException(rb.getString("mneme.entityprovider.notauthorized"));
        }
        // Retrieve list of assessments for specified site. Retrieve both published and unpublished assessments and ordered in ascending create date.
        List<Assessment> assessmentList = assessmentService.getContextAssessments(context, AssessmentService.AssessmentsSort.cdate_a, Boolean.FALSE);

        // Get list of assignments present in the gradebook
		List<Assignment> assignmentList = new ArrayList<>();
        try {
             assignmentList = gradebookService.getAssignments(context);
        } catch (GradebookNotFoundException gnfe) {
            log.info("No gradebook exists for site " + context, gnfe);
        }

        // Convert list of Assessments to a list of SimpleAssessments.
        List<SimpleAssessment> returnList = convertAssessmentListToSimpleAssessmentList(assessmentList, assignmentList);
        return returnList;
    }

    /**
     * Converts a list of Assessment into a list of SimpleAssessment
     * @param listOfAssessments
     * @return
     */
    private List<SimpleAssessment> convertAssessmentListToSimpleAssessmentList(List<Assessment> listOfAssessments, List assignments){

        // List to hold the converted assessments.
        List<SimpleAssessment> returnList = new ArrayList<SimpleAssessment>();
        SimpleAssessment convertedAssessment = null;

        // Loop through the Assessments list and convert them to SimpleAssessments.
        for (Assessment assessment: listOfAssessments){
            convertedAssessment = convertAssessmentToSimpleAssessment(assessment, assignments);
            returnList.add(convertedAssessment);
        }
        return returnList;
    }

    /**
     * Converts a single <code>org.etudes.mneme.api.Assessment</code> object into a 
     * <code>org.etudes.mneme.entityproviders.SimpleAssessment</code> object.
     * @param assessment
     * @return
     */
    private SimpleAssessment convertAssessmentToSimpleAssessment(Assessment assessment, List assignments){
        SimpleAssessment convertedAssessement = new SimpleAssessment();
        // Select the fields from the Assessment object we want returned in the REST call.
        if (assessment != null){
            convertedAssessement.setId(assessment.getId());
            convertedAssessement.setTitle(assessment.getTitle());
            convertedAssessement.setPublished(assessment.getPublished());

            // UMICH-1126. Include the corresponding gradebook id, if "send to gradebook" is set to true in options. 
            if (assessment.getGradebookIntegration() != null && assessment.getGradebookIntegration()){ 
                addGradebookItemExternalIds(convertedAssessement, assessment, assignments);
            }

		}
        return convertedAssessement;
    }

    // Add the external id values for this assessment so it can be linked with 
    // the Gradebook REST API 
    private void addGradebookItemExternalIds(SimpleAssessment convertedAssessement, Assessment assessment, List assignments) {
         for (Iterator iter = assignments.iterator(); iter.hasNext();) { 
             Assignment assignment = (Assignment) iter.next();
             if (assessment.getTitle().equals(assignment.getExternalId())) {
                 convertedAssessement.setGradebookItemExternalId(assignment.getExternalId());
                 convertedAssessement.setGradebookItemId(assignment.getId());
                 break;
             }
         }
    }
      

	/* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable#getHandledOutputFormats()
     */
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON };
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable#getHandledInputFormats()
     */
    public String[] getHandledInputFormats() {
        return new String[] { Formats.XML, Formats.JSON };
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable#getSampleEntity()
     */
    public Object getSampleEntity() {
        return new SimpleAssessment();
    }

    /**
     * @return the current sakai user id (not username)
     */
    private String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    public void setAssessmentService(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

	public GradebookService getGradebookService() {
        return gradebookService;
    }

	public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService; 
    }

}