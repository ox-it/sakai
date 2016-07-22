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
 * PoolStorageMysql implements PoolStorage for MySQL.
 */
public abstract class PoolStorageMysql extends PoolStorageSql implements PoolStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(PoolStorageMysql.class);

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
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_POOL (");
		sql.append(" CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, DIFFICULTY, HISTORICAL,");
		sql.append(" MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POINTS, TITLE )");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[11];
		fields[0] = pool.getContext();
		fields[1] = pool.getCreatedBy().getDate().getTime();
		fields[2] = pool.getCreatedBy().getUserId();
		fields[3] = pool.getDescription();
		fields[4] = pool.getDifficulty().toString();
		fields[5] = pool.getIsHistorical() ? "1" : "0";
		fields[6] = pool.getMint() ? "1" : "0";
		fields[7] = pool.getModifiedBy().getDate().getTime();
		fields[8] = pool.getModifiedBy().getUserId();
		fields[9] = pool.getPointsEdit();
		fields[10] = pool.getTitle();

		Long id = this.sqlService.dbInsert(null, sql.toString(), fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("insertPoolTx: dbInsert failed");
		}

		// set the pool's id
		pool.initId(id.toString());
	}
}
