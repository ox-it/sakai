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

import org.etudes.mneme.api.AssessmentGrading;
import org.etudes.mneme.api.Changeable;

/**
 * AssessmentGradingImpl implements AssessmentGrading
 */
public class AssessmentGradingImpl implements AssessmentGrading
{
	protected Boolean anonymous = Boolean.FALSE;

	protected Boolean autoRelease = Boolean.TRUE;

	/** Track the original auto-release value. */
	protected transient Boolean autoReleaseWas = Boolean.TRUE;

	protected Boolean gradebookIntegration = Boolean.FALSE;

	/** Track the original gradebookIntegration value. */
	protected transient Boolean gradebookIntegrationWas = Boolean.FALSE;

	protected Boolean gradebookRejectedAssessment = Boolean.FALSE;

	protected transient Changeable owner = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public AssessmentGradingImpl(AssessmentGradingImpl other, Changeable owner)
	{
		this(owner);
		set(other);
	}

	/**
	 * Construct.
	 */
	public AssessmentGradingImpl(Changeable owner)
	{
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getAnonymous()
	{
		return this.anonymous;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getAutoRelease()
	{
		return this.autoRelease;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getGradebookIntegration()
	{
		return this.gradebookIntegration;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getGradebookRejectedAssessment()
	{
		return this.gradebookRejectedAssessment;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		return Boolean.valueOf(!gradebookRejectedAssessment);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnonymous(Boolean setting)
	{
		if (setting == null) throw new IllegalArgumentException();
		if (this.anonymous.equals(setting)) return;

		this.anonymous = setting;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAutoRelease(Boolean setting)
	{
		if (setting == null) throw new IllegalArgumentException();
		if (this.autoRelease.equals(setting)) return;

		this.autoRelease = setting;

		this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setGradebookIntegration(Boolean setting)
	{
		if (setting == null) throw new IllegalArgumentException();
		if (this.gradebookIntegration.equals(setting)) return;

		this.gradebookIntegration = setting;

		// clear the invalid flag if we are no longer integrated
		if (!this.gradebookIntegration)
		{
			this.gradebookRejectedAssessment = Boolean.FALSE;
		}

		this.owner.setChanged();
	}

	/**
	 * Check if the auto-release setting was changed.
	 * 
	 * @return TRUE if changed, FALSE if not.
	 */
	protected Boolean getAutoReleaseChanged()
	{
		Boolean rv = Boolean.valueOf(!this.autoRelease.equals(this.autoReleaseWas));
		return rv;
	}

	/**
	 * Check if the gradebookIntegration setting was changed.
	 * 
	 * @return TRUE if changed, FALSE if not.
	 */
	protected Boolean getGradebookIntegrationChanged()
	{
		Boolean rv = Boolean.valueOf(!this.gradebookIntegration.equals(this.gradebookIntegrationWas));
		return rv;
	}

	/**
	 * Initialize the auto release, and set the "was" to the same.
	 * 
	 * @param autoRelease
	 *        The auto-release setting.
	 */
	protected void initAutoRelease(Boolean autoRelease)
	{
		this.autoRelease = autoRelease;
		this.autoReleaseWas = autoRelease;
	}

	/**
	 * Initialize the gradebook integration, and set the "was" to the same.
	 * 
	 * @param gradebookIntegration
	 *        The gradebookIntegration setting.
	 */
	protected void initGradebookIntegration(Boolean gradebookIntegration)
	{
		this.gradebookIntegration = gradebookIntegration;
		this.gradebookIntegrationWas = gradebookIntegration;
	}

	/**
	 * Initialize the gradebookRejectedAssessment setting.
	 * 
	 * @param gradebookRejectedAssessment
	 *        The gradebookRejectedAssessment setting.
	 */
	protected void initGradebookRejectedAssessment(Boolean gradebookRejectedAssessment)
	{
		this.gradebookRejectedAssessment = gradebookRejectedAssessment;
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(AssessmentGradingImpl other)
	{
		this.autoRelease = other.autoRelease;
		this.autoReleaseWas = other.autoReleaseWas;
		this.gradebookIntegration = other.gradebookIntegration;
		this.gradebookIntegrationWas = other.gradebookIntegrationWas;
		this.gradebookRejectedAssessment = other.gradebookRejectedAssessment;
		this.anonymous = other.anonymous;
	}
}
