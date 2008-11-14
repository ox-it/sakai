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

import org.apache.commons.dbcp.SakaiBasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.impl.PoolStorage;
import org.etudes.mneme.impl.PoolStorageOracle;

/**
 * Test PoolStorageOracle.<br />
 * Note: to use, change the setupDataSource() code to connect to your Oracle db.<br />
 * The schema will be created if missing via auto-ddl.
 */
public class PoolStorageTestXoracle extends PoolStorageTestX
{
	public class MyPoolStorageOracle extends PoolStorageOracle
	{
		public PoolImpl newPool()
		{
			return new PoolImpl();
		}
	}

	/** Logger. */
	private static final Log log = LogFactory.getLog(PoolStorageTestXoracle.class);

	/**
	 * @param arg0
	 */
	public PoolStorageTestXoracle(String arg0)
	{
		super(arg0);
	}

	protected SakaiBasicDataSource setupDataSource()
	{
		// a data source (see db/pack/components.xml javax.sql.BaseDataSource)
		SakaiBasicDataSource ds = new SakaiBasicDataSource();
		ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		ds.setUrl("jdbc:oracle:thin:@localhost:11004:sakaidev");
		ds.setUsername("ggolden");
		ds.setPassword("ggolden");
		ds.setInitialSize(10);
		ds.setMaxActive(10);
		ds.setMaxIdle(10);
		ds.setMinIdle(10);
		ds.setMaxWait(300000);
		ds.setNumTestsPerEvictionRun(3);
		ds.setTestOnBorrow(true);
		ds.setTestOnReturn(false);
		ds.setTestWhileIdle(false);
		ds.setValidationQuery("select 1 from DUAL");
		ds.setDefaultAutoCommit(false);
		ds.setDefaultReadOnly(false);
		ds.setDefaultTransactionIsolationString("TRANSACTION_READ_COMMITTED");
		ds.setPoolPreparedStatements(false);
		ds.setMaxOpenPreparedStatements(0);
		ds.setMinEvictableIdleTimeMillis(1800000);
		ds.setTimeBetweenEvictionRunsMillis(900000);

		return ds;
	}

	protected PoolStorage setupPoolStorage()
	{
		PoolStorageOracle s = new MyPoolStorageOracle();
		s.setAutoDdl("true");
		s.setSqlService(sqlService);
		s.setThreadLocalManager(thread_localManager);
		s.init();

		return s;
	}

	protected void teardownPoolStorage()
	{
		((PoolStorageOracle) storage).destroy();
	}

	protected String vendor()
	{
		return "oracle";
	}
}
