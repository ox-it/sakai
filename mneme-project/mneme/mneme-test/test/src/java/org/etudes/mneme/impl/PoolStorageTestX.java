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

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.dbcp.SakaiBasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.impl.PoolImpl;
import org.etudes.mneme.impl.PoolStorage;
import org.etudes.mneme.impl.PoolStorageSql;
import org.sakaiproject.db.impl.BasicSqlService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.thread_local.impl.ThreadLocalComponent;

/**
 * Test Pool
 */
public abstract class PoolStorageTestX extends TestCase
{
	public class SqlServiceTest extends BasicSqlService
	{
		ThreadLocalManager tlm = null;

		protected void setThreadLocalManager(ThreadLocalManager tlm)
		{
			this.tlm = tlm;
		}

		protected ThreadLocalManager threadLocalManager()
		{
			return tlm;
		}

		protected UsageSessionService usageSessionService()
		{
			// TODO: might have to mock
			return null;
		}
	}

	/** Logger. */
	private static final Log log = LogFactory.getLog(PoolStorageTestX.class);

	protected final static String CONTEXT = "JUNIT_TEST_JUNIT";

	protected SqlServiceTest sqlService = null;

	protected PoolStorage storage = null;

	protected ThreadLocalComponent thread_localManager = null;

	/**
	 * @param arg0
	 */
	public PoolStorageTestX(String arg0)
	{
		super(arg0);
	}

//	/**
//	 * Test clearStaleMintPools()
//	 * 
//	 * @throws Exception
//	 */
//	public void test001clearStaleMintPools() throws Exception
//	{
//		// create a pool - leave it mint - make it old
//		Date old = new Date(new Date().getTime() - (2 * 24 * 60 * 60 * 1000));
//		PoolImpl pool = this.storage.newPool();
//		pool.setContext(CONTEXT);
//		pool.setTitle(CONTEXT);
//		pool.getCreatedBy().setDate(old);
//		pool.getCreatedBy().setUserId("admin");
//		pool.getModifiedBy().setDate(old);
//		pool.getModifiedBy().setUserId("admin");
//		this.storage.savePool(pool);
//
//		// it should now exist
//		Boolean exists = this.storage.existsPool(pool.getId());
//		assertTrue(exists == Boolean.TRUE);
//
//		// this should leave the pool in place
//		this.storage.clearStaleMintPools(old);
//
//		// it should now exist
//		exists = this.storage.existsPool(pool.getId());
//		assertTrue(exists == Boolean.TRUE);
//
//		// this should remove it
//		this.storage.clearStaleMintPools(new Date());
//
//		// it should not exist
//		exists = this.storage.existsPool(pool.getId());
//		assertTrue(exists == Boolean.FALSE);
//	}

	/**
	 * Test existsPool() removePool() savePool()
	 * 
	 * @throws Exception
	 */
	public void test002existsPool_removePool_savePool() throws Exception
	{
		// create a pool
		PoolImpl pool = this.storage.newPool();
		pool.setContext(CONTEXT);
		pool.setTitle(CONTEXT);
		pool.getCreatedBy().setDate(new Date());
		pool.getCreatedBy().setUserId("admin");
		pool.getModifiedBy().setDate(new Date());
		pool.getModifiedBy().setUserId("admin");
		this.storage.savePool(pool);

		// it should now exist
		Boolean exists = this.storage.existsPool(pool.getId());
		assertTrue(exists == Boolean.TRUE);

		// remove it
		this.storage.removePool(pool);

		// it should not exist
		exists = this.storage.existsPool(pool.getId());
		assertTrue(exists == Boolean.FALSE);
	}

