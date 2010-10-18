/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AcceptSubmitStatus;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.AssessmentDates;
import org.etudes.mneme.api.AssessmentGrading;
import org.etudes.mneme.api.AssessmentParts;
import org.etudes.mneme.api.AssessmentPassword;
import org.etudes.mneme.api.AssessmentReview;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentSpecialAccess;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.Attribution;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Presentation;
import org.etudes.mneme.api.QuestionGrouping;
import org.etudes.mneme.api.Submission;
import org.sakaiproject.user.api.User;

/**
 * SubmissionAssessmentImpl implements Assessment, and implements the special access overrides when a submission is referencing assessment data
 */
public class SubmissionAssessmentImpl implements Assessment
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SubmissionAssessmentImpl.class);

	/** The assessment id the submission is part of, also used for getting submission-global settings. */
	protected String assessmentId = null;

	protected AssessmentService assessmentService = null;

	protected SubmissionImpl submission = null;

	/**
	 * Construct
	 */
	public SubmissionAssessmentImpl(String assessmentId, SubmissionImpl submission, AssessmentService service)
	{
		this.assessmentId = assessmentId;
		this.assessmentService = service;
		this.submission = submission;
	}

	/**
	 * Construct as a deep copy of another
	 */
	protected SubmissionAssessmentImpl(SubmissionAssessmentImpl other, SubmissionImpl submission)
	{
		set(other, submission);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// two Assessments are equals if they have the same id
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.assessmentId == null) return false;
		if (!(obj instanceof Assessment)) return false;
		Assessment a = (Assessment) obj;
		if (a.getId() == null) return false;
		if ((this.assessmentId != null) && (this.assessmentId.equals(a.getId()))) return true;

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public AcceptSubmitStatus getAcceptSubmitStatus()
	{
		return getAssessment().getAcceptSubmitStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getAllowedPoints()
	{
		return getAssessment().getAllowedPoints();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getAnonymous()
	{
		return this.getAssessment().getAnonymous();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getArchived()
	{
		return getAssessment().getArchived();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContext()
	{
		return getAssessment().getContext();
	}

	/**
	 * {@inheritDoc}
	 */
	public Attribution getCreatedBy()
	{
		return getAssessment().getCreatedBy();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentDates getDates()
	{
		// formal course evaluation does not recognize special access
		if (!getAssessment().getFormalCourseEval())
		{
			// this might be overridden in the main assessment's special access
			AssessmentAccess special = getAssessment().getSpecialAccess().getUserAccess(this.submission.getUserId());
			if (special != null)
			{
				if (special.getOverrideAcceptUntilDate() || special.getOverrideDueDate() || special.getOverrideOpenDate())
				{
					// return a special dates impl that knows how to override
					return new AssessmentDatesOverrideImpl(getAssessment(), special);
				}
			}
		}

		return getAssessment().getDates();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getFormalCourseEval()
	{
		return this.getAssessment().getFormalCourseEval();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getGradebookIntegration()
	{
		return this.getAssessment().getGradebookIntegration();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentGrading getGrading()
	{
		return getAssessment().getGrading();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasMultipleTries()
	{
		return getAssessment().getHasMultipleTries();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasPoints()
	{
		return getAssessment().getHasPoints();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasTimeLimit()
	{
		// if the main (historical) has no limit, we ignore any special access defined
		if (!getAssessment().getHasTimeLimit()) return Boolean.FALSE;

		// formal course evaluation does not recognize special access
		if (!getAssessment().getFormalCourseEval())
		{
			// this might be overridden in the main assessment's special access
			AssessmentAccess special = getAssessment().getSpecialAccess().getUserAccess(this.submission.getUserId());
			if (special != null)
			{
				if (special.getOverrideTimeLimit())
				{
					return special.getHasTimeLimit();
				}
			}
		}

		return getAssessment().getHasTimeLimit();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasTriesLimit()
	{
		// formal course evaluation does not recognize special access
		if (!getAssessment().getFormalCourseEval())
		{
			// this might be overridden in the main assessment's special access
			AssessmentAccess special = getAssessment().getSpecialAccess().getUserAccess(this.submission.getUserId());
			if (special != null)
			{
				if (special.getOverrideTries())
				{
					return special.getHasTriesLimit();
				}
			}
		}

		return getAssessment().getHasTriesLimit();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasUnscoredSubmissions()
	{
		return getAssessment().getHasUnscoredSubmissions();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return getAssessment().getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsChanged()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsClosed()
	{
		return getDates().getIsClosed();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsLive()
	{
		return getAssessment().getIsLive();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsLocked()
	{
		return getAssessment().getIsLocked();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsOpen(Boolean withGrace)
	{
		return getDates().getIsOpen(withGrace);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsPointsValid()
	{
		return getAssessment().getIsPointsValid();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsSingleQuestion()
	{
		return getAssessment().getIsSingleQuestion();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		return getAssessment().getIsValid();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMint()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Attribution getModifiedBy()
	{
		return getAssessment().getModifiedBy();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getNeedsPoints()
	{
		return getAssessment().getNeedsPoints();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentParts getParts()
	{
		return getAssessment().getParts();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentPassword getPassword()
	{
		// formal course evaluation does not recognize special access
		if (!getAssessment().getFormalCourseEval())
		{
			// this might be overridden in the main assessment's special access
			AssessmentAccess special = getAssessment().getSpecialAccess().getUserAccess(this.submission.getUserId());
			if (special != null)
			{
				if (special.getOverridePassword())
				{
					return special.getPassword();
				}
			}
		}

		return getAssessment().getPassword();
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool getPool()
	{
		return getAssessment().getPool();
	}

	/**
	 * {@inheritDoc}
	 */
	public Presentation getPresentation()
	{
		return getAssessment().getPresentation();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPublished()
	{
		return getAssessment().getPublished();
	}

	/**
	 * {@inheritDoc}
	 */
	public QuestionGrouping getQuestionGrouping()
	{
		return getAssessment().getQuestionGrouping();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getRandomAccess()
	{
		return getAssessment().getRandomAccess();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getRequireHonorPledge()
	{
		return getAssessment().getRequireHonorPledge();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getResultsEmail()
	{
		return this.getAssessment().getResultsEmail();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getResultsSent()
	{
		return this.getAssessment().getResultsSent();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentReview getReview()
	{
		return getAssessment().getReview();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Float> getScores()
	{
		return getAssessment().getScores();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getShowHints()
	{
		return getAssessment().getShowHints();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getShowModelAnswer()
	{
		return getAssessment().getShowModelAnswer();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentSpecialAccess getSpecialAccess()
	{
		return getAssessment().getSpecialAccess();
	}

	/**
	 * {@inheritDoc}
	 */
	public Submission getSubmissionContext()
	{
		// TODO: return this.submission?
		return getAssessment().getSubmissionContext();
	}

	/**
	 * {@inheritDoc}
	 */
	public Presentation getSubmitPresentation()
	{
		return getAssessment().getSubmitPresentation();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getSubmitUsers()
	{
		return this.getAssessment().getSubmitUsers();
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getTimeLimit()
	{
		// if the main has no limit, we ignore any special access defined
		if (!getAssessment().getHasTimeLimit()) return null;

		// formal course evaluation does not recognize special access
		if (!getAssessment().getFormalCourseEval())
		{
			// this might be overridden in the main assessment's special access
			AssessmentAccess special = getAssessment().getSpecialAccess().getUserAccess(this.submission.getUserId());
			if (special != null)
			{
				if (special.getOverrideTimeLimit())
				{
					return special.getTimeLimit();
				}
			}
		}

		return getAssessment().getTimeLimit();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTitle()
	{
		return getAssessment().getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getTries()
	{
		// formal course evaluation does not recognize special access
		if (!getAssessment().getFormalCourseEval())
		{
			// this might be overridden in the main assessment's special access
			AssessmentAccess special = getAssessment().getSpecialAccess().getUserAccess(this.submission.getUserId());
			if (special != null)
			{
				if (special.getOverrideTries())
				{
					return special.getTries();
				}
			}
		}

		return getAssessment().getTries();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentType getType()
	{
		return getAssessment().getType();
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return getAssessment().hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initType(AssessmentType type)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setArchived(Boolean archived)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setContext(String context)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFormalCourseEval(Boolean setting)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHasTimeLimit(Boolean hasTimeLimit)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHasTriesLimit(Boolean hasTries)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNeedsPoints(Boolean needsPoints)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPublished(Boolean published)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setQuestionGrouping(QuestionGrouping value)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRandomAccess(Boolean setting)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRequireHonorPledge(Boolean honorPledge)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setResultsEmail(String setting)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setResultsSent(Date setting)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShowHints(Boolean showHints)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShowModelAnswer(Boolean show)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTimeLimit(Long limit)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTitle(String title)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTries(Integer count)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(AssessmentType type)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Access the assessment.
	 * 
	 * @return The assessment.
	 */
	protected Assessment getAssessment()
	{
		// TODO: cache the actual assessment for the thread, to avoid the real assessment cache's copy-out policy

		AssessmentImpl rv = (AssessmentImpl) this.assessmentService.getAssessment(this.assessmentId);
		rv.initSubmissionContext(this.submission);
		return rv;
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(SubmissionAssessmentImpl other, SubmissionImpl submission)
	{
		this.assessmentService = other.assessmentService;
		this.assessmentId = other.assessmentId;
		this.submission = submission;
	}
}
