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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPoolService;
import org.etudes.mneme.api.Shuffler;
import org.etudes.mneme.impl.PoolImpl;

/**
 * Test Pool.
 */
public class PoolTest extends TestCase
{
	public class MyQuestionService implements QuestionPoolService
	{
		/**
		 * {@inheritDoc}
		 */
		public Integer countQuestions(Pool pool, String search, String questionType, Boolean survey, Boolean valid)
		{
			return Integer.valueOf(5);
		}

		/**
		 * {@inheritDoc}
		 */
		public List<Question> findQuestions(Pool pool, FindQuestionsSort sort, String search, String questionType, Integer pageNum, Integer pageSize,
				Boolean survey, Boolean valid)
		{
			return new ArrayList<Question>();
		}

		/**
		 * {@inheritDoc}
		 */
		public List<String> getPoolQuestionIds(Pool pool, Boolean survey, Boolean valid)
		{
			List<String> rv = new ArrayList<String>();
			rv.add("1");
			rv.add("2");
			rv.add("3");
			rv.add("4");
			rv.add("5");

			return rv;
		}
	}

	protected class ShufflerImpl implements Shuffler
	{
		protected long seed = 0;

		public ShufflerImpl(long seed)
		{
			this.seed = seed;
		}

		/**
		 * {@inheritDoc}
		 */
		public void shuffle(List<? extends Object> source, String extra)
		{
			Collections.shuffle(source, new Random(this.seed));
		}
	}

	/** Logger. */
	private static final Log log = LogFactory.getLog(PoolTest.class);

	protected Pool pool = null;

	protected QuestionPoolService questionPoolService = null;

	/**
	 * @param arg0
	 */
	public PoolTest(String arg0)
	{
		super(arg0);
	}

	public void testAttribution1() throws Exception
	{
		Date now = new Date();
		final String TESTER = "tester";

		pool.getCreatedBy().setDate(now);
		pool.getCreatedBy().setUserId(TESTER);
		assertTrue(pool.getCreatedBy().getDate().equals(now));
		assertTrue(pool.getCreatedBy().getUserId().equals(TESTER));

		// too long (length=100)
		final String OVERLONG = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		final String JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";

		pool.getCreatedBy().setUserId(JUSTRIGHT);
		assertTrue(pool.getCreatedBy().getUserId().equals(JUSTRIGHT));

		try
		{
			pool.getCreatedBy().setUserId(OVERLONG);
			fail("expected illegal argument");
		}
		catch (IllegalArgumentException e)
		{
		}
		assertTrue(pool.getCreatedBy().getUserId().equals(JUSTRIGHT));

		pool.getCreatedBy().setDate(null);
		pool.getCreatedBy().setUserId(null);
		assertTrue(pool.getCreatedBy().getDate() == null);
		assertTrue(pool.getCreatedBy().getUserId() == null);
	}

	public void testAttribution2() throws Exception
	{
		Date now = new Date();
		final String TESTER = "tester";

		pool.getModifiedBy().setDate(now);
		pool.getModifiedBy().setUserId(TESTER);
		assertTrue(pool.getModifiedBy().getDate().equals(now));
		assertTrue(pool.getModifiedBy().getUserId().equals(TESTER));

		// too long (length=100)
		final String OVERLONG = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		final String JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";

		pool.getModifiedBy().setUserId(JUSTRIGHT);
		assertTrue(pool.getModifiedBy().getUserId().equals(JUSTRIGHT));

		try
		{
			pool.getModifiedBy().setUserId(OVERLONG);
			fail("expected illegal argument");
		}
		catch (IllegalArgumentException e)
		{
		}
		assertTrue(pool.getModifiedBy().getUserId().equals(JUSTRIGHT));

		pool.getModifiedBy().setDate(null);
		pool.getModifiedBy().setUserId(null);
		assertTrue(pool.getModifiedBy().getDate() == null);
		assertTrue(pool.getModifiedBy().getUserId() == null);
	}

