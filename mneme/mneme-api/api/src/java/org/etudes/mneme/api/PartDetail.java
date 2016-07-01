/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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

import java.util.Map;

/**
 * PartDetail models the PoolDraw or QuestionPick entries that specify the question makeup of a Part.
 */
public interface PartDetail
{
	/**
	 * Access a description string for the detail.
	 * 
	 * @return The detail description.
	 */
	String getDescription();

	/**
	 * @return the effective points value - if there is an override, use that, else use the total points computed from the question pool values.
	 */
	Float getEffectivePoints();

	/**
	 * Check if the detail supports points, or if it does not. Survey questions, or draws of Surveys questions, for example, do not support points.
	 * 
	 * @return TRUE if the detail supports points, FALSE if it does not.
	 */
	Boolean getHasPoints();

	/**
	 * Access the id of this assessment.
	 * 
	 * @return The assessment's id.
	 */
	String getId();

	/**
	 * Get a message describing what is wrong with the part.
	 * 
	 * @return A localized message describing what is wrong with the part, or null if the part is valid.
	 */
	String getInvalidMessage();

	/**
	 * @return TRUE for phantom details, FALSE for real ones.
	 */
	Boolean getIsPhantom();

	/**
	 * @return True if the detail is specific to a question, false if it is general to a set of questions (i.e. a random draw).
	 */
	Boolean getIsSpecific();

	/**
	 * @return True if the detail is valid, false if not.
	 */
	Boolean getIsValid();

	/**
	 * @return The total point value ignoring any override set.
	 */
	Float getNonOverridePoints();

	/**
	 * Get the number of questions this detail provides.
	 * 
	 * @return The number of questions this detail adds to the part.
	 */
	Integer getNumQuestions();

	/**
	 * Access the ordering information within the part.
	 * 
	 * @return The ordering information within the part.
	 */
	Ordering<PartDetail> getOrdering();

	/**
	 * @return The Part in which this detail resides.
	 */
	Part getPart();

	/**
	 * @return The points set for the entire part, or null if not set.
	 */
	Float getPoints();

	/**
	 * Access the pool associated with this detail (the question's pool or the pool to draw from).
	 * 
	 * @return The pool.
	 */
	Pool getPool();

	/**
	 * Access the pool id associated with this detail (the question's pool or the pool to draw from).
	 * 
	 * @return The pool id.
	 */
	String getPoolId();

	/**
	 * @return The point value for a single question in the part, or null if not set.
	 */
	Float getQuestionPoints();

	/**
	 * Compute the total points for the part detail.
	 * 
	 * @return The total points for the part detail, or 0 if there are no points.
	 */
	Float getTotalPoints();

	/**
	 * Access the detail's type; either a question type if this is a specific question, or "draw" for random draw.
	 * 
	 * @return The detail type.
	 */
	String getType();

	/**
	 * Restore the detail's pool and or question id to the original, translated by the maps if present.
	 * 
	 * @param poolIdMap
	 *        A map of old-to-new pool ids.
	 * @param questionIdMap
	 *        A map of old-to-new question ids.
	 * @return true if successful, false if not.
	 */
	boolean restoreToOriginal(Map<String, String> poolIdMap, Map<String, String> questionIdMap);

	/**
	 * Set the points. If it matches the non-override value, clear the override, else set this as the override value.
	 * 
	 * @param points
	 *        The point value for all the questions in the part.
	 */
	void setEffectivePoints(Float points);

	/**
	 * Set the point value override for the questions in this part; a value for all the questions.
	 * 
	 * @param points
	 *        The point value for all the questions in the part, or null to clear the setting.
	 */
	void setPoints(Float points);
}
