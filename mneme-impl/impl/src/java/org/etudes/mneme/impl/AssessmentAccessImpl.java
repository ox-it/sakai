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
import java.util.Date;
import java.util.List;

import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.AssessmentPassword;
import org.etudes.mneme.api.Changeable;

/**
 * AssessmentAccessImpl implements AssessmentAccess
 */
public class AssessmentAccessImpl implements AssessmentAccess, Changeable
{
	protected Date acceptUntilDate = null;

	protected transient Assessment assessment = null;

	protected Date dueDate = null;

	protected String id = null;

	protected Date openDate = null;

	protected Boolean overrideAcceptUntilDate = Boolean.FALSE;

	protected Boolean overrideDueDate = Boolean.FALSE;

	protected Boolean overrideOpenDate = Boolean.FALSE;

	protected Boolean overridePassword = Boolean.FALSE;

	protected Boolean overrideTimeLimit = Boolean.FALSE;

	protected Boolean overrideTries = Boolean.FALSE;

	protected Changeable owner = null;

	protected AssessmentPasswordImpl password = null;

	protected Long timeLimit = null;

	protected Integer tries = null;

	protected List<String> userIds = new ArrayList<String>();

	boolean changed = false;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public AssessmentAccessImpl(Assessment assessment, AssessmentAccessImpl other, Changeable owner)
	{
		this.owner = owner;
		this.assessment = assessment;
		set(other);
	}

