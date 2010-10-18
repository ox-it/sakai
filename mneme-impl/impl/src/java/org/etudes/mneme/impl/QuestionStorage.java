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
import java.util.Map;
import java.util.Set;

import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.util.api.Translation;

/**
 * QuestionStorage defines the storage interface for Questions.
 */
public interface QuestionStorage
{
	/**
	 * Clear out all questions from the context.
	 * 
	 * @param context
	 *        The context.
	 */
	void clearContext(String context);

	/**
	 * Clear out any mint objects that are old enough to be considered abandoned.
	 * 
	 * @param stale
	 *        The time to compare to the create date; before this they are stale.
	 * @return A List of the question ids cleared.
	 */
	List<String> clearStaleMintQuestions(Date stale);

	/**
	 * Construct a new question object that is a copy of another.
	 * 
	 * @param question
	 *        The question to copy.
	 * @return A question object.
	 */
	QuestionImpl clone(QuestionImpl question);

	/**
	 * Create a new question that is a copy of each question in the pool.
	 * 
	 * @param userId
	 *        The user to own the questions.
	 * @param source
	 *        The pool of questions to copy.
	 * @param destination
	 *        the pool where the question will live.
	 * @param asHistory
	 *        If set, make the questions historical.
	 * @param oldToNew
	 *        A map, which, if present, will be filled in with the mapping of the source question id to the destination question id for each question copied.
	 * @param attachmentTranslations
	 *        A list of Translations for attachments and embedded media.
	 * @param merge
	 *        if true, if there is question already in the pool that matches one to be copied, don't copy it and create a new question.
	 * @param includeQuestions
	 *        if not null, only import the pool's question if its id is in the set.
	 * @return A List of the ids of the new questions created.
	 */
	List<String> copyPoolQuestions(String userId, Pool source, Pool destination, boolean asHistory, Map<String, String> oldToNew,
			List<Translation> attachmentTranslations, boolean merge, Set<String> includeQuestions);

	/**
	 * Count the questions in this context.
	 * 
	 * @param context
	 *        The context.
	 * @param questionType
	 *        The (optional) question type; if specified, only questions of this type are included.
	 * @param survey
	 *        if TRUE, include only survey questions, if FALSE, include only assessment questions, if NULL, include both.
	 * @param valid
	 *        if TRUE, include only valid questions, if FALSE, include only invalid questions, if NULL, include both.
	 * @return The questions in this pool with this criteria.
	 */
	Integer countContextQuestions(String context, String questionType, Boolean survey, Boolean valid);

	/**
	 * Count the questions in this pool - separate counts for survey and non-survey.
	 * 
	 * @param pool
	 *        The pool.
	 * @param questionType
	 *        The (optional) question type; if specified, only questions of this type are included.
	 * @param valid
	 *        if TRUE, include only valid questions, if FALSE, include only invalid questions, if NULL, include both.
	 * @return The questions in this pool with this criteria, separate assessment and survey counts.
	 */
	Pool.PoolCounts countPoolQuestions(Pool pool, String questionType, Boolean valid);

	/**
	 * Count the questions in the non-historical pools of this context - separate counts for survey and non-survey.
	 * 
	 * @param context
	 *        The context.
	 * @param valid
	 *        if TRUE, include only valid questions, if FALSE, include only invalid questions, if NULL, include both.
	 * @return A Map of the pool id -> count, separate assessment and survey counts.
	 */
	Map<String, Pool.PoolCounts> countPoolQuestions(String context, Boolean valid);

	/**
	 * Check if a question by this id exists.
	 * 
	 * @param id
	 *        The question id
	 * @return TRUE if the question with this id exists, FALSE if not.
	 */
	Boolean existsQuestion(String id);

	/**
	 * Find all the non-historical question ids.
	 * 
	 * @return a List of the non-historical question ids.
	 */
	List<String> findAllNonHistoricalIds();

	/**
	 * Find all the questions in the context, sorted and paged.
	 * 
	 * @param context
	 *        The context.
	 * @param sort
	 *        The sort criteria.
	 * @param questionType
	 *        The (optional) question type; if specified, only questions of this type are included.
	 * @param pageNum
	 *        The page number (1 based) to display, or null to disable paging and get them all.
	 * @param pageSize
	 *        The number of items for the requested page, or null if we are not paging.
	 * @param survey
	 *        if TRUE, include only survey questions, if FALSE, include only assessment questions, if NULL, include both.
	 * @param valid
	 *        if TRUE, include only valid questions, if FALSE, include only invalid questions, if NULL, include both.
	 * @return The list of questions.
	 */
	List<QuestionImpl> findContextQuestions(String context, QuestionService.FindQuestionsSort sort, String questionType, Integer pageNum,
			Integer pageSize, Boolean survey, Boolean valid);

	/**
	 * Find all the questions in the Pool, sorted and paged.
	 * 
	 * @param pool
	 *        The Pool.
	 * @param sort
	 *        The sort criteria.
	 * @param questionType
	 *        The (optional) question type; if specified, only questions of this type are included.
	 * @param pageNum
	 *        The page number (1 based) to display, or null to disable paging and get them all.
	 * @param pageSize
	 *        The number of items for the requested page, or null if we are not paging.
	 * @param survey
	 *        if TRUE, include only survey questions, if FALSE, include only assessment questions, if NULL, include both.
	 * @param valid
	 *        if TRUE, include only valid questions, if FALSE, include only invalid questions, if NULL, include both.
	 * @return The list of questions.
	 */
	List<QuestionImpl> findPoolQuestions(Pool pool, QuestionService.FindQuestionsSort sort, String questionType, Integer pageNum, Integer pageSize,
			Boolean survey, Boolean valid);

	/**
	 * Find all the questions in the pool
	 * 
	 * @param pool
	 *        The pool.
	 * @param survey
	 *        if TRUE, include only survey questions, if FALSE, include only assessment questions, if NULL, include both.
	 * @param valid
	 *        if TRUE, include only valid questions, if FALSE, include only invalid questions, if NULL, include both.
	 * @return The List of question ids that are in the pool.
	 */
	List<String> getPoolQuestions(Pool pool, Boolean Survey, Boolean valid);

	/**
	 * Access a question by id.
	 * 
	 * @param id
	 *        the question id.
	 * @return The question with this id, or null if not found.
	 */
	QuestionImpl getQuestion(String id);

	/**
	 * Initialize.
	 */
	void init();

	/**
	 * Move a question from one pool to another.
	 * 
	 * @param question
	 *        The question to move.
	 * @param pool
	 *        The pool to hold the question.
	 */
	void moveQuestion(Question question, Pool pool);

	/**
	 * Construct a new question object.
	 * 
	 * @return A question object.
	 */
	QuestionImpl newQuestion();

	/**
	 * Remove a question from storage.
	 * 
	 * @param question
	 *        The question to remove.
	 */
	void removeQuestion(QuestionImpl question);

	/**
	 * Save changes made to this question.
	 * 
	 * @param question
	 *        the question to save.
	 */
	void saveQuestion(QuestionImpl question);
}
