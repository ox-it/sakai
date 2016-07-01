/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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

import org.etudes.ambrosia.api.Component;

/**
 * TypeSpecificQuestion defines the plug-in question handler for a question type.
 */
public interface TypeSpecificQuestion extends Cloneable
{
	/**
	 * Clone a copy
	 * 
	 * @param question
	 *        the Question that the clone is for.
	 * @return A copy.
	 */
	Object clone(Question question);

	/**
	 * Consolidate the question, such as after editing and before save.
	 * 
	 * @param destination
	 *        The destination from the post.
	 * @return null to stay at the current destination, or the destination to go to.
	 */
	String consolidate(String destination);

	/**
	 * Access the user interface component for authoring of this question type.
	 * 
	 * @return The user interface component for authoring of this question type.
	 */
	Component getAuthoringUi();

	/**
	 * Access the type specific question data as an array of strings that can be restored with setData()
	 * 
	 * @return The type specific question data.
	 */
	String[] getData();

	/**
	 * Access the user interface component for delivery of this question type.
	 * 
	 * @return The user interface component for delivery of this question type.
	 */
	Component getDeliveryUi();

	/**
	 * Access a description of the question.
	 * 
	 * @return A description of the question.
	 */
	String getDescription();

	/**
	 * Check if the question supports a correct answer or not. Survey questions, for example, do not support a correct answer.
	 * 
	 * @return TRUE if the question supports a correct answer, FALSE if not.
	 */
	Boolean getHasCorrect();

	/**
	 * Check if the question type supports points, or if it does not.
	 * 
	 * @return TRUE if the question type supports points, FALSE if it does not.
	 */
	Boolean getHasPoints();

	/**
	 * Get a message describing what is wrong with the question.
	 * 
	 * @return A localized message describing what is wrong with the question, or null if the question is valid.
	 */
	String getInvalidMessage();

	/**
	 * Check if the question type is always a survey question.
	 * 
	 * @return TRUE if the question type is always a survey question, FALSE if not.
	 */
	Boolean getIsSurvey();

	/**
	 * Check if this question definition is valid.
	 * 
	 * @return TRUE if this question definition is valid, FALSE if not.
	 */
	Boolean getIsValid();

	/**
	 * Access the plugin for this type.
	 * 
	 * @return The plugin for this type.
	 */
	QuestionPlugin getPlugin();

	/**
	 * Access the user interface component for review of this question type.
	 * 
	 * @return The user interface component for review of this question type.
	 */
	Component getReviewUi();

	/**
	 * Check if this type uses the generic question feedback.
	 * 
	 * @return TRUE to use the generic question feedback for this type, FALSE to not.
	 */
	Boolean getUseFeedback();

	/**
	 * Check if this type uses the generic question hints.
	 * 
	 * @return TRUE to use the generic question hints for this type, FALSE to not.
	 */
	Boolean getUseHints();

	/**
	 * Check if this type uses the generic question presentation.
	 * 
	 * @return TRUE to use the generic question presentation for this type, FALSE to not.
	 */
	Boolean getUseQuestionPresentation();

	/**
	 * Check if this type uses the generic question presentation attachments.
	 * 
	 * @return TRUE to use the generic question presentation attachments for this type, FALSE to not.
	 */
	Boolean getUseQuestionPresentationAttachments();

	/**
	 * Check if this type uses the generic question reason.
	 * 
	 * @return TRUE to use the generic question reason for this type, FALSE to not.
	 */
	Boolean getUseReason();

	/**
	 * Access the user interface component for answer view of this question type.
	 * 
	 * @return The user interface component for answer view of this question type.
	 */
	Component getViewAnswerUi();

	/**
	 * Access the user interface component for view-only delivery of this question type.
	 * 
	 * @return The user interface component for view-only delivery of this question type.
	 */
	Component getViewDeliveryUi();

	/**
	 * Access the user interface component for question view of this question type.
	 * 
	 * @return The user interface component for question view of this question type.
	 */
	Component getViewQuestionUi();

	/**
	 * Access the user interface component for statistics view of this question type.
	 * 
	 * @return The user interface component for statistics view of this question type.
	 */
	Component getViewStatsUi();

	/**
	 * Restore settings from this array of strings, created by getData()
	 * 
	 * @param data
	 *        The data to restore.
	 */
	void setData(String[] data);
}
