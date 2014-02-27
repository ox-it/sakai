/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.GradesRejectsAssessmentException;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * GradesServiceGradebook23Impl implements GradesService, using the Sakai Gradebook, version 2.3, as the grading authority.
 */
public class GradesServiceGradebook23Impl implements GradesService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(GradesServiceGradebook23Impl.class);

	/** Our application name in the grade book UI. */
	protected static final String APPLICATION_NAME = "AT&S";

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService = null;

	/** Dependency: GradebookExternalAssessmentService */
	// for 2.4 only: protected GradebookExternalAssessmentService m_gradebookService = null;
	protected GradebookService gradebookService = null;

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** The site service. */
	protected SiteService siteService = null;

	/** Dependency: SubmissionService */
	protected SubmissionService submissionService = null;

	/** Dependency: ThreadLocalManager */
	protected ThreadLocalManager threadLocalManager = null;

	/** Dependency: UserDirectoryService. */
	protected UserDirectoryService userDirectoryService = null;

	/**
	 * {@inheritDoc}
	 */
	public Boolean assessmentReported(Assessment assessment)
	{
		if (assessment == null) throw new IllegalArgumentException();

		try
		{
			boolean hasGradebook = gradebookService.isGradebookDefined(assessment.getContext());
			if (hasGradebook && (assessment.getTitle() != null))
			{
				boolean reported = gradebookService.isExternalAssignmentDefined(assessment.getContext(), assessment.getTitle());
				return Boolean.valueOf(reported);
			}
		}
		catch (GradebookNotFoundException e)
		{
			M_log.warn("assessmentReported:" + e.toString());
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean available(String context)
	{
		Site site = null;
		try
		{
			site = siteService.getSite(context);
		}
		catch (Exception e)
		{
			M_log.debug("Exception thrown while getting site" + e.toString());
		}
		boolean hasGradebook = gradebookService.isGradebookDefined(context) && site.getToolForCommonId("sakai.gradebook.tool") != null;
		return Boolean.valueOf(hasGradebook);
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean initAssessmentGrades(Assessment assessment) throws GradesRejectsAssessmentException
	{
		if (assessment == null) throw new IllegalArgumentException();

		// make sure we are published, valid, have a title, and desire gradebook integration
		if (!(assessment.getPublished() && assessment.getGrading().getGradebookIntegration() && assessment.getIsValid() && (assessment.getTitle() != null)))
			return Boolean.FALSE;

		// make sure we don't already have an entry
		if (assessmentReported(assessment))
		{
			throw new GradesRejectsAssessmentException();
		}

		M_log.debug("initAssessmentGrades: " + assessment.getId());

		try
		{
			// make sure there's a gradebook
			boolean hasGradebook = gradebookService.isGradebookDefined(assessment.getContext());
			if (hasGradebook)
			{
				// TODO: what url to use?
				String url = null;

				// make an entry for the assessment
				gradebookService.addExternalAssessment(assessment.getContext(), assessment.getTitle(), url, assessment.getTitle(),
						toDoubleScore(assessment.getParts().getTotalPoints()), assessment.getDates().getDueDate(), APPLICATION_NAME);
				return Boolean.TRUE;
			}
		}
		catch (GradebookNotFoundException e)
		{
			M_log.warn("initAssessmentGrades: " + assessment.getId() + e.toString());
		}
		catch (ConflictingAssignmentNameException e)
		{
			throw new GradesRejectsAssessmentException();
		}
		catch (ConflictingExternalIdException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}
		catch (AssignmentHasIllegalPointsException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean reportAssessmentGrades(Assessment assessment)
	{
		if (assessment == null) throw new IllegalArgumentException();

		// make sure we are published, valid, have a title, and desire gradebook integration
		if (!(assessment.getPublished() && assessment.getGrading().getGradebookIntegration() && assessment.getIsValid() && (assessment.getTitle() != null)))
			return Boolean.FALSE;

		// make sure our assessment is in the gradebook
		if (!assessmentReported(assessment)) return Boolean.FALSE;

		M_log.debug("reportAssessmentGrades: " + assessment.getId());

		try
		{
			// make sure there's a gradebook
			boolean hasGradebook = gradebookService.isGradebookDefined(assessment.getContext());
			if (hasGradebook)
			{
				// get the "official" submissions map of user id -> Float score (for released completed submissions)
				Map<String, Float> scores = this.submissionService.getAssessmentHighestScores(assessment, Boolean.TRUE);

				// make them double for gb
				Map<String, Double> dScores = new HashMap<String, Double>();
				for (Map.Entry entry : scores.entrySet())
				{
					String key = (String) entry.getKey();
					Float total = (Float) entry.getValue();
					dScores.put(key, (total == null) ? null : Double.valueOf(toDoubleScore(total)));
				}

				// report them
				gradebookService.updateExternalAssessmentScores(assessment.getContext(), assessment.getTitle(), dScores);

				return Boolean.TRUE;
			}
		}
		catch (GradebookNotFoundException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}
		catch (AssessmentNotFoundException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean reportSubmissionGrade(Submission submission)
	{
		if (submission == null) throw new IllegalArgumentException();

		// make sure we are published, valid and desire gradebook integration
		Assessment assessment = submission.getAssessment();
		if (!(assessment.getPublished() && assessment.getGrading().getGradebookIntegration() && assessment.getIsValid() && (assessment.getTitle() != null)))
			return Boolean.FALSE;

		// if this one is not complete, it will not cause a change in what we report
		if (!submission.getIsComplete()) return Boolean.FALSE;

		M_log.debug("reportSubmissionGrade: " + submission.getId());

		try
		{
			// make sure there's an entry
			boolean hasGradebook = gradebookService.isGradebookDefined(assessment.getContext());
			if (hasGradebook)
			{
				boolean reported = gradebookService.isExternalAssignmentDefined(assessment.getContext(), assessment.getTitle());
				if (reported)
				{
					Double dScore = null;

					// get the official (highest) score for this submission, considering this and all other completed and released submissions
					Float score = this.submissionService.getSubmissionOfficialScore(assessment, submission.getUserId());
					if (score != null)
					{
						dScore = Double.valueOf(toDoubleScore(score));
					}

					// report it
					gradebookService.updateExternalAssessmentScore(assessment.getContext(), assessment.getTitle(), submission.getUserId(), dScore);

					return Boolean.TRUE;
				}
			}
		}
		catch (GradebookNotFoundException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}
		catch (AssessmentNotFoundException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean retractAssessmentGrades(Assessment assessment)
	{
		M_log.debug("retractAssessmentGrades: " + assessment.getId());

		try
		{
			boolean hasGradebook = gradebookService.isGradebookDefined(assessment.getContext());
			if (hasGradebook && (assessment.getTitle() != null))
			{
				boolean reported = gradebookService.isExternalAssignmentDefined(assessment.getContext(), assessment.getTitle());
				if (reported)
				{
					gradebookService.removeExternalAssessment(assessment.getContext(), assessment.getTitle());
					return Boolean.TRUE;
				}
			}
		}
		catch (GradebookNotFoundException e)
		{
			M_log.warn("retractAssessmentGrades: " + e.toString());
		}
		catch (AssessmentNotFoundException e)
		{
			M_log.warn("retractAssessmentGrades" + e.toString());
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean retractSubmissionGrade(Submission submission)
	{
		Assessment assessment = submission.getAssessment();

		M_log.debug("retractSubmissionGrade: " + submission.getId());

		try
		{
			// make sure there's an entry
			boolean hasGradebook = gradebookService.isGradebookDefined(assessment.getContext());
			if (hasGradebook && (assessment.getTitle() != null))
			{
				boolean reported = gradebookService.isExternalAssignmentDefined(assessment.getContext(), assessment.getTitle());
				if (reported)
				{
					// null retracts the score
					Double score = null;

					// report it
					gradebookService.updateExternalAssessmentScore(assessment.getContext(), assessment.getTitle(), submission.getUserId(), score);

					return Boolean.TRUE;
				}
			}
		}
		catch (GradebookNotFoundException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}
		catch (AssessmentNotFoundException e)
		{
			M_log.warn("reportAssessmentGrades: " + assessment.getId() + e.toString());
		}

		return Boolean.FALSE;
	}

	/**
	 * Dependency: AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Dependency: GradebookService.
	 * 
	 * @param service
	 *        The GradebookService.
	 */
	public void setGradebookService(/* for 2.4 only: GradebookExternalAssessmentService */GradebookService service)
	{
		this.gradebookService = service;
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Dependency: SiteService.
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		this.siteService = service;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		this.sessionManager = service;
	}

	/**
	 * Dependency: SubmissionService.
	 * 
	 * @param service
	 *        The SubmissionService.
	 */
	public void setSubmissionService(SubmissionService service)
	{
		this.submissionService = service;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The ThreadLocalManager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		this.threadLocalManager = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUserDirectoryService(UserDirectoryService service)
	{
		this.userDirectoryService = service;
	}

	/**
	 * Convert the points / score value into a double, without picking up float to double conversion junk
	 * 
	 * @param score
	 *        The value to convert.
	 * @return The converted value.
	 */
	protected double toDoubleScore(Float score)
	{
		if (score == null) return 0.0d;

		// we want only .xx precision, and we don't want any double junk from the float to double conversion
		float times100 = score.floatValue() * 100f;

		Double rv = (double) times100;

		rv = rv / 100d;
		return rv;
	}
}
