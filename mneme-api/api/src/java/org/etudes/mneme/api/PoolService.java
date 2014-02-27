/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/tags/2.1.27/mn
eme-api/api/src/java/org/etudes/mneme/api/PoolService.java $
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
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
 * PoolService provides services around question pool management for Mneme.
 */
public interface PoolService extends PoolGetService
{
	/**
	 * Sort options for findPools()
	 */
	enum FindPoolsSort
	{
		points_a, points_d, title_a, title_d, created_a, created_d
	}

	/**
	 * Check if the current user is allowed to manage pools in this context.
	 * 
	 * @param context
	 *        The context.
	 * @return TRUE if the user is allowed, FALSE if not.
	 */
	Boolean allowManagePools(String context);

	/**
	 * Clear out any mint objects that are old enough to be considered abandoned.
	 */
	void clearStaleMintPools();

	/**
	 * Copy an existing pool creating a new pool in the context.
	 * 
	 * @param context
	 *        The context.
	 * @param pool
	 *        The pool to copy.
	 * @return The new pool as a copy of the old pool, complete with questions.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to create a new pool.
	 */
	Pool copyPool(String context, Pool pool) throws AssessmentPermissionException;

	/**
	 * Check if this pool exists.
	 * 
	 * @param poolId
	 *        The pool ID.
	 * @return TRUE if the pool exists, FALSE if not.
	 */
	Boolean existsPool(String poolId);

	/**
	 * Locate a list of pools in this context with this criteria (excluding mints and historical).
	 * 
	 * @param context
	 *        The context.
	 * @param sort
	 *        The sort criteria.
	 * @param search
	 *        The search criteria.
	 * @return a list of pools that meet the criteria.
	 */
	List<Pool> findPools(String context, FindPoolsSort sort, String search);

	/**
	 * Get all the pools available to the context (including historical).
	 * 
	 * @param context
	 *        The context.
	 * @return The pools available to the context.
	 */
	List<Pool> getAllPools(String context);

	/**
	 * Get all the pools available to the context (excluding mints and historical).
	 * 
	 * @param context
	 *        The context.
	 * @return The pools available to the context.
	 */
	List<Pool> getPools(String context);

	/**
	 * Create a new pool.
	 * 
	 * @param context
	 *        The context.
	 * @return The new pool.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to create a new pool.
	 */
	Pool newPool(String context) throws AssessmentPermissionException;

	/**
	 * Remove this pool.
	 * 
	 * @param pool
	 *        The pool to remove.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to manage this pool.
	 */
	void removePool(Pool pool) throws AssessmentPermissionException;

	/**
	 * Save changes made to this pool.
	 * 
	 * @param pool
	 *        The pool to save.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to edit this pool.
	 */
	void savePool(Pool pool) throws AssessmentPermissionException;
}
