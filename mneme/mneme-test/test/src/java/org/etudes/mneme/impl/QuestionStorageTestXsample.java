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

/**
 * Test QuestionStorageSample
 */
public class QuestionStorageTestXsample extends QuestionStorageTestX
{
	public class MyPoolStorageSample extends PoolStorageSample
	{
		public PoolImpl newPool()
		{
			// TODO: setters
			return new PoolImpl();
		}
	}

	public class MyQuestionStorageSample extends QuestionStorageSample
	{
		public QuestionImpl newQuestion()
		{
			// TODO: setters
			return new QuestionImpl();
		}
	}

	/** Logger. */
	private static final Log log = LogFactory.getLog(QuestionStorageTestXsample.class);

	/**
	 * @param arg0
	 */
	public QuestionStorageTestXsample(String arg0)
	{
		super(arg0);
	}

	protected SakaiBasicDataSource setupDataSource()
	{
		return null;
	}

	protected PoolStorage setupPoolStorage()
	{
		PoolStorageSample s = new MyPoolStorageSample();
		s.init();

		return s;
	}

	protected QuestionStorage setupQuestionStorage()
	{
		QuestionStorageSample s = new MyQuestionStorageSample();
		s.setMnemeService(mnemeService);
		s.init();

		return s;
	}

	protected void teardownPoolStorage()
	{
		((PoolStorageSample) poolStorage).destroy();
	}

	protected void teardownQuestionStorage()
	{
		((QuestionStorageSample) storage).destroy();
	}

	protected String vendor()
	{
		return null;
	}
}
