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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test manifest
 */
public class ManifestTestX extends TestX
{
	// public static final String VENDOR = "oracle";

	public static final String VENDOR = "mysql";

	/** Logger. */
	private static final Log log = LogFactory.getLog(ManifestTestX.class);

	/**
	 * @param arg0
	 */
	public ManifestTestX(String arg0)
	{
		super(arg0);
	}

	public void test001() throws Exception
	{
		// find all the historical pool ids
		String sql = "SELECT ID FROM MNEME_POOL WHERE HISTORICAL='1'";
		List<String> poolIds = this.sqlService.dbRead(sql);

		// check each one
		for (String pid : poolIds)
		{
			// read the questions as the manifest was read
			sql = "SELECT M.QUESTION_ID FROM MNEME_POOL_MANIFEST M WHERE M.POOL_ID = ? ORDER BY M.QUESTION_ID ASC";
			Object[] fields = new Object[1];
			fields[0] = Long.valueOf(pid);
			List<String> manifest = this.sqlService.dbRead(sql, fields, null);

			// read the questions as we propose to read it in 1.1, directly from the questions
			sql = "SELECT Q.ID FROM MNEME_QUESTION Q WHERE Q.POOL_ID = ? ORDER BY Q.ID ASC";
			List<String> questions = this.sqlService.dbRead(sql, fields, null);

			assertTrue(listEquals(manifest, questions));
		}
	}

	protected boolean listEquals(List<String> a, List<String> b)
	{
		if (a.size() != b.size()) return false;
		for (int i = 0; i < a.size(); i++)
		{
			if (!a.get(i).equals(b.get(i))) return false;
		}
		return true;
	}

	/**
	 * @param arg0
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @param arg0
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	protected String vendor()
	{
		return VENDOR;
	}
}
