/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2013 Etudes, Inc.
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

import org.etudes.mneme.api.AssessmentReview;
import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.ReviewShowCorrect;
import org.etudes.mneme.api.ReviewTiming;

/**
 * AssessmentReviewImpl implements AssessmentReview
 */
public class AssessmentReviewImpl implements AssessmentReview
{
	protected transient AssessmentImpl assessment = null;

	protected Date date = null;

	protected Changeable owner = null;

	protected ReviewShowCorrect showCorrectAnswer = ReviewShowCorrect.yes;

	protected ReviewShowCorrect showIncorrectQuestions = ReviewShowCorrect.incorrect_only;

	protected Boolean showFeedback = Boolean.TRUE;

	protected Boolean showSummary = Boolean.FALSE;

	protected ReviewTiming timing = ReviewTiming.submitted;

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment this belongs to.
	 * @param other
	 *        The other to copy.
	 */
	public AssessmentReviewImpl(AssessmentImpl assessment, AssessmentReviewImpl other, Changeable owner)
	{
		this(assessment, owner);
		set(other);
	}

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment this belongs to.
	 */
	public AssessmentReviewImpl(AssessmentImpl assessment, Changeable owner)
	{
		this.assessment = assessment;
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getDate()
	{
		return this.date;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getNowAvailable()
	{
		// if review timing is date, we can tell without a submission
		if (this.timing == ReviewTiming.date)
		{
			if (this.date != null)
			{
				// we can now review if NOW is after the review date
				return new Date().after(this.date);
			}

			// no date? no review
			return Boolean.FALSE;
		}

		// otherwise we need a submission
		if (this.assessment.getSubmissionContext() == null) return Boolean.FALSE;

		// for submittted
		if (this.timing == ReviewTiming.submitted)
		{
			return this.assessment.getSubmissionContext().getIsComplete();
		}

		// for graded
		if (this.timing == ReviewTiming.graded)
		{
			return this.assessment.getSubmissionContext().getIsReleased();
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public ReviewShowCorrect getShowCorrectAnswer()
	{
		return this.showCorrectAnswer;
	}

	/**
	 * {@inheritDoc}
	 */
	public ReviewShowCorrect getShowIncorrectQuestions()
	{
		return this.showIncorrectQuestions;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getShowFeedback()
	{
		return this.showFeedback;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getShowSummary()
	{
		return this.showSummary;
	}

	/**
	 * {@inheritDoc}
	 */
	public ReviewTiming getTiming()
	{
		return this.timing;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDate(Date date)
	{
		if (!Different.different(this.date, date)) return;

		this.date = date;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShowCorrectAnswer(ReviewShowCorrect setting)
	{
		if (setting == null) return;
		if (this.showCorrectAnswer.equals(setting)) return;

		this.showCorrectAnswer = setting;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShowIncorrectQuestions(ReviewShowCorrect setting)
	{
		if (setting == null) return;
		if (this.showIncorrectQuestions.equals(setting)) return;

		this.showIncorrectQuestions = setting;

		this.owner.setChanged();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setShowFeedback(Boolean setting)
	{
		if (setting == null) return;
		if (this.showFeedback.equals(setting)) return;

		this.showFeedback = setting;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShowSummary(Boolean setting)
	{
		if (setting == null)
		{
			this.showSummary = Boolean.FALSE;
			return;
		}
		if (this.showSummary.equals(setting)) return;

		this.showSummary = setting;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTiming(ReviewTiming setting)
	{
		if (setting == null) throw new IllegalArgumentException();
		if (this.timing.equals(setting)) return;

		this.timing = setting;

		this.owner.setChanged();
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(AssessmentReviewImpl other)
	{
		this.date = other.date;
		this.showCorrectAnswer = other.showCorrectAnswer;
		this.showIncorrectQuestions = other.showIncorrectQuestions;
		this.showFeedback = other.showFeedback;
		this.showSummary = other.showSummary;
		this.timing = other.timing;
	}
}