	/**
	 * Test the context
	 * 
	 * @throws Exception
	 */
	public void testContext() throws Exception
	{
		// normal
		final String CONTEXT_NORMAL = "context";
		pool.setContext(CONTEXT_NORMAL);
		assertTrue(pool.getContext().equals(CONTEXT_NORMAL));

		// null
		pool.setContext(null);
		assertTrue(pool.getContext().equals(""));
		pool.setContext("");
		assertTrue(pool.getContext().equals(""));

		// all blanks - no trimming, please
		final String BLANKS = "     ";
		pool.setContext(BLANKS);
		assertTrue(pool.getContext().equals(BLANKS));

		// too long (length=100)
		final String OVERLONG = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		final String JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
		try
		{
			pool.setContext(OVERLONG);
			fail("expecting IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
		}
		assertTrue(pool.getContext().equals(BLANKS));

		pool.setContext(JUSTRIGHT);
		assertTrue(pool.getContext().equals(JUSTRIGHT));
	}

	/**
	 * Test the description: normal, untrimmed, all blanks, too long
	 * 
	 * @throws Exception
	 */
	public void testDescription() throws Exception
	{
		// normal
		final String TITLE_NORMAL = "title";
		pool.setDescription(TITLE_NORMAL);
		assertTrue(pool.getDescription().equals(TITLE_NORMAL));
		pool.setDescription(null);
		assertTrue(pool.getDescription() == null);

		// untrimmed
		final String TITLE_W_BLANKS = "  title   ";
		final String TITLE_WO_BLANKS = "title";
		pool.setDescription(TITLE_W_BLANKS);
		assertTrue(pool.getDescription().equals(TITLE_WO_BLANKS));

		// all blanks
		final String TITLE_BLANKS = "     ";
		pool.setDescription(TITLE_BLANKS);
		assertTrue(pool.getDescription() == null);

		// not limited to 255
		final String TITLE_OVERLONG = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		// final String TITLE_JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345";
		pool.setDescription(TITLE_OVERLONG);
		assertTrue(pool.getDescription().equals(TITLE_OVERLONG));
	}

	/**
	 * Test the difficulty
	 * 
	 * @throws Exception
	 */
	public void testDifficulty() throws Exception
	{
		// test default
		assertTrue(pool.getDifficulty() == 3);

		try
		{
			pool.setDifficulty(null);
			fail("expecting IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
		}
		assertTrue(pool.getDifficulty() == 3);

		pool.setDifficulty(Integer.valueOf(0));
		assertTrue(pool.getDifficulty() == 1);

		pool.setDifficulty(Integer.valueOf(1));
		assertTrue(pool.getDifficulty() == 1);

		pool.setDifficulty(Integer.valueOf(2));
		assertTrue(pool.getDifficulty() == 2);

		pool.setDifficulty(Integer.valueOf(3));
		assertTrue(pool.getDifficulty() == 3);

		pool.setDifficulty(Integer.valueOf(4));
		assertTrue(pool.getDifficulty() == 4);

		pool.setDifficulty(Integer.valueOf(5));
		assertTrue(pool.getDifficulty() == 5);

		pool.setDifficulty(Integer.valueOf(6));
		assertTrue(pool.getDifficulty() == 5);
	}

	public void testDrawQuestionIds() throws Exception
	{
		// 0 - 5,3,2,4,1
		// 22 - 5,4,2,1,3
		List<String> ids = pool.drawQuestionIds(new ShufflerImpl(22l), Integer.valueOf(3), null);
		assertTrue(ids != null);
		assertTrue(ids.size() == 3);
		assertTrue(ids.get(0).equals("5"));
		assertTrue(ids.get(1).equals("4"));
		assertTrue(ids.get(2).equals("2"));

		ids = pool.drawQuestionIds(new ShufflerImpl(0l), Integer.valueOf(1), null);
		assertTrue(ids != null);
		assertTrue(ids.size() == 1);
		assertTrue(ids.get(0).equals("5"));

		try
		{
			ids = pool.drawQuestionIds(new ShufflerImpl(0l), Integer.valueOf(-1), null);
			fail("excepted IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
		}

		try
		{
			ids = pool.drawQuestionIds(new ShufflerImpl(0l), null, null);
			fail("excepted IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
		}

		ids = pool.drawQuestionIds(new ShufflerImpl(22l), Integer.valueOf(50), null);
		assertTrue(ids != null);
		assertTrue(ids.size() == 5);
		assertTrue(ids.get(0).equals("5"));
		assertTrue(ids.get(1).equals("4"));
		assertTrue(ids.get(2).equals("2"));
		assertTrue(ids.get(3).equals("1"));
		assertTrue(ids.get(4).equals("3"));
	}

	public void testFlags() throws Exception
	{
		assertTrue(pool.getMint().equals(Boolean.TRUE));
		assertTrue(pool.getIsHistorical().equals(Boolean.FALSE));
	}

	public void testGetAllQuestionIds() throws Exception
	{
		List<String> ids = pool.getAllQuestionIds(null, null);
		assertTrue(ids != null);
		assertTrue(ids.size() == 5);
		assertTrue(ids.get(0).equals("1"));
		assertTrue(ids.get(1).equals("2"));
		assertTrue(ids.get(2).equals("3"));
		assertTrue(ids.get(3).equals("4"));
		assertTrue(ids.get(4).equals("5"));
	}

	public void testGetNumQuestions() throws Exception
	{
		Integer count = pool.getNumQuestions();
		assertTrue(count != null);
		assertTrue(count.intValue() == 5);
	}

	/**
	 * Test the points
	 * 
	 * @throws Exception
	 */
	public void testPoints() throws Exception
	{
		// unset, the points should be zero
		final Float ZERO = 0f;
		assertTrue(pool.getPoints().equals(ZERO));
		assertTrue(pool.getPointsEdit() == null);

		// 1.5
		final Float ONE_FIVE = 1.5f;
		pool.setPoints(ONE_FIVE);
		assertTrue(pool.getPoints().equals(ONE_FIVE));
		assertTrue(pool.getPointsEdit().equals(ONE_FIVE));

		// too many decimals - truncate
		final Float ONE_FIVE_PLUS = 1.5719843920f;
		final Float ONE_FIVE_SEVEN = 1.57f;
		pool.setPoints(ONE_FIVE_PLUS);
		assertTrue(pool.getPoints().equals(ONE_FIVE_SEVEN));
		assertTrue(pool.getPointsEdit().equals(ONE_FIVE_SEVEN));

		// too many decimals - round
		final Float ONE_FIVE_PLUS2 = 1.579843920f;
		final Float ONE_FIVE_EIGHT = 1.58f;
		pool.setPoints(ONE_FIVE_PLUS2);
		assertTrue(pool.getPoints().equals(ONE_FIVE_EIGHT));
		assertTrue(pool.getPointsEdit().equals(ONE_FIVE_EIGHT));

		// 0
		pool.setPoints(ZERO);
		assertTrue(pool.getPoints().equals(ZERO));
		assertTrue(pool.getPointsEdit().equals(ZERO));

		// big number
		final Float BIG = Float.valueOf(10000.0f);
		pool.setPoints(BIG);
		assertTrue(pool.getPoints().equals(BIG));
		assertTrue(pool.getPointsEdit().equals(BIG));

		// big number
		pool.setPointsEdit(BIG);
		assertTrue(pool.getPoints().equals(BIG));
		assertTrue(pool.getPointsEdit().equals(BIG));

		// out of range
		pool.setPoints(Float.valueOf(-1.0f));
		assertTrue(pool.getPoints().equals(ZERO));
		assertTrue(pool.getPointsEdit().equals(ZERO));

		pool.setPointsEdit(Float.valueOf(-1.0f));
		assertTrue(pool.getPoints().equals(ZERO));
		assertTrue(pool.getPointsEdit().equals(ZERO));

		pool.setPoints(Float.valueOf(10001.0f));
		assertTrue(pool.getPoints().equals(BIG));
		assertTrue(pool.getPointsEdit().equals(BIG));

		pool.setPointsEdit(Float.valueOf(10001.0f));
		assertTrue(pool.getPoints().equals(BIG));
		assertTrue(pool.getPointsEdit().equals(BIG));

		// null
		pool.setPoints(ONE_FIVE);
		try
		{
			pool.setPoints(null);
			fail("expecting IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
		}
		assertTrue(pool.getPoints().equals(ONE_FIVE));
		assertTrue(pool.getPointsEdit().equals(ONE_FIVE));

		// null, but allowed
		pool.setPointsEdit(null);
		assertTrue(pool.getPointsEdit() == null);
		assertTrue(pool.getPoints().equals(ZERO));

		// value using set setPointsEdit
		pool.setPointsEdit(ONE_FIVE);
		assertTrue(pool.getPoints().equals(ONE_FIVE));
		assertTrue(pool.getPointsEdit().equals(ONE_FIVE));
	}

	/**
	 * Test the title: normal, untrimmed, all blanks, too long
	 * 
	 * @throws Exception
	 */
	public void testTitle() throws Exception
	{
		// normal title
		final String TITLE_NORMAL = "title";
		pool.setTitle(TITLE_NORMAL);
		assertTrue(pool.getTitle().equals(TITLE_NORMAL));
		pool.setTitle(null);
		assertTrue(pool.getTitle() == null);

		// untrimmed title
		final String TITLE_W_BLANKS = "  title   ";
		final String TITLE_WO_BLANKS = "title";
		pool.setTitle(TITLE_W_BLANKS);
		assertTrue(pool.getTitle().equals(TITLE_WO_BLANKS));

		// all blanks
		final String TITLE_BLANKS = "     ";
		pool.setTitle(TITLE_BLANKS);
		assertTrue(pool.getTitle() == null);

		// too long (length=260)
		final String TITLE_OVERLONG = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		final String TITLE_JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345";
		pool.setTitle(TITLE_OVERLONG);
		assertTrue(pool.getTitle().equals(TITLE_JUSTRIGHT));
	}

	/**
	 * @param arg0
	 */
	protected void setUp() throws Exception
	{
		super.setUp();

		questionPoolService = new MyQuestionService();

		PoolImpl p = new PoolImpl();
		p.setQuestionService(questionPoolService);
		pool = p;
	}

	/**
	 * @param arg0
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
}
