/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012 Etudes, Inc.
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

import java.util.List;

/**
 * Pool defines the question pools.
 */
public interface Pool
{
	/** for getNumQuestionsSurvey. */
	public class PoolCounts
	{
		public Integer invalidAssessment;

		public Integer invalidSurvey;

		public Integer validAssessment;

		public Integer validSurvey;
	};

	/**
	 * Draw valid questions based on this random seed.
	 * 
	 * @param shuffler
	 *        The shuffler.
	 * @param numQuestions
	 *        The number of questions to draw.
	 * @param survey
	 *        if TRUE, include only survey questions, if FALSE, include only assessment questions, if NULL, include both.
	 * @return A List of question ids drawn from the pool.
	 */
	List<String> drawQuestionIds(Shuffler shuffler, Integer numQuestions, Boolean survey);

	/**
	 * Locate a list of questions in this pool with this criteria.
	 * 
	 * @param sort
	 *        The sort criteria.
	 * @param search
	 *        The search criteria.
	 * @param pageNum
	 *        The page number (1 based) to display, or null to disable paging and get them all.
	 * @param pageSize
	 *        The number of items for the requested page, or null if we are not paging.
	 * @return a list of questions that meet the criteria.
	 */
	List<Question> findQuestions(QuestionService.FindQuestionsSort sort, String search, Integer pageNum, Integer pageSize);

	/**
	 * Access all questions.
	 * 
	 * @param survey
	 *        if TRUE, include only survey questions, if FALSE, include only assessment questions, if NULL, include both.
	 * @param valid
	 *        if TRUE, include only valid questions, if FALSE, include only invalid questions, if NULL, include both.
	 * @return A List of question ids from the pool.
	 */
	List<String> getAllQuestionIds(Boolean survey, Boolean valid);

	/**
	 * Access the context of this pool.
	 * 
	 * @return The pool's context string. Will not be null.
	 */
	String getContext();

	/**
	 * Access the created-by (owner / date)
	 * 
	 * @return The pool's created-by.
	 */
	Attribution getCreatedBy();

	/**
	 * Access the description of the pool.
	 * 
	 * @return The description of the pool (rich text / html).
	 */
	String getDescription();

	/**
	 * Access the number of valid questions currently defined in the pool, counting survey and non-survey, valid and invalid separately.
	 * 
	 * @return the number of assessment questions and the number of survey questions.
	 */
	PoolCounts getDetailedNumQuestions();

	/**
	 * Access the difficulty value for the questions in this pool.<br />
	 * Values range from 1 (hardest) to 5 (easiest).
	 * 
	 * @return The difficulty value for the questions in this pool.
	 */
	Integer getDifficulty();

	/**
	 * Access the id of this pool.
	 * 
	 * @return The pool's id.
	 */
	String getId();

	/**
	 * Check if the pool is historical.
	 * 
	 * @return TRUE if the pool is historical, FALSE if not.
	 */
	Boolean getIsHistorical();

	/**
	 * Check if the end user has never made initial settings.
	 * 
	 * @return TRUE if this has not been modified since creation, FALSE if it has.
	 */
	Boolean getMint();

	/**
	 * Access the modified-by (owner / date)
	 * 
	 * @return The pool's modified-by.
	 */
	Attribution getModifiedBy();

	/**
	 * Access the number of questions currently defined in the pool.
	 * 
	 * @return The number of questions currently defined in the pool.
	 */
	Integer getNumQuestions();

	/**
	 * Access the number of points for each question in this pool.
	 * 
	 * @return The number of points for each question in this pool, or 0 if no points are set.
	 */
	Float getPoints();

	/**
	 * Access the number of points for each question in this pool.
	 * 
	 * @return The number of points for each question in this pool, or null if no points are set.
	 */
	Float getPointsEdit();

	/**
	 * Access the title of the pool.
	 * 
	 * @return The title of the pool.
	 */
	String getTitle();

	/**
	 * Set the context of this pool.
	 * 
	 * @param context
	 *        The pool's context string. Must be <=99 characters in length.
	 */
	void setContext(String context);

	/**
	 * Set the description of the pool.
	 * 
	 * @param description
	 *        html text. Must be well formed HTML or plain text.
	 */
	void setDescription(String description);

	/**
	 * Set the difficulty value for the questions in this pool.
	 * 
	 * @param difficulty
	 *        The difficulty value for the questions in this pool.<br />
	 *        Must be between 1 (lowest) and 5 (highest) - other values will be adjusted to a near end.<br />
	 *        Defaults to 3.<br />
	 *        May not be null.
	 */
	void setDifficulty(Integer difficulty);

	/**
	 * Set the number of points for each question in this pool.
	 * 
	 * @param points
	 *        The number of points for each question in this pool.<br />
	 *        Must be >= 0, <= 10000, otherwise set to nearest valid value. May not be null.
	 */
	void setPoints(Float points);

	/**
	 * Set the number of points for each question in this pool.
	 * 
	 * @param points
	 *        The number of points for each question in this pool, or null to have none set.<br />
	 *        Must be >= 0, <= 10000, otherwise set to nearest valid value. May be null.
	 */
	void setPointsEdit(Float points);

	/**
	 * Set the title of the pool.
	 * 
	 * @param title
	 *        The title of the pool.<br />
	 *        Truncated to 255 characters, trimmed, if all blank, set to null.
	 */
	void setTitle(String title);
}
