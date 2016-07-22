/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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
 * PoolDraw contain the details of a part's draw from a pool.
 */
public interface PoolDraw extends PartDetail
{
	/**
	 * Draw questions based on this random seed.
	 * 
	 * @param shuffler
	 *        The shuffler.
	 * @return A List of question ids drawn from the pool.
	 */
	List<String> drawQuestionIds(Shuffler shuffler);

	/**
	 * Access all questions.
	 * 
	 * @return A List of question ids from the pool.
	 */
	List<String> getAllQuestionIds();

	/**
	 * Access the original pool id.
	 * 
	 * @return The original pool id.
	 */
	String getOrigPoolId();

	/**
	 * Get the number of questions available in the pool.<br />
	 * The current pool size is reduced by the number of questions manually drawn from the pool in this assessment.
	 * 
	 * @return The number of questions available in the pool.
	 */
	Integer getPoolNumAvailableQuestions();

	/**
	 * Set the number of questions to draw from the pool.
	 * 
	 * @param numQuestions
	 *        The number of questions to draw from the pool.
	 */
	void setNumQuestions(Integer numQuestions);

	/**
	 * Set the pool to draw from.
	 * 
	 * @param pool
	 *        The pool to draw from.
	 */
	void setPool(Pool pool);
}
