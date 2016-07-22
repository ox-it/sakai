/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-api/api/src/java/org/etudes/mneme/api/Settings.java $
 * $Id: Settings.java 6402 2013-11-27 22:00:33Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2013 Etudes, Inc.
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

package org.etudes.mneme.api;


/**
 * Settings is a class used to capture the settings the instructor wants to change.
 */
public interface Settings
{
	
	/**
	 * @return The value of allOptionsSetting
	 */
	Boolean getAllOptionsSetting();

    /**
     * @return The value of anonGradingSetting
     */
    Boolean getAnonGradingSetting();

	/**
	 * @return The value of autoEmailSetting
	 */
	Boolean getAutoEmailSetting();
	
	/**
	 * @return The value of awardCertSetting
	 */
	Boolean getAwardCertSetting();

	/**
	 * @return The value of finalMessageSetting
	 */
	Boolean getFinalMessageSetting();

	/**
	 * @return The value of hintsSetting
	 */
	Boolean getHintsSetting();

	/**
	 * @return The value of honorPledgeSetting
	 */
	Boolean getHonorPledgeSetting();

	/**
	 * @return The value of modelAnswerSetting
	 */
	Boolean getModelAnswerSetting();

	/**
	 * @return The value of navlaySetting
	 */
	Boolean getNavlaySetting();

	/**
	 * @return The value of partNumberSetting
	 */
	Boolean getPartNumberSetting();

	/**
	 * @return The value of passwordSetting
	 */
	Boolean getPasswordSetting();

	/**
	 * @return The value of releaseSubSetting
	 */
	Boolean getReleaseSubSetting();

	/**
	 * @return The value of reviewOptionsSetting
	 */
	Boolean getReviewOptionsSetting();

	/**
	 * @return The value of sendGBSetting
	 */
	Boolean getSendGBSetting();

	/**
	 * @return The value of shuffleChoicesSetting
	 */
	Boolean getShuffleChoicesSetting();

	/**
	 * @return The value of timeLimitSetting
	 */
	Boolean getTimeLimitSetting();

	/**
	 * @return The value of triesSetting
	 */
	Boolean getTriesSetting();
	
	/**
	 * Set the value of all options choice
	 * 
	 * @param allOptionsSetting
	 */
	void setAllOptionsSetting(Boolean allOptionsSetting);
	
	/**
	 * Set the value of anonymous grading choice
	 * 
	 * @param anonGradingSetting
	 */
	void setAnonGradingSetting(Boolean anonGradingSetting);
	
	/**
	 * Set the value of anonymous email choice
	 * 
	 * @param autoEmailSetting
	 */
	void setAutoEmailSetting(Boolean autoEmailSetting);	
	
	/**
	 * Set the value of award certificate choice
	 * 
	 * @param awardCertSetting
	 */
	void setAwardCertSetting(Boolean awardCertSetting);
	
	/**
	 * Set the value of final message choice
	 * 
	 * @param finalMessageSetting
	 */
	void setFinalMessageSetting(Boolean finalMessageSetting);
	
	/**
	 * Set the value of hints choice
	 * 
	 * @param hintsSetting
	 */
	void setHintsSetting(Boolean hintsSetting);
	
	/**
	 * Set the value of honor pledge setting
	 * 
	 * @param honorPledgeSetting
	 */
	void setHonorPledgeSetting(Boolean honorPledgeSetting);
	
	/**
	 * Set the value of model answer choice
	 * 
	 * @param modelAnswerSetting
	 */
	void setModelAnswerSetting(Boolean modelAnswerSetting);
	
	/**
	 * Set the value of navigation choice and layout
	 * 
	 * @param navlaySetting
	 */
	void setNavlaySetting(Boolean navlaySetting);
	
	/**
	 * Set the value of part number choice
	 * 
	 * @param partNumberSetting
	 */
	void setPartNumberSetting(Boolean partNumberSetting);
	
	/**
	 * Set the value of password choice
	 * 
	 * @param passwordSetting
	 */
	void setPasswordSetting(Boolean passwordSetting);
	
	/**
	 * Set the value of release submissions choice
	 * 
	 * @param releaseSubSetting
	 */
	void setReleaseSubSetting(Boolean releaseSubSetting);
	
	/**
	 * Set the value of review options choice
	 * 
	 * @param reviewOptionsSetting
	 */
	void setReviewOptionsSetting(Boolean reviewOptionsSetting);
	
	/**
	 * Set the value of send to gradebook choice
	 * 
	 * @param sendGBSetting
	 */
	void setSendGBSetting(Boolean sendGBSetting);
	
	/**
	 * Set the value of shuffle choices setting choice
	 * 
	 * @param shuffleChoicesSetting
	 */
	void setShuffleChoicesSetting(Boolean shuffleChoicesSetting);
	
	/**
	 * Set the value of time limit choice setting
	 * 
	 * @param timeLimitSetting
	 */
	void setTimeLimitSetting(Boolean timeLimitSetting);

	/**
	 * Set the value of tries choice setting
	 * 
	 * @param triesSetting
	 */
	void setTriesSetting(Boolean triesSetting);
}
