/**********************************************************************************
 * $URL$
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

package org.etudes.mneme.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.PoolService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * PoolStorageMysql implements PoolStorage in SQL databases
 */
public abstract class PoolStorageSql implements PoolStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(PoolStorageSql.class);

	/** Configuration: to run the ddl on init or not. */
	protected boolean autoDdl = false;

	/** Dependency: SqlService. */
	protected SqlService sqlService = null;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * {@inheritDoc}
	 */
	public void clearContext(final String context)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				clearContextTx(context);
			}
		}, "clearContext: " + context.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolImpl clone(PoolImpl pool)
	{
		return new PoolImpl(pool);
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean existsPool(String poolId)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(1) FROM MNEME_POOL P");
		sql.append(" WHERE P.ID=?");
		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(poolId);
		List results = this.sqlService.dbRead(sql.toString(), fields, null);
		if (results.size() > 0)
		{
			int size = Integer.parseInt((String) results.get(0));
			return Boolean.valueOf(size == 1);
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PoolImpl> findPools(String context, PoolService.FindPoolsSort sort)
	{
		// if ((!pool.historical) && (!pool.getMint()) && pool.getContext().equals(context))

		// the where and order by
		StringBuilder whereOrder = new StringBuilder();
		whereOrder.append("WHERE P.CONTEXT=? AND P.MINT='0' AND P.HISTORICAL='0' ORDER BY ");
		switch (sort)
		{
			case title_a:
			{
				whereOrder.append("P.TITLE ASC, P.CREATED_BY_DATE ASC");
				break;
			}
			case title_d:
			{
				whereOrder.append("P.TITLE DESC, P.CREATED_BY_DATE DESC");
				break;
			}
			case points_a:
			{
				whereOrder.append("P.POINTS ASC, P.TITLE ASC, P.CREATED_BY_DATE ASC");
				break;
			}
			case points_d:
			{
				whereOrder.append("P.POINTS DESC, P.TITLE DESC, P.CREATED_BY_DATE DESC");
				break;
			}
			case created_a:
			{
				whereOrder.append("P.CREATED_BY_DATE ASC");
				break;
			}
			case created_d:
			{
				whereOrder.append("P.CREATED_BY_DATE DESC");
				break;
			}
		}

		Object[] fields = new Object[1];
		fields[0] = context;

		List<PoolImpl> rv = readPools(whereOrder.toString(), fields);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolImpl getPool(String poolId)
	{
		return readPool(poolId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PoolImpl> getPools(String context, boolean includeHistorical)
	{
		// if (((!pool.historical) || includeHistorical) && (!pool.getMint()) && pool.getContext().equals(context))

		StringBuilder whereOrder = new StringBuilder();
		whereOrder.append("WHERE P.CONTEXT=? AND P.MINT='0'");
		if (!includeHistorical)
		{
			whereOrder.append(" AND P.HISTORICAL='0'");
		}
		whereOrder.append(" ORDER BY CREATED_BY_DATE ASC");

		Object[] fields = new Object[1];
		fields[0] = context;

		return readPools(whereOrder.toString(), fields);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PoolImpl> getStaleMintPools(final Date stale)
	{
		StringBuilder whereOrder = new StringBuilder();
		whereOrder.append("WHERE MINT='1' AND CREATED_BY_DATE < ?");

		Object[] fields = new Object[1];
		fields[0] = stale.getTime();

		return readPools(whereOrder.toString(), fields);
	}

	/**
	 * {@inheritDoc}
	 */
	public abstract PoolImpl newPool();

	/**
	 * {@inheritDoc}
	 */
	public void removePool(PoolImpl pool)
	{
		deletePool(pool);
	}

	/**
	 * {@inheritDoc}
	 */
	public void savePool(PoolImpl pool)
	{
		// for new pools
		if (pool.getId() == null)
		{
			insertPool(pool);
		}

		// for existing pools
		else
		{
			updatePool(pool);
		}
	}

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		autoDdl = new Boolean(value).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSqlService(SqlService service)
	{
		this.sqlService = service;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		threadLocalManager = service;
	}

	/**
	 * Transaction code for clearContext()
	 */
	protected void clearContextTx(String context)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_POOL");
		sql.append(" WHERE CONTEXT=?");

		Object[] fields = new Object[1];
		fields[0] = context;

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("clearContextTx: dbWrite failed");
		}
	}

	/**
	 * Delete a pool.
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void deletePool(final PoolImpl pool)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				deletePoolTx(pool);
			}
		}, "deletePool: " + pool.getId());
	}

	/**
	 * Delete a pool (transaction code).
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void deletePoolTx(PoolImpl pool)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_POOL");
		sql.append(" WHERE ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(pool.getId());

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("deletePoolTx: db write failed");
		}
	}

	/**
	 * Insert a new pool.
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void insertPool(final PoolImpl pool)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				insertPoolTx(pool);
			}
		}, "insertPool: " + pool.getId());
	}

	/**
	 * Insert a new pool (transaction code).
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected abstract void insertPoolTx(PoolImpl pool);

	/**
	 * Read a pool
	 * 
	 * @param id
	 *        The pool id.
	 * @return The pool.
	 */
	protected PoolImpl readPool(String id)
	{
		String whereOrder = "WHERE P.ID = ?";
		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(id);
		List<PoolImpl> rv = readPools(whereOrder, fields);
		if (rv.size() > 0)
		{
			return rv.get(0);
		}

		return null;
	}

	/**
	 * Read a selection of pools
	 * 
	 * @param whereOrder
	 *        The WHERE and ORDER BY sql clauses
	 * @param fields
	 *        The bind variables.
	 * @return The pools.
	 */
	protected List<PoolImpl> readPools(String whereOrder, Object[] fields)
	{
		final List<PoolImpl> rv = new ArrayList<PoolImpl>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.CONTEXT, P.CREATED_BY_DATE, P.CREATED_BY_USER, P.DESCRIPTION, P.DIFFICULTY,");
		sql.append(" P.HISTORICAL, P.ID, P.MINT, P.MODIFIED_BY_DATE, P.MODIFIED_BY_USER, P.POINTS, P.TITLE");
		sql.append(" FROM MNEME_POOL P ");
		sql.append(whereOrder);

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					PoolImpl pool = newPool();
					pool.setContext(SqlHelper.readString(result, 1));
					pool.getCreatedBy().setDate(SqlHelper.readDate(result, 2));
					pool.getCreatedBy().setUserId(SqlHelper.readString(result, 3));
					pool.setDescription(SqlHelper.readString(result, 4));
					pool.setDifficulty(SqlHelper.readInteger(result, 5));
					pool.initHistorical(SqlHelper.readBoolean(result, 6));
					pool.initId(SqlHelper.readId(result, 7));
					pool.initMint(SqlHelper.readBoolean(result, 8));
					pool.getModifiedBy().setDate(SqlHelper.readDate(result, 9));
					pool.getModifiedBy().setUserId(SqlHelper.readString(result, 10));
					pool.setPointsEdit(SqlHelper.readFloat(result, 11));
					pool.setTitle(SqlHelper.readString(result, 12));

					pool.changed.clearChanged();
					rv.add(pool);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("readPools: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * Update an existing pool.
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void updatePool(final PoolImpl pool)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				updatePoolTx(pool);
			}
		}, "updatePool: " + pool.getId());
	}

	/**
	 * Update an existing pool (transaction code).
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void updatePoolTx(PoolImpl pool)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE MNEME_POOL SET");
		sql.append(" CONTEXT=?, DESCRIPTION=?, DIFFICULTY=?, HISTORICAL=?,");
		sql.append(" MINT=?, MODIFIED_BY_DATE=?, MODIFIED_BY_USER=?, POINTS=?, TITLE=?");
		sql.append(" WHERE ID=?");

		Object[] fields = new Object[10];
		fields[0] = pool.getContext();
		fields[1] = pool.getDescription();
		fields[2] = pool.getDifficulty().toString();
		fields[3] = pool.getIsHistorical() ? "1" : "0";
		fields[4] = pool.getMint() ? "1" : "0";
		fields[5] = pool.getModifiedBy().getDate().getTime();
		fields[6] = pool.getModifiedBy().getUserId();
		fields[7] = pool.getPointsEdit();
		fields[8] = pool.getTitle();
		fields[9] = Long.valueOf(pool.getId());

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("updatePoolTx: db write failed");
		}
	}
}
