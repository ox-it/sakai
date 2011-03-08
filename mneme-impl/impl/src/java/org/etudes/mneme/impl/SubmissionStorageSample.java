/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * SubmissionStorageSample defines sample storage for Submissions.
 */
public abstract class SubmissionStorageSample implements SubmissionStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SubmissionStorageSample.class);

	protected AssessmentService assessmentService = null;

	protected Object idGenerator = new Object();

	protected MnemeService mnemeService = null;

	protected long nextAnswerId = 100;

	protected long nextSubmissionId = 100;

	protected SecurityService securityService = null;

	protected SessionManager sessionManager = null;

	protected Map<String, SubmissionImpl> submissions = new LinkedHashMap<String, SubmissionImpl>();

	protected SubmissionServiceImpl submissionService = null;

	/**
	 * {@inheritDoc}
	 */
	public SubmissionImpl clone(SubmissionImpl other)
	{
		return new SubmissionImpl(other);
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> findPartQuestions(Part part)
	{
		List<String> rv = new ArrayList<String>();

		// check the submissions to this assessment
		for (SubmissionImpl submission : this.submissions.values())
		{
			// TODO: only for complete? && submission.getIsComplete() and only if answered
			if (submission.getAssessment().equals(part.getAssessment()) && submission.getIsComplete() && (!submission.getIsTestDrive()))
			{
				for (Answer answer : submission.getAnswers())
				{
					if (((AnswerImpl) answer).getPartId().equals(part.getId()) && answer.getIsAnswered())
					{
						if (!rv.contains(answer.getQuestion().getId()))
						{
							rv.add(answer.getQuestion().getId());
						}
					}
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Answer getAnswer(String answerId)
	{
		for (SubmissionImpl submission : this.submissions.values())
		{
			for (Answer answer : submission.getAnswers())
			{
				if (answer.getId().equals(answerId))
				{
					return answer;
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SubmissionImpl> getAssessmentCompleteSubmissions(Assessment assessment)
	{
		// collect the submissions to this assessment
		List<SubmissionImpl> rv = new ArrayList<SubmissionImpl>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getIsComplete() && submission.getAssessment().equals(assessment))
			{
				rv.add(new SubmissionImpl(submission));
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAssessmentHasUnscoredSubmissions(Assessment assessment)
	{
		List<String> rv = new ArrayList<String>();

		// check the submissions to this assessment
		for (SubmissionImpl submission : this.submissions.values())
		{
			// if any for this assessment are complete and not released, the assessment is not fully released
			if (submission.getAssessment().equals(assessment) && submission.getIsComplete() && (!submission.getIsTestDrive().booleanValue())
					&& (submission.getEvaluation().getScore() == null) && (!submission.getEvaluation().getEvaluated().booleanValue()))
			{
				for (Answer answer : submission.getAnswers())
				{
					if ((answer.getIsAnswered()) && (answer.getTotalScore() == null) && (!answer.getQuestion().getIsSurvey().booleanValue()))
					{
						if (!rv.contains(submission.getUserId()))
						{
							rv.add(submission.getUserId());
						}
					}
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Float> getAssessmentHighestScores(Assessment assessment, Boolean releasedOnly)
	{
		Map<String, Float> rv = new HashMap<String, Float>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().equals(assessment) && submission.getIsComplete() && (!submission.getIsTestDrive()))
			{
				if (releasedOnly && !submission.getIsReleased()) continue;

				Float total = submission.getTotalScore();
				if (total != null)
				{
					Float prior = rv.get(submission.getUserId());
					if (prior != null)
					{
						if (prior.floatValue() < total.floatValue())
						{
							rv.put(submission.getUserId(), total);
						}
					}
					else
					{
						rv.put(submission.getUserId(), total);
					}
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAssessmentQuestionHasUnscoredSubmissions(Assessment assessment, Question question)
	{
		List<String> rv = new ArrayList<String>();

		// check the submissions to this assessment
		for (SubmissionImpl submission : this.submissions.values())
		{
			// if any submissions that are for this assessment are complete and not released, the assessment is not fully released
			if (submission.getAssessment().equals(assessment) && submission.getIsComplete() && (!submission.getIsTestDrive().booleanValue())
					&& (submission.getEvaluation().getScore() == null) && (!submission.getEvaluation().getEvaluated().booleanValue()))
			{
				for (Answer answer : submission.getAnswers())
				{
					if ((answer.getQuestion().equals(question)) && (answer.getIsAnswered()) && (answer.getTotalScore() == null))
					{
						if (!rv.contains(submission.getUserId()))
						{
							rv.add(submission.getUserId());
						}
					}
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Float> getAssessmentScores(Assessment assessment)
	{
		List<Float> rv = new ArrayList<Float>();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SubmissionImpl> getAssessmentSubmissions(Assessment assessment)
	{
		// collect the submissions to this assessment
		List<SubmissionImpl> rv = new ArrayList<SubmissionImpl>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().equals(assessment) && (!submission.getIsTestDrive()))
			{
				rv.add(new SubmissionImpl(submission));
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<? extends SubmissionImpl> getContextSubmissions(String context)
	{
		List<SubmissionImpl> rv = new ArrayList<SubmissionImpl>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			// find those in the context, filter out archived assessments
			if (submission.getAssessment().getContext().equals(context) && (!submission.getAssessment().getArchived()))
			{
				rv.add(new SubmissionImpl(submission));
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SubmissionImpl> getOpenSubmissions()
	{
		List<SubmissionImpl> rv = new ArrayList<SubmissionImpl>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (!submission.getIsComplete())
			{
				rv.add(new SubmissionImpl(submission));
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Float> getQuestionScores(Question question)
	{
		List<Float> rv = new ArrayList<Float>();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public SubmissionImpl getSubmission(String id)
	{
		SubmissionImpl rv = this.submissions.get(id);
		if (rv != null)
		{
			rv = new SubmissionImpl(rv);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getSubmissionHighestScore(Assessment assessment, String userId)
	{
		Float rv = null;
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().equals(assessment) && submission.getUserId().equals(userId) && submission.getIsComplete()
					&& submission.getIsReleased() && ((rv == null) || (submission.getTotalScore() > rv.floatValue())))
			{
				rv = submission.getTotalScore();
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getSubmissionScore(Submission submission)
	{
		SubmissionImpl s = getSubmission(submission.getId());
		if (s != null)
		{
			return s.getTotalScore();
		}

		return 0f;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SubmissionImpl> getUserAssessmentSubmissions(Assessment assessment, String userId)
	{
		List<SubmissionImpl> rv = new ArrayList<SubmissionImpl>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			// find those to this assessment for this user
			if (submission.getAssessment().equals(assessment) && submission.getUserId().equals(userId))
			{
				rv.add(new SubmissionImpl(submission));
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SubmissionImpl> getUserContextSubmissions(String context, String userId, Boolean publishedOnly)
	{
		List<SubmissionImpl> rv = new ArrayList<SubmissionImpl>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			// find those in the context for this user, filter out archived and un-published assessments
			if (submission.getAssessment().getContext().equals(context) && submission.getUserId().equals(userId)
					&& (!submission.getAssessment().getArchived()))
			{
				if (publishedOnly && !submission.getAssessment().getPublished()) continue;

				rv.add(new SubmissionImpl(submission));
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getUsersSubmitted(Assessment assessment)
	{
		List<String> rv = new ArrayList<String>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().equals(assessment) && !rv.contains(submission.getUserId()))
			{
				rv.add(submission.getUserId());
			}
		}

		return rv;
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
	public abstract AnswerImpl newAnswer();

	/**
	 * {@inheritDoc}
	 */
	public abstract SubmissionImpl newSubmission();

	// /**
	// * {@inheritDoc}
	// */
	// public void removeIncompleteAssessmentSubmissions(Assessment assessment)
	// {
	// for (Iterator i = this.submissions.values().iterator(); i.hasNext();)
	// {
	// SubmissionImpl submission = (SubmissionImpl) i.next();
	// if (submission.getAssessment().equals(assessment) && (!submission.getIsComplete()))
	// {
	// i.remove();
	// }
	// }
	// }

	// /**
	// * {@inheritDoc}
	// */
	// public void removeSubmission(SubmissionImpl submission)
	// {
	// this.submissions.remove(submission.getId());
	// }

	/**
	 * {@inheritDoc}
	 */
	public void removeTestDriveSubmissions(Assessment assessment)
	{
		List<String> ids = new ArrayList<String>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().equals(assessment) && submission.getIsTestDrive())
			{
				ids.add(submission.getId());
			}
		}

		for (String id : ids)
		{
			this.submissions.remove(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeTestDriveSubmissions(String context)
	{
		List<String> ids = new ArrayList<String>();
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().getContext().equals(context) && submission.getIsTestDrive())
			{
				ids.add(submission.getId());
			}
		}

		for (String id : ids)
		{
			this.submissions.remove(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveAnswers(List<Answer> answers)
	{
		// for each answer, place it into the submission replacing the answer we have or adding
		for (Answer a : answers)
		{
			// if there is no id, assign one
			if (a.getId() == null)
			{
				long id = 0;
				synchronized (this.idGenerator)
				{
					id = this.nextAnswerId;
					this.nextAnswerId++;
				}
				((AnswerImpl) a).initId("n" + Long.toString(id));
			}

			// find the submission
			SubmissionImpl s = this.submissions.get(a.getSubmission().getId());
			if (s != null)
			{
				// replace or add the answer
				s.replaceAnswer((AnswerImpl) a);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveSubmission(SubmissionImpl submission)
	{
		// assign an id
		if (submission.getId() == null)
		{
			long id = 0;
			synchronized (this.idGenerator)
			{
				id = this.nextSubmissionId;
				this.nextSubmissionId++;
			}
			submission.initId("s" + Long.toString(id));
		}

		else if (submission.getId().startsWith(SubmissionService.PHANTOM_PREFIX))
		{
			// lets not save phanton submissions
			throw new IllegalArgumentException();
		}

		// if we have this already, update ONLY the main information, not the answers
		SubmissionImpl old = this.submissions.get(submission.getId());
		if (old != null)
		{
			old.setMain(submission);
		}

		// otherwise save it w/ no answers
		else
		{
			SubmissionImpl s = new SubmissionImpl(submission);
			s.clearAnswers();
			this.submissions.put(submission.getId(), s);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveSubmissionEvaluation(SubmissionImpl submission)
	{
		if (submission.getId().startsWith(SubmissionService.PHANTOM_PREFIX))
		{
			// lets not save phanton submissions
			throw new IllegalArgumentException();
		}

		// has to be an existing saved submission
		if (submission.getId() == null) throw new IllegalArgumentException();

		// we must already have the submission
		SubmissionImpl old = this.submissions.get(submission.getId());
		if (old == null) throw new IllegalArgumentException();

		// update the submission evaluation
		old.evaluation.set(submission.evaluation);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveSubmissionReleased(SubmissionImpl submission)
	{
		if (submission.getId().startsWith(SubmissionService.PHANTOM_PREFIX))
		{
			// lets not save phanton submissions
			throw new IllegalArgumentException();
		}

		// has to be an existing saved submission
		if (submission.getId() == null) throw new IllegalArgumentException();

		// we must already have the submission
		SubmissionImpl old = this.submissions.get(submission.getId());
		if (old == null) throw new IllegalArgumentException();

		// update the submission evaluation
		old.released = submission.released;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMnemeService(MnemeService service)
	{
		this.mnemeService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSessionManager(SessionManager service)
	{
		this.sessionManager = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSubmissionService(SubmissionServiceImpl service)
	{
		this.submissionService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean submissionsDependsOn(Question question)
	{
		// for all submissions in the context
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().getContext().equals(question.getContext()))
			{
				// check the answers
				for (Answer answer : submission.getAnswers())
				{
					if (((AnswerImpl) answer).questionId.equals(question.getId()))
					{
						return Boolean.TRUE;
					}
				}
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean submissionsExist(Assessment assessment)
	{
		for (SubmissionImpl submission : this.submissions.values())
		{
			if (submission.getAssessment().equals(assessment))
			{
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
