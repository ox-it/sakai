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

import java.util.List;

/**
 * DrawPart holds a set of manually selected questions.
 */
public interface DrawPart extends Part
{
	/**
	 * Add a pool and count to draw from.
	 * 
	 * @param pool
	 *        The pool to draw from.
	 * @param numQuestions
	 *        The number of questions to draw.
	 */
	PoolDraw addPool(Pool pool, Integer numQuestions);

	/**
	 * Get all the pools and their counts.
	 * 
	 * @return The List of draws.
	 */
	List<PoolDraw> getDraws();

	/**
	 * Get all the pools and their counts, sorted.
	 * 
	 * @return The List of draws.
	 */
	List<PoolDraw> getDraws(PoolService.FindPoolsSort sort);

	/**
	 * Get a list of draws for each pool specified - for those that are already in this part, set the non-null numQuestions.<br />
	 * The draws are virtual, not part of the DrawPart.
	 * 
	 * @param context
	 *        The context.
	 * @param sort
	 *        The sort criteria (from the PoolService).
	 * @param search
	 *        The search criteria.
	 * @return A list of draws for each pool.
	 */
	List<PoolDraw> getDrawsForPools(String context, PoolService.FindPoolsSort sort, String search);

	/**
	 * Get a virtal draw for this pool, set to the same count as one of our draws if we have one, else set to 0.<br />
	 * The draw is virtual, not part of this DrawPart.
	 * 
	 * @return The virtual PoolDraw for this pool.
	 */
	PoolDraw getVirtualDraw(Pool pool);

	/**
	 * Remove a pool's draw from the part.
	 * 
	 * @param pool
	 *        The pool to remove.
	 */
	void removePool(Pool pool);

	/**
	 * Apply these draws to the draws of the pool, adding, removing and changing counts as needed.
	 * 
	 * @param draws
	 *        The virtual (unconnected to this part) draws to apply.
	 */
	void updateDraws(List<PoolDraw> draws);
}
