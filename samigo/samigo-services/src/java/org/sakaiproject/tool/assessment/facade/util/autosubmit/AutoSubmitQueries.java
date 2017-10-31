package org.sakaiproject.tool.assessment.facade.util.autosubmit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueries;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.AutoSubmitAssessmentsJob;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Queries for persisting a single attempt/submission and all related updates in a single transaction
 * @author plukasew
 */
public class AutoSubmitQueries extends HibernateDaoSupport implements AutoSubmitQueriesAPI
{
	private static final Log LOG = LogFactory.getLog(AutoSubmitQueries.class);
	
	// Process all attempts/submissions for a single user/quiz pair
	// this method runs in a single database transaction, any rollbacks will revert all changes for only these attempts
	@Override
	public void autoSubmitSingleUserAssessmentAttempts(List<QuizAttempt> attempts, PublishedAssessmentFacade publishedAssessment,
			PersistenceHelper persistenceHelper, boolean updateGrades, EventLogService eventService, EventLogFacade eventLogFacade,
			Map toGradebookPublishedAssessmentSiteIdMap, GradebookServiceHelper gbsHelper, GradebookExternalAssessmentService g,
			HashMap sectionSetMap, AssessmentGradingFacadeQueries queryFacade)
	{
		QuizAttempt autoSubmittedAttempt = null;
		for (QuizAttempt attempt : attempts)
		{
			AssessmentGradingData adata = validateGradingData(attempt);
			long gradingId = adata.getAssessmentGradingId();

			try
			{
				adata.setHasAutoSubmissionRun(Boolean.TRUE);
				
				if (attempt.autoSubmit)
				{
					prepareAttemptForAutoSubmit(adata, publishedAssessment, sectionSetMap, queryFacade);
					autoSubmittedAttempt = attempt;
				}
			
				getHibernateTemplate().saveOrUpdate(adata);

				//update grades
				if(updateGrades && attempt.autoSubmit && toGradebookPublishedAssessmentSiteIdMap != null
						&& toGradebookPublishedAssessmentSiteIdMap.containsKey(adata.getPublishedAssessmentId()))
				{
					updateGrades(gradingId, adata, toGradebookPublishedAssessmentSiteIdMap, gbsHelper, persistenceHelper, g);
				}
			}
			catch (Exception e)
			{
				// record which grading id we had trouble with
				LOG.error("Error while auto submitting assessment grade data id: " + gradingId, e);
				throw e;
			}
		}
		
		// if we get this far and have autosubmitted, log and notify
		if (autoSubmittedAttempt != null)
		{
			logAndNotify(autoSubmittedAttempt, eventService, eventLogFacade, publishedAssessment);
		}
	}
	