	/**
	 * Test findPools()
	 * 
	 * @throws Exception
	 */
	public void test003findPools() throws Exception
	{
		// create some pools
		PoolImpl pool1 = this.storage.newPool();
		pool1.setContext(CONTEXT);
		pool1.setTitle("a");
		pool1.setPoints(Float.valueOf(5));
		pool1.getCreatedBy().setDate(new Date());
		pool1.getCreatedBy().setUserId("admin");
		pool1.getModifiedBy().setDate(new Date());
		pool1.getModifiedBy().setUserId("admin");
		pool1.clearMint();
		this.storage.savePool(pool1);

		PoolImpl pool2 = this.storage.newPool();
		pool2.setContext(CONTEXT);
		pool2.setTitle("b");
		pool2.setPoints(Float.valueOf(10));
		pool2.getCreatedBy().setDate(new Date());
		pool2.getCreatedBy().setUserId("admin");
		pool2.getModifiedBy().setDate(new Date());
		pool2.getModifiedBy().setUserId("admin");
		pool2.clearMint();
		this.storage.savePool(pool2);

		PoolImpl pool3 = this.storage.newPool();
		pool3.setContext(CONTEXT);
		pool3.setTitle("c");
		pool3.setPoints(Float.valueOf(1));
		pool3.getCreatedBy().setDate(new Date());
		pool3.getCreatedBy().setUserId("admin");
		pool3.getModifiedBy().setDate(new Date());
		pool3.getModifiedBy().setUserId("admin");
		pool3.clearMint();
		this.storage.savePool(pool3);

		PoolImpl pool4 = this.storage.newPool();
		pool4.setContext(CONTEXT);
		pool4.setTitle("a");
		pool4.setPoints(Float.valueOf(5));
		pool4.getCreatedBy().setDate(new Date());
		pool4.getCreatedBy().setUserId("admin");
		pool4.getModifiedBy().setDate(new Date());
		pool4.getModifiedBy().setUserId("admin");
		pool4.clearMint();
		this.storage.savePool(pool4);

		// mint still, so should not show up
		PoolImpl pool5 = this.storage.newPool();
		pool5.setContext(CONTEXT);
		pool5.setTitle("d");
		pool5.setPoints(Float.valueOf(7));
		pool5.getCreatedBy().setDate(new Date());
		pool5.getCreatedBy().setUserId("admin");
		pool5.getModifiedBy().setDate(new Date());
		pool5.getModifiedBy().setUserId("admin");
		this.storage.savePool(pool5);

		// title_a
		List<PoolImpl> pools = this.storage.findPools(CONTEXT, PoolService.FindPoolsSort.title_a);
		assertTrue(pools != null);
		assertTrue(pools.size() == 4);
		assertTrue(pools.get(0).equals(pool1));
		assertTrue(pools.get(1).equals(pool4));
		assertTrue(pools.get(2).equals(pool2));
		assertTrue(pools.get(3).equals(pool3));

		// title_d
		pools = this.storage.findPools(CONTEXT, PoolService.FindPoolsSort.title_d);
		assertTrue(pools != null);
		assertTrue(pools.size() == 4);
		assertTrue(pools.get(0).equals(pool3));
		assertTrue(pools.get(1).equals(pool2));
		assertTrue(pools.get(2).equals(pool4));
		assertTrue(pools.get(3).equals(pool1));

		// points_a
		pools = this.storage.findPools(CONTEXT, PoolService.FindPoolsSort.points_a);
		assertTrue(pools != null);
		assertTrue(pools.size() == 4);
		assertTrue(pools.get(0).equals(pool3));
		assertTrue(pools.get(1).equals(pool1));
		assertTrue(pools.get(2).equals(pool4));
		assertTrue(pools.get(3).equals(pool2));

		// points_d
		pools = this.storage.findPools(CONTEXT, PoolService.FindPoolsSort.points_d);
		assertTrue(pools != null);
		assertTrue(pools.size() == 4);
		assertTrue(pools.get(0).equals(pool2));
		assertTrue(pools.get(1).equals(pool4));
		assertTrue(pools.get(2).equals(pool1));
		assertTrue(pools.get(3).equals(pool3));
	}

	/**
	 * Test getPool()
	 * 
	 * @throws Exception
	 */
	public void test005getPool() throws Exception
	{
		// create a pool
		Date now = new Date();
		PoolImpl pool = this.storage.newPool();
		pool.setContext(CONTEXT);
		pool.setTitle(CONTEXT);
		pool.getCreatedBy().setDate(now);
		pool.getCreatedBy().setUserId("admin");
		pool.getModifiedBy().setDate(now);
		pool.getModifiedBy().setUserId("admin");
		this.storage.savePool(pool);

		// find it
		PoolImpl found = this.storage.getPool(pool.getId());
		assertTrue(found != null);
		assertTrue(found.getId().equals(pool.getId()));
		assertTrue(found.getTitle().equals(pool.getTitle()));
		assertTrue(found.getCreatedBy().getDate().equals(pool.getCreatedBy().getDate()));
		assertTrue(found.getCreatedBy().getUserId().equals(pool.getCreatedBy().getUserId()));
		assertTrue(found.getModifiedBy().getDate().equals(pool.getModifiedBy().getDate()));
		assertTrue(found.getModifiedBy().getUserId().equals(pool.getModifiedBy().getUserId()));

		// remove it
		this.storage.removePool(pool);

		// it should not exist
		found = this.storage.getPool(pool.getId());
		assertTrue(found == null);
	}

