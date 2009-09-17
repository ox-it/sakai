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

package org.etudes.mneme.impl;

import java.util.Date;
import java.util.List;

import org.etudes.mneme.api.PoolService;

/**
 * PoolStorage defines the storage interface for Pools.
 */
public interface PoolStorage
{
	/**
	 * Clear out all pools from the context.
	 * 
	 * @param context
	 *        The context.
	 */
	void clearContext(String context);

	/**
	 * Construct a new pool object as a copy of another.
	 * 
	 * @param pool
	 *        The pool to copy.
	 * @return A pool object.
	 */
	PoolImpl clone(PoolImpl pool);

	/**
	 * Check if a pool by this id exists.
	 * 
	 * @param poolId
	 *        The pool id
	 * @return TRUE if the pool with this id exists, FALSE if not.
	 */
	Boolean existsPool(String poolId);

	/**
	 * Find all the pools in this context that meet the criteria (excluding mints and historicals).
	 * 
	 * @param context
	 *        The context.
	 * @param sort
	 *        The sort criteria.
	 * @return The list of pools that meet the criteria.
	 */
	List<PoolImpl> findPools(String context, PoolService.FindPoolsSort sort);

	/**
	 * Access a pool by id.
	 * 
	 * @param poolId
	 *        the pool id.
	 * @return The pool with this id, or null if not found.
	 */
	PoolImpl getPool(String poolId);

	/**
	 * Access all pools in the context (excluding mints).
	 * 
	 * @param context
	 *        The context.
	 * @param includeHistorical
	 *        if true, include historical pools, else exclude them.
	 * @return The List of Pools in the context.
	 */
	List<PoolImpl> getPools(String context, boolean includeHistorical);

	/**
	 * Get any pools that are mint and old enough to be considered abandoned.
	 * 
	 * @param stale
	 *        The time to compare to the create date; before this they are stale.
	 * @return The list of stale mint pools.
	 */
	List<PoolImpl> getStaleMintPools(Date stale);

	/**
	 * Initialize.
	 */
	void init();

	/**
	 * Construct a new pool object.
	 * 
	 * @return A pool object.
	 */
	PoolImpl newPool();

	/**
	 * Remove a pool from storage.
	 * 
	 * @param pool
	 *        The pool to remove.
	 */
	void removePool(PoolImpl pool);

	/**
	 * Save changes made to this pool.
	 * 
	 * @param pool
	 *        the pool to save.
	 */
	void savePool(PoolImpl pool);
}
