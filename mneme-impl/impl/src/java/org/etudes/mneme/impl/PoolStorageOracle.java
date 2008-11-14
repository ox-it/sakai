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

package org.etudes.mneme.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PoolStorageMysql implements PoolStorage for Oracle.
 */
public abstract class PoolStorageOracle extends PoolStorageSql implements PoolStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(PoolStorageOracle.class);

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// if we are auto-creating our schema, check and create
		if (autoDdl)
		{
			this.sqlService.ddl(this.getClass().getClassLoader(), "mneme_pool");
		}

		M_log.info("init()");
	}

	/**
	 * Insert a new pool (transaction code).
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void insertPoolTx(PoolImpl pool)
	{
		// get the next id
		Long id = this.sqlService.getNextSequence("MNEME_POOL_SEQ", null);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_POOL (ID,");
		sql.append(" CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, DIFFICULTY, HISTORICAL,");
		sql.append(" MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POINTS, TITLE )");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[12];
		fields[0] = id;
		fields[1] = pool.getContext();
		fields[2] = pool.getCreatedBy().getDate().getTime();
		fields[3] = pool.getCreatedBy().getUserId();
		fields[4] = pool.getDescription();
		fields[5] = pool.getDifficulty().toString();
		fields[6] = pool.getIsHistorical() ? "1" : "0";
		fields[7] = pool.getMint() ? "1" : "0";
		fields[8] = pool.getModifiedBy().getDate().getTime();
		fields[9] = pool.getModifiedBy().getUserId();
		fields[10] = pool.getPointsEdit();
		fields[11] = pool.getTitle();

		if (!this.sqlService.dbWrite(null, sql.toString(), fields))
		{
			throw new RuntimeException("insertPoolTx: dbWrite failed");
		}

		// set the pool's id
		pool.initId(id.toString());
	}
}