	/**
	 * Test getPools()
	 * 
	 * @throws Exception
	 */
	public void test006getPools() throws Exception
	{
		// create some pools
		PoolImpl pool1 = this.storage.newPool();
		pool1.setContext(CONTEXT);
		pool1.setTitle("a");
		pool1.setPoints(Float.valueOf(5));
		pool1.getCreatedBy().setDate(new Date());
		pool1.getCreatedBy().setUserId("admin");
		pool1.getModifiedBy().setDate(new Date());
		pool1.getModifiedBy().setUserId("admin");
		pool1.clearMint();
		this.storage.savePool(pool1);

		PoolImpl pool2 = this.storage.newPool();
		pool2.setContext(CONTEXT);
		pool2.setTitle("b");
		pool2.setPoints(Float.valueOf(10));
		pool2.getCreatedBy().setDate(new Date());
		pool2.getCreatedBy().setUserId("admin");
		pool2.getModifiedBy().setDate(new Date());
		pool2.getModifiedBy().setUserId("admin");
		pool2.clearMint();
		this.storage.savePool(pool2);

		PoolImpl pool3 = this.storage.newPool();
		pool3.setContext(CONTEXT);
		pool3.setTitle("c");
		pool3.setPoints(Float.valueOf(1));
		pool3.getCreatedBy().setDate(new Date());
		pool3.getCreatedBy().setUserId("admin");
		pool3.getModifiedBy().setDate(new Date());
		pool3.getModifiedBy().setUserId("admin");
		pool3.clearMint();
		this.storage.savePool(pool3);

		PoolImpl pool4 = this.storage.newPool();
		pool4.setContext(CONTEXT);
		pool4.setTitle("a");
		pool4.setPoints(Float.valueOf(5));
		pool4.getCreatedBy().setDate(new Date());
		pool4.getCreatedBy().setUserId("admin");
		pool4.getModifiedBy().setDate(new Date());
		pool4.getModifiedBy().setUserId("admin");
		pool4.clearMint();
		this.storage.savePool(pool4);

		// mint still, so should not show up
		PoolImpl pool5 = this.storage.newPool();
		pool5.setContext(CONTEXT);
		pool5.setTitle("d");
		pool5.setPoints(Float.valueOf(7));
		pool5.getCreatedBy().setDate(new Date());
		pool5.getCreatedBy().setUserId("admin");
		pool5.getModifiedBy().setDate(new Date());
		pool5.getModifiedBy().setUserId("admin");
		this.storage.savePool(pool5);

		List<PoolImpl> pools = this.storage.getPools(CONTEXT,false);
		assertTrue(pools != null);
		assertTrue(pools.size() == 4);
		assertTrue(pools.get(0).equals(pool1));
		assertTrue(pools.get(1).equals(pool2));
		assertTrue(pools.get(2).equals(pool3));
		assertTrue(pools.get(3).equals(pool4));
	}

	/**
	 * Test newPool()
	 * 
	 * @throws Exception
	 */
	public void test007newPool() throws Exception
	{
		// create a pool
		PoolImpl pool = this.storage.newPool();
		assertTrue(pool != null);
		assertTrue(pool.getId() == null);
	}

	/**
	 * Test newPool()
	 * 
	 * @throws Exception
	 */
	public void test008newPool() throws Exception
	{
		// create one to copy
		PoolImpl pool1 = this.storage.newPool();
		pool1.setContext(CONTEXT);
		pool1.setTitle("a");
		pool1.setPoints(Float.valueOf(5));
		pool1.getCreatedBy().setDate(new Date());
		pool1.getCreatedBy().setUserId("admin");
		pool1.getModifiedBy().setDate(new Date());
		pool1.getModifiedBy().setUserId("admin");
		pool1.clearMint();
		this.storage.savePool(pool1);

		pool1 = this.storage.getPool(pool1.getId());

		// create a copy
		PoolImpl pool = this.storage.clone(pool1);
		assertTrue(pool != null);
		assertTrue(pool.getId() != null);
		assertTrue(pool.getId().equals(pool1.getId()));
		assertTrue(pool.getTitle().equals(pool1.getTitle()));
		assertTrue(pool.getCreatedBy().getDate().equals(pool1.getCreatedBy().getDate()));
		assertTrue(pool.getCreatedBy().getUserId().equals(pool1.getCreatedBy().getUserId()));
		assertTrue(pool.getModifiedBy().getDate().equals(pool1.getModifiedBy().getDate()));
		assertTrue(pool.getModifiedBy().getUserId().equals(pool1.getModifiedBy().getUserId()));
	}

	/**
	 * @param arg0
	 */
	protected void setUp() throws Exception
	{
		super.setUp();

		SakaiBasicDataSource ds = setupDataSource();
		if (ds != null)
		{
			// the thread local manager
			ThreadLocalComponent tl = new ThreadLocalComponent();
			tl.init();
			thread_localManager = tl;

			// the SqlService
			SqlServiceTest sql = new SqlServiceTest();
			sql.setVendor(vendor());
			sql.setDefaultDataSource(ds);
			sql.setThreadLocalManager(thread_localManager);
			sql.init();
			sqlService = sql;
		}

		// finally, our target...
		PoolStorage s = setupPoolStorage();
		storage = s;

		// clean up from any prior tests
		storage.clearContext(CONTEXT);
	}

	protected abstract SakaiBasicDataSource setupDataSource();

	protected abstract PoolStorage setupPoolStorage();

	/**
	 * @param arg0
	 */
	protected void tearDown() throws Exception
	{
		// clean up from any prior tests
		storage.clearContext(CONTEXT);

		teardownPoolStorage();
		if (sqlService != null) sqlService.destroy();
		if (thread_localManager != null) thread_localManager.destroy();

		super.tearDown();
	}

	protected abstract void teardownPoolStorage();

	protected abstract String vendor();
}
