/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/SettingsImpl.java $
 * $Id: SettingsImpl.java 6429 2013-12-02 19:41:13Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c)  2013 Etudes, Inc.
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

import org.etudes.mneme.api.Settings;

/**
 * SettingsImpl implements Settings
 */
public class SettingsImpl implements Settings
{
	/** Our logger. */

	protected Boolean allOptionsSetting = Boolean.FALSE;
	
	protected Boolean anonGradingSetting = Boolean.FALSE;
	
	protected Boolean autoEmailSetting = Boolean.FALSE;
	
	protected Boolean awardCertSetting = Boolean.FALSE;

	protected Boolean finalMessageSetting = Boolean.FALSE;

	protected Boolean hintsSetting = Boolean.FALSE;

	protected Boolean honorPledgeSetting = Boolean.FALSE;
	
	protected Boolean modelAnswerSetting = Boolean.FALSE;

	protected Boolean navlaySetting = Boolean.FALSE;

	protected Boolean partNumberSetting = Boolean.FALSE;

	protected Boolean passwordSetting = Boolean.FALSE;

	protected Boolean releaseSubSetting = Boolean.FALSE;

	protected Boolean reviewOptionsSetting = Boolean.FALSE;

	protected Boolean sendGBSetting = Boolean.FALSE;

	protected Boolean shuffleChoicesSetting = Boolean.FALSE;

	protected Boolean timeLimitSetting = Boolean.FALSE;

	protected Boolean triesSetting = Boolean.FALSE;

	/**
	 * {@inheritDoc}
	 */
	public Boolean getAllOptionsSetting()
	{
		return allOptionsSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getAnonGradingSetting()
	{
		return anonGradingSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getAutoEmailSetting()
	{
		return autoEmailSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getAwardCertSetting()
	{
		return awardCertSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getFinalMessageSetting()
	{
		return finalMessageSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHintsSetting()
	{
		return hintsSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHonorPledgeSetting()
	{
		return honorPledgeSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getModelAnswerSetting()
	{
		return modelAnswerSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getNavlaySetting()
	{
		return navlaySetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPartNumberSetting()
	{
		return partNumberSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPasswordSetting()
	{
		return passwordSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getReleaseSubSetting()
	{
		return releaseSubSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getReviewOptionsSetting()
	{
		return reviewOptionsSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getSendGBSetting()
	{
		return sendGBSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getShuffleChoicesSetting()
	{
		return shuffleChoicesSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getTimeLimitSetting()
	{
		return timeLimitSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getTriesSetting()
	{
		return this.triesSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAllOptionsSetting(Boolean allOptionsSetting)
	{
		this.allOptionsSetting = allOptionsSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnonGradingSetting(Boolean anonGradingSetting)
	{
		this.anonGradingSetting = anonGradingSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setAutoEmailSetting(Boolean autoEmailSetting)
	{
		this.autoEmailSetting = autoEmailSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setAwardCertSetting(Boolean awardCertSetting)
	{
		this.awardCertSetting = awardCertSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setFinalMessageSetting(Boolean finalMessageSetting)
	{
		this.finalMessageSetting = finalMessageSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setHintsSetting(Boolean hintsSetting)
	{
		this.hintsSetting = hintsSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setHonorPledgeSetting(Boolean honorPledgeSetting)
	{
		this.honorPledgeSetting = honorPledgeSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setModelAnswerSetting(Boolean modelAnswerSetting)
	{
		this.modelAnswerSetting = modelAnswerSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setNavlaySetting(Boolean navlaySetting)
	{
		this.navlaySetting = navlaySetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setPartNumberSetting(Boolean partNumberSetting)
	{
		this.partNumberSetting = partNumberSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setPasswordSetting(Boolean passwordSetting)
	{
		this.passwordSetting = passwordSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setReleaseSubSetting(Boolean releaseSubSetting)
	{
		this.releaseSubSetting = releaseSubSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setReviewOptionsSetting(Boolean reviewOptionsSetting)
	{
		this.reviewOptionsSetting = reviewOptionsSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSendGBSetting(Boolean sendGBSetting)
	{
		this.sendGBSetting = sendGBSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setShuffleChoicesSetting(Boolean shuffleChoicesSetting)
	{
		this.shuffleChoicesSetting = shuffleChoicesSetting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setTimeLimitSetting(Boolean timeLimitSetting)
	{
		this.timeLimitSetting = timeLimitSetting;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTriesSetting(Boolean setting)
	{
		this.triesSetting = setting;
	}
	
}