	private void updateGrades(long gradingId, AssessmentGradingData adata, Map toGradebookPublishedAssessmentSiteIdMap, GradebookServiceHelper gbsHelper,
			PersistenceHelper persistenceHelper, GradebookExternalAssessmentService g)
	{
		String currentSiteId = (String) toGradebookPublishedAssessmentSiteIdMap.get(adata.getPublishedAssessmentId());
		if (gbsHelper != null && gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(currentSiteId), g)){
			int retryCount = persistenceHelper.getRetryCount();
			boolean success = false;
			Exception lastException = null;
			while (retryCount > 0){
				try {
					Map<String, Double> studentScore = new HashMap<>();
					studentScore.put(adata.getAgentId(), adata.getFinalScore());
					gbsHelper.updateExternalAssessmentScores(adata.getPublishedAssessmentId(), studentScore, g);
					retryCount = 0;
					success = true;
				}
				catch (Exception e) {
					LOG.error("Error while updating external assessment score during auto submitting assessment grade data id: " + gradingId, e);
					lastException = e;
					retryCount = persistenceHelper.retryDeadlock(e, retryCount);
				}
			}

			if (!success)
			{
				throw new RuntimeException(lastException);
			}
		}
	}
	
	// Add the autosubmission to the event log and notify the student
	private void logAndNotify(QuizAttempt autoSubmittedAttempt, EventLogService eventService, EventLogFacade eventLogFacade,
			PublishedAssessmentFacade publishedAssessment)
	{
		AssessmentGradingData adata = autoSubmittedAttempt.attempt;
		long gradingId = adata.getAssessmentGradingId();
		List eventLogDataList = eventService.getEventLogData(gradingId);
		if (!eventLogDataList.isEmpty()) {
			EventLogData eventLogData= (EventLogData) eventLogDataList.get(0);
			//will do the i18n issue later.
			eventLogData.setErrorMsg("No Errors (Auto submit)");
			Date endDate = new Date();
			eventLogData.setEndDate(endDate);
			if(eventLogData.getStartDate() != null) {
				double minute= 1000*60;
				int eclipseTime = (int)Math.ceil(((endDate.getTime() - eventLogData.getStartDate().getTime())/minute));
				eventLogData.setEclipseTime(eclipseTime);
			} else {
				eventLogData.setEclipseTime(null);
				eventLogData.setErrorMsg("Error during auto submit");
			}
			eventLogFacade.setData(eventLogData);
			eventService.saveOrUpdateEventLog(eventLogFacade);
		}

		EventTrackingService.post(EventTrackingService.newEvent("sam.auto-submit.job",
				AutoSubmitAssessmentsJob.safeEventLength("publishedAssessmentId=" + adata.getPublishedAssessmentId() +
						", assessmentGradingId=" + gradingId), true));

		Map<String, Object> notiValues = new HashMap<>();
		notiValues.put("publishedAssessmentID", adata.getPublishedAssessmentId());
		notiValues.put("assessmentGradingID", gradingId);
		notiValues.put("userID", adata.getAgentId());
		notiValues.put("submissionDate", adata.getSubmittedDate());

		String confirmationNumber = adata.getAssessmentGradingId() + "-" + publishedAssessment.getPublishedAssessmentId() + "-"
			+ adata.getAgentId() + "-" + adata.getSubmittedDate().toString();
		notiValues.put( "confirmationNumber", confirmationNumber );

		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AUTO_SUBMITTED,
				notiValues.toString(), AgentFacade.getCurrentSiteId(), false, SamigoConstants.NOTI_EVENT_ASSESSMENT_SUBMITTED));
	}
	
	private AssessmentGradingData validateGradingData(QuizAttempt attempt)
	{
		AssessmentGradingData adata = attempt.attempt;
		if (adata == null || adata.getAssessmentGradingId() == null)
		{
			LOG.error("AssessmentGradingData object/id cannot be null");
			throw new IllegalArgumentException("AssessmentGradingData object/id cannot be null");
		}
		
		return adata;
	}
	
	// turn the attempt into a submission and complete any missing item grading data entries (quiz questions that were not answered)
	private void prepareAttemptForAutoSubmit(AssessmentGradingData adata, PublishedAssessmentFacade assessment, HashMap sectionSetMap,
			AssessmentGradingFacadeQueries queryFacade)
	{
		adata.setForGrade(Boolean.TRUE);
		if (adata.getTotalAutoScore() == null) {
				adata.setTotalAutoScore(0d);
		}
		if (adata.getFinalScore() == null) {
				adata.setFinalScore(0d);
		}
		if (adata.getAttemptDate() != null && assessment != null && assessment.getDueDate() != null &&
						adata.getAttemptDate().after(assessment.getDueDate())) {
				adata.setIsLate(true);
		}
		// SAM-1088
		else if (adata.getSubmittedDate() != null && assessment != null && assessment.getDueDate() != null &&
						adata.getSubmittedDate().after(assessment.getDueDate())) {
				adata.setIsLate(true);
		}

		adata.setIsAutoSubmitted(Boolean.TRUE);
		adata.setStatus(AssessmentGradingData.SUBMITTED);
		queryFacade.completeItemGradingData(adata, sectionSetMap);
	}
}