	/**
	 * Construct.
	 * 
	 * @param assessment
	 *        The assessment this is the parts for.
	 * @param assessmentService
	 *        The AssessmentService.
	 * @param questionService
	 *        The QuestionService.
	 * @param poolService
	 *        The PoolService.
	 */
	public AssessmentAccessImpl(Assessment assessment, Changeable owner)
	{
		this.owner = owner;
		this.password = new AssessmentPasswordImpl(owner);
		this.assessment = assessment;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearChanged()
	{
		this.changed = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// two are equals if they have the same id
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		if ((this.id == null) || (((AssessmentAccessImpl) obj).id == null)) return false;
		return this.id.equals(((AssessmentAccessImpl) obj).id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getAcceptUntilDate()
	{
		if (!this.overrideAcceptUntilDate) return this.assessment.getDates().getAcceptUntilDate();

		return this.acceptUntilDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getChanged()
	{
		return Boolean.valueOf(this.changed);
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getDueDate()
	{
		if (!this.overrideDueDate) return this.assessment.getDates().getDueDate();

		return this.dueDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasTimeLimit()
	{
		if (!this.overrideTimeLimit) return this.assessment.getHasTimeLimit();

		return Boolean.valueOf(this.timeLimit != null);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasTriesLimit()
	{
		if (!this.overrideTries) return this.assessment.getHasTriesLimit();

		return Boolean.valueOf(this.tries != null);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// open, if defined, must be before acceptUntil and due, if defined
		if ((getOpenDate() != null) && (getDueDate() != null) && (!getOpenDate().before(getDueDate()))) return Boolean.FALSE;
		if ((getOpenDate() != null) && (getAcceptUntilDate() != null) && (!getOpenDate().before(getAcceptUntilDate()))) return Boolean.FALSE;

		// due, if defined, must be not after acceptUntil, if defined
		if ((getDueDate() != null) && (getAcceptUntilDate() != null) && (getDueDate().after(getAcceptUntilDate()))) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getOpenDate()
	{
		if (!this.overrideOpenDate) return this.assessment.getDates().getOpenDate();

		return this.openDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOverrideAcceptUntilDate()
	{
		return this.overrideAcceptUntilDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOverrideDueDate()
	{
		return this.overrideDueDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOverrideOpenDate()
	{
		return this.overrideOpenDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOverridePassword()
	{
		return this.overridePassword;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOverrideTimeLimit()
	{
		return this.overrideTimeLimit;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOverrideTries()
	{
		return this.overrideTries;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentPassword getPassword()
	{
		if (!this.overridePassword) return this.assessment.getPassword();

		return this.password;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPasswordValue()
	{
		if (!this.overridePassword) return this.assessment.getPassword().getPassword();

		return this.password.getPassword();
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getTimeLimit()
	{
		if (!this.overrideTimeLimit) return this.assessment.getTimeLimit();

		return this.timeLimit;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getTries()
	{
		if (!this.overrideTries) return this.assessment.getTries();

		return this.tries;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getUsers()
	{
		return this.userIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return this.id == null ? "null".hashCode() : this.id.hashCode();
	}

	/**
	 * Init the override password.
	 * 
	 * @param override
	 *        The override.
	 */
	public void initOverridePassword(Boolean override)
	{
		this.overridePassword = override;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean isForUser(String userId)
	{
		return Boolean.valueOf(this.userIds.contains(userId));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAcceptUntilDate(Date date)
	{
		boolean override = false;
		Date d = null;

		// compute what we should have based on the new setting and the assessment setting
		if (!Different.different(date, this.assessment.getDates().getAcceptUntilDate()))
		{
			override = false;
			d = null;
		}

		else
		{
			override = true;
			d = date;
		}

		// if we already have this, we are done
		if (!Different.different(d, this.acceptUntilDate) && (override == this.overrideAcceptUntilDate)) return;

		this.overrideAcceptUntilDate = override;
		this.acceptUntilDate = d;

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChanged()
	{
		this.changed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDueDate(Date date)
	{
		boolean override = false;
		Date d = null;

		// compute what we should have based on the new setting and the assessment setting
		if (!Different.different(date, this.assessment.getDates().getDueDate()))
		{
			override = false;
			d = null;
		}

		else
		{
			override = true;
			d = date;
		}

		// if we already have this, we are done
		if (!Different.different(d, this.dueDate) && (override == this.overrideDueDate)) return;

		this.overrideDueDate = override;
		this.dueDate = d;

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHasTimeLimit(Boolean hasTimeLimit)
	{
		if (hasTimeLimit == null) throw new IllegalArgumentException();

		// ignore any positive setting - the negative ones are what count
		if (hasTimeLimit) return;

		boolean override = false;

		// check against the real assessment setting for a difference
		if (this.assessment.getHasTimeLimit())
		{
			override = true;
		}
		else
		{
			override = false;
		}

		// check for a real difference
		if (override == this.overrideTimeLimit) return;

		this.overrideTimeLimit = override;
		this.timeLimit = null;

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHasTriesLimit(Boolean hasTriesLimit)
	{
		if (hasTriesLimit == null) throw new IllegalArgumentException();

		// ignore any positive setting - the negative ones are what count
		if (hasTriesLimit) return;

		boolean override = false;

		// check against the real assessment setting for a difference
		if (this.assessment.getHasTriesLimit())
		{
			override = true;
		}
		else
		{
			override = false;
		}

		// check for a real difference
		if (this.overrideTries == override) return;

		this.overrideTries = override;
		this.tries = null;

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOpenDate(Date date)
	{
		boolean override = false;
		Date d = null;

		// compute what we should have based on the new setting and the assessment setting
		if (!Different.different(date, this.assessment.getDates().getOpenDate()))
		{
			override = false;
			d = null;
		}

		else
		{
			override = true;
			d = date;
		}

		// if we already have this, we are done
		if (!Different.different(d, this.openDate) && (override == this.overrideOpenDate)) return;

		this.overrideOpenDate = override;
		this.openDate = d;

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOverrideAcceptUntilDate(Boolean override)
	{
		if (override == null) throw new IllegalArgumentException();
		if (override.equals(this.overrideAcceptUntilDate)) return;

		this.overrideAcceptUntilDate = override;
		if (!override)
		{
			this.acceptUntilDate = null;
		}

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOverrideDueDate(Boolean override)
	{
		if (override == null) throw new IllegalArgumentException();
		if (override.equals(this.overrideDueDate)) return;

		this.overrideDueDate = override;
		if (!override)
		{
			this.dueDate = null;
		}

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOverrideOpenDate(Boolean override)
	{
		if (override == null) throw new IllegalArgumentException();
		if (override.equals(this.overrideOpenDate)) return;

		this.overrideOpenDate = override;
		if (!override)
		{
			this.openDate = null;
		}

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOverridePassword(Boolean override)
	{
		if (override == null) throw new IllegalArgumentException();
		if (override.equals(this.overridePassword)) return;

		this.overridePassword = override;
		if (!override)
		{
			this.password = null;
		}

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOverrideTimeLimit(Boolean override)
	{
		if (override == null) throw new IllegalArgumentException();
		if (override.equals(this.overrideTimeLimit)) return;

		this.overrideTimeLimit = override;
		if (!override)
		{
			this.timeLimit = null;
		}

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOverrideTries(Boolean override)
	{
		if (override == null) throw new IllegalArgumentException();
		if (override.equals(this.overrideTries)) return;

		this.overrideTries = override;
		if (!override)
		{
			this.tries = null;
		}

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPasswordValue(String password)
	{
		// massage the password
		if (password != null)
		{
			password = password.trim();
			if (password.length() > 255) password = password.substring(0, 255);
			if (password.length() == 0) password = null;
		}

		boolean override = false;
		String pw = null;

		if (!Different.different(password, this.assessment.getPassword().getPassword()))
		{
			override = false;
			pw = null;
		}

		else
		{
			override = true;
			pw = password;
		}

		if (!Different.different(pw, this.password.getPassword()) && (override == this.overridePassword)) return;

		this.overridePassword = override;
		this.password.setPassword(pw);

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTimeLimit(Long limit)
	{
		boolean override = false;
		Long tl = null;

		if (!Different.different(limit, this.assessment.getTimeLimit()))
		{
			override = false;
			tl = null;
		}

		else
		{
			override = true;
			tl = limit;
		}

		if (!Different.different(tl, this.timeLimit) && (override == this.overrideTimeLimit)) return;

		this.overrideTimeLimit = override;
		this.timeLimit = tl;

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTries(Integer count)
	{
		boolean override = false;
		Integer t = null;

		if (!Different.different(count, this.assessment.getTries()))
		{
			override = false;
			t = null;
		}

		else
		{
			override = true;
			t = count;
		}

		if (!Different.different(t, this.tries) && (override == this.overrideTries)) return;

		this.overrideTries = override;
		this.tries = t;

		this.owner.setChanged();
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUsers(List<String> newIds)
	{
		// has anything changed?
		if ((newIds == null) && this.userIds.isEmpty()) return;
		boolean changed = false;
		if (newIds != null)
		{
			for (String newId : newIds)
			{
				if (!this.userIds.contains(newId))
				{
					changed = true;
					break;
				}
			}
			for (String oldId : this.userIds)
			{
				if (!newIds.contains(oldId))
				{
					changed = true;
					break;
				}
			}
		}
		else
		{
			if (!this.userIds.isEmpty()) changed = true;
		}

		if (!changed) return;

		this.userIds.clear();
		if (newIds != null)
		{
			this.userIds.addAll(newIds);
		}

		this.owner.setChanged();
		setChanged();

		// make sure that this access is the only one that pertains to these users
		((AssessmentSpecialAccessImpl) this.assessment.getSpecialAccess()).assureSingleAccessForUser(this);
	}

	/**
	 * Init the accept until date.
	 * 
	 * @param date
	 *        The date.
	 */
	protected void initAcceptUntilDate(Date date)
	{
		this.acceptUntilDate = date;
	}

	/**
	 * Init the due date.
	 * 
	 * @param date
	 *        The date.
	 */
	protected void initDueDate(Date date)
	{
		this.dueDate = date;
	}

	/**
	 * Establish the id.
	 * 
	 * @param id
	 *        The part id.
	 */
	protected void initId(String id)
	{
		this.id = id;
	}

	/**
	 * Init the open date.
	 * 
	 * @param date
	 *        The date.
	 */
	protected void initOpenDate(Date date)
	{
		this.openDate = date;
	}

	/**
	 * Init the override accept until date.
	 * 
	 * @param override
	 *        The override.
	 */
	protected void initOverrideAcceptUntilDate(Boolean override)
	{
		this.overrideAcceptUntilDate = override;
	}

	/**
	 * Init the override due date.
	 * 
	 * @param override
	 *        The override.
	 */
	protected void initOverrideDueDate(Boolean override)
	{
		this.overrideDueDate = override;
	}

	/**
	 * Init the override open date.
	 * 
	 * @param override
	 *        The override.
	 */
	protected void initOverrideOpenDate(Boolean override)
	{
		this.overrideOpenDate = override;
	}

	/**
	 * Init the override time limit.
	 * 
	 * @param override
	 *        The override.
	 */
	protected void initOverrideTimeLimit(Boolean override)
	{
		this.overrideTimeLimit = override;
	}

	/**
	 * Init the override tries.
	 * 
	 * @param override
	 *        The override.
	 */
	protected void initOverrideTries(Boolean override)
	{
		this.overrideTries = override;
	}

	/**
	 * Init the password.
	 * 
	 * @param password
	 *        The password.
	 */
	protected void initPasswordValue(String password)
	{
		// massage the password
		if (password != null)
		{
			password = password.trim();
			if (password.length() > 255) password = password.substring(0, 255);
			if (password.length() == 0) password = null;
		}

		this.password.setPassword(password);
	}

	/**
	 * Init the time limit.
	 * 
	 * @param limit
	 *        The time limit.
	 */
	protected void initTimeLimit(Long limit)
	{
		this.timeLimit = limit;
	}

	/**
	 * Init the tries.
	 * 
	 * @param tries
	 *        The tries.
	 */
	protected void initTries(Integer tries)
	{
		this.tries = tries;
	}

	/**
	 * Init the user list.
	 * 
	 * @param ids
	 *        The user list.
	 */
	protected void initUsers(List<String> ids)
	{
		this.userIds.clear();
		if (ids != null)
		{
			this.userIds.addAll(ids);
		}
	}

	/**
	 * Set as a copy of another (deep copy).
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(AssessmentAccessImpl other)
	{
		this.acceptUntilDate = other.acceptUntilDate;
		this.changed = other.changed;
		this.dueDate = other.dueDate;
		this.id = other.id;
		this.openDate = other.openDate;
		this.overrideAcceptUntilDate = other.overrideAcceptUntilDate;
		this.overrideDueDate = other.overrideDueDate;
		this.overrideOpenDate = other.overrideOpenDate;
		this.overridePassword = other.overridePassword;
		this.overrideTimeLimit = other.overrideTimeLimit;
		this.overrideTries = other.overrideTries;
		this.password = null;
		if (other.password != null)
		{
			this.password = new AssessmentPasswordImpl(other.password, this.owner);
		}
		this.timeLimit = other.timeLimit;
		this.tries = other.tries;
		this.userIds = new ArrayList<String>(other.userIds);
	}
}
