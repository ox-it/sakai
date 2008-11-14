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

import java.util.List;

import org.apache.commons.dbcp.SakaiBasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test char db support in mysql
 */
public class CharTestXmysql extends TestX
{
	/** Logger. */
	private static final Log log = LogFactory.getLog(CharTestXmysql.class);

	final String ONEBYTE_OVERLONG = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

	final String ONEBYTE_JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345";

	final String MULTIBYTE_OVERLONG = "ºÃ©Œ§678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

	final String MULTIBYTE_JUSTRIGHT = "ºÃ©Œ§6789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345";

	/**
	 * @param arg0
	 */
	public CharTestXmysql(String arg0)
	{
		super(arg0);
	}

	protected String trimTo255(String source)
	{
		if (source.length() > 255) return source.substring(0, 255);
		return source;
	}

	public void test001() throws Exception
	{
		String sql = "insert into chartest1 (col1) values (?)";
		Object[] fields = new Object[1];
		fields[0] = ONEBYTE_OVERLONG;
		try
		{
			this.sqlService.dbWrite(sql, fields);
			fail("expecting runtime exception");

		}
		catch (RuntimeException e)
		{
		}

		fields[0] = trimTo255(ONEBYTE_OVERLONG);
		this.sqlService.dbWrite(sql, fields);

		sql = "select col1 from chartest1";
		List<String> results = this.sqlService.dbRead(sql);
		assertTrue(results.size() == 1);
		assertTrue(results.get(0).equals(ONEBYTE_JUSTRIGHT));
	}

	public void test002() throws Exception
	{
		String sql = "insert into chartest1 (col1) values (?)";
		Object[] fields = new Object[1];
		fields[0] = MULTIBYTE_OVERLONG;
		try
		{
			this.sqlService.dbWrite(sql, fields);
			fail("expecting runtime exception");

		}
		catch (RuntimeException e)
		{
		}

		fields[0] = trimTo255(MULTIBYTE_OVERLONG);
		this.sqlService.dbWrite(sql, fields);

		sql = "select col1 from chartest1";
		List<String> results = this.sqlService.dbRead(sql);
		assertTrue(results.size() == 1);
		assertTrue(results.get(0).equals(MULTIBYTE_JUSTRIGHT));
	}

	/**
	 * @param arg0
	 */
	protected void setUp() throws Exception
	{
		super.setUp();

		// clear out any prior test stuff in db
		String sql = "drop table chartest1";
		this.sqlService.dbWriteFailQuiet(null, sql, null);

		// create a test table
		sql = "create table chartest1 (col1 varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=utf8";
		this.sqlService.dbWrite(sql);
	}

	protected SakaiBasicDataSource setupDataSource()
	{
		// a data source (see db/pack/components.xml javax.sql.BaseDataSource)
		SakaiBasicDataSource ds = new SakaiBasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://127.0.0.1:3306/sakai?useUnicode=true&characterEncoding=UTF-8");
		ds.setUsername("sakaiuser");
		ds.setPassword("password");
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

	/**
	 * @param arg0
	 */
	protected void tearDown() throws Exception
	{
		// clear out any prior test stuff in db
		String sql = "drop table chartest1";
		this.sqlService.dbWriteFailQuiet(null, sql, null);

		super.tearDown();
	}

	protected String vendor()
	{
		return "mysql";
	}
}
