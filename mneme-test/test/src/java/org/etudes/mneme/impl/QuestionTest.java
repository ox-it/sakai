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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolGetService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionGetService;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.SubmissionUnscoredQuestionService;

/**
 * Test Question.
 */
public class QuestionTest extends TestCase
{
	public class MyMnemeService implements MnemeService
	{
		/**
		 * {@inheritDoc}
		 */
		public QuestionPlugin getQuestionPlugin(String type)
		{
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public List<QuestionPlugin> getQuestionPlugins()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void registerQuestionPlugin(QuestionPlugin plugin)
		{
			// TODO Auto-generated method stub

		}
	}

	public class MyPoolGetService implements PoolGetService
	{
		/**
		 * {@inheritDoc}
		 */
		public Pool getPool(String poolId)
		{
			PoolImpl rv = new PoolImpl();
			rv.initId(poolId);
			rv.setContext("context");
			return rv;
		}
	}

	public class MyQuestionGetService implements QuestionGetService
	{
		/**
		 * {@inheritDoc}
		 */
		public Question getQuestion(String questionId)
		{
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class MySubmissionUnscoredQuestionService implements SubmissionUnscoredQuestionService
	{
		/**
		 * {@inheritDoc}
		 */
		public Boolean getAssessmentQuestionHasUnscoredSubmissions(Assessment assessment, Question question)
		{
			return Boolean.TRUE;
		}
	}

	/** Logger. */
	private static final Log log = LogFactory.getLog(QuestionTest.class);

	protected MnemeService mnemeService = null;

	protected PoolGetService poolGetService = null;

	protected QuestionImpl question = null;

	protected QuestionGetService questionGetService = null;

	protected SubmissionUnscoredQuestionService submissionUnscoredQuestionService = null;

	/**
	 * @param arg0
	 */
	public QuestionTest(String arg0)
	{
		super(arg0);
	}

	public void testAttribution1() throws Exception
	{
		Date now = new Date();
		final String TESTER = "tester";

		question.getCreatedBy().setDate(now);
		question.getCreatedBy().setUserId(TESTER);
		assertTrue(question.getCreatedBy().getDate().equals(now));
		assertTrue(question.getCreatedBy().getUserId().equals(TESTER));

		// too long (length=100)
		final String OVERLONG = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		final String JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";

		question.getCreatedBy().setUserId(JUSTRIGHT);
		assertTrue(question.getCreatedBy().getUserId().equals(JUSTRIGHT));

		try
		{
			question.getCreatedBy().setUserId(OVERLONG);
			fail("expected illegal argument");
		}
		catch (IllegalArgumentException e)
		{
		}
		assertTrue(question.getCreatedBy().getUserId().equals(JUSTRIGHT));

		question.getCreatedBy().setDate(null);
		question.getCreatedBy().setUserId(null);
		assertTrue(question.getCreatedBy().getDate() == null);
		assertTrue(question.getCreatedBy().getUserId() == null);
	}

	public void testAttribution2() throws Exception
	{
		Date now = new Date();
		final String TESTER = "tester";

		question.getModifiedBy().setDate(now);
		question.getModifiedBy().setUserId(TESTER);
		assertTrue(question.getModifiedBy().getDate().equals(now));
		assertTrue(question.getModifiedBy().getUserId().equals(TESTER));

		// too long (length=100)
		final String OVERLONG = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		final String JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";

		question.getModifiedBy().setUserId(JUSTRIGHT);
		assertTrue(question.getModifiedBy().getUserId().equals(JUSTRIGHT));

		try
		{
			question.getModifiedBy().setUserId(OVERLONG);
			fail("expected illegal argument");
		}
		catch (IllegalArgumentException e)
		{
		}
		assertTrue(question.getModifiedBy().getUserId().equals(JUSTRIGHT));

		question.getModifiedBy().setDate(null);
		question.getModifiedBy().setUserId(null);
		assertTrue(question.getModifiedBy().getDate() == null);
		assertTrue(question.getModifiedBy().getUserId() == null);
	}

	public void testChanged() throws Exception
	{
		question.clearChanged();
		assertTrue(question.getIsChanged().equals(Boolean.FALSE));
		question.setChanged();
		assertTrue(question.getIsChanged().equals(Boolean.TRUE));

		question.clearChanged();
		assertTrue(question.getIsChanged().equals(Boolean.FALSE));
		question.setChanged();
		assertTrue(question.getIsChanged().equals(Boolean.TRUE));

		question.setExplainReason(Boolean.TRUE);
		question.clearChanged();
		question.setExplainReason(Boolean.TRUE);
		assertTrue(question.getIsChanged().equals(Boolean.FALSE));
		question.setExplainReason(Boolean.FALSE);
		assertTrue(question.getIsChanged().equals(Boolean.TRUE));

		question.setFeedback("");
		question.clearChanged();
		question.setFeedback("");
		assertTrue(question.getIsChanged().equals(Boolean.FALSE));
		question.setFeedback("x");
		assertTrue(question.getIsChanged().equals(Boolean.TRUE));

		question.setHints("");
		question.clearChanged();
		question.setHints("");
		assertTrue(question.getIsChanged().equals(Boolean.FALSE));
		question.setHints("x");
		assertTrue(question.getIsChanged().equals(Boolean.TRUE));

		Pool p1 = this.poolGetService.getPool("1");
		Pool p2 = this.poolGetService.getPool("2");
		question.setPool(p1);
		question.clearChanged();
		question.setPool(p1);
		assertTrue(question.getIsChanged().equals(Boolean.FALSE));
		question.setPool(p2);
		assertTrue(question.getIsChanged().equals(Boolean.TRUE));

		question.getPresentation().setText("");
		question.clearChanged();
		question.getPresentation().setText("");
		assertTrue(question.getIsChanged().equals(Boolean.FALSE));
		question.getPresentation().setText("x");
		assertTrue(question.getIsChanged().equals(Boolean.TRUE));
	}

	/**
	 * Test the context
	 * 
	 * @throws Exception
	 */
	public void testContext() throws Exception
	{
		// normal
		final String CONTEXT_NORMAL = "contextX";
		question.initContext(CONTEXT_NORMAL);
		assertTrue(question.getContext().equals(CONTEXT_NORMAL));

		Pool pool = poolGetService.getPool("1");
		question.setPool(pool);
		assertTrue(question.getContext().equals("context"));
	}

	public void testDescription() throws Exception
	{
		// String[] data = new String[0];
		// question.getTypeSpecificQuestion().setData(data);
		question.getPresentation().setText(null);
		assertTrue(question.getDescription() == null);

		question.getPresentation().setText("");
		assertTrue(question.getDescription() == null);

		question.getPresentation().setText("the question");
		assertTrue(question.getDescription().equals("the question"));

		question.getPresentation().setText("  title   ");
		assertTrue(question.getDescription().equals("title"));

		final String TITLE_OVERLONG = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		final String TITLE_JUSTRIGHT = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345";

		question.getPresentation().setText(TITLE_OVERLONG);
		assertTrue(question.getDescription().equals(TITLE_JUSTRIGHT));

		question.getPresentation().setText("");
		assertTrue(question.getDescription() == null);
		question.getPresentation().setText(TITLE_JUSTRIGHT);
		assertTrue(question.getDescription().equals(TITLE_JUSTRIGHT));

		question.getPresentation().setText("<p>this is some <b>html</b></p>");
		assertTrue(question.getDescription().equals("this is some html"));
	}

	public void testExplainReason() throws Exception
	{
		// default
		assertTrue(question.getExplainReason().equals(Boolean.FALSE));

		try
		{
			question.setExplainReason(null);
			fail("expected IllegalArgumentExceptionn");
		}
		catch (IllegalArgumentException e)
		{
		}

		question.setExplainReason(Boolean.TRUE);
		assertTrue(question.getExplainReason().equals(Boolean.TRUE));

		question.setExplainReason(Boolean.FALSE);
		assertTrue(question.getExplainReason().equals(Boolean.FALSE));
	}

	public void testFeedback() throws Exception
	{
		// default
		assertTrue(question.getFeedback() == null);

		question.setFeedback(null);
		assertTrue(question.getFeedback() == null);

		question.setFeedback("");
		assertTrue(question.getFeedback() == null);

		question.setFeedback("feedback");
		assertTrue(question.getFeedback().equals("feedback"));

		question.setFeedback("   feedback   ");
		assertTrue(question.getFeedback().equals("feedback"));

		final String CHARS260 = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		question.setFeedback(CHARS260);
		assertTrue(question.getFeedback().equals(CHARS260));

		question.setFeedback("<p>this is some <b>html</b></p>");
		assertTrue(question.getFeedback().equals("<p>this is some <b>html</b></p>"));
	}

	public void testGetHasUnscoredSubmissions() throws Exception
	{
		assert (question.getHasUnscoredSubmissions().equals(Boolean.TRUE));
	}

	public void testHints() throws Exception
	{
		// default
		assertTrue(question.getHints() == null);

		question.setHints(null);
		assertTrue(question.getHints() == null);

		question.setHints("");
		assertTrue(question.getHints() == null);

		question.setHints("feedback");
		assertTrue(question.getHints().equals("feedback"));

		question.setHints("   feedback   ");
		assertTrue(question.getHints().equals("feedback"));

		final String CHARS260 = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		question.setHints(CHARS260);
		assertTrue(question.getHints().equals(CHARS260));

		question.setHints("<p>this is some <b>html</b></p>");
		assertTrue(question.getHints().equals("<p>this is some <b>html</b></p>"));
	}

	public void testHistorical() throws Exception
	{
		assertTrue(question.getIsHistorical().equals(Boolean.FALSE));

		question.initHistorical(Boolean.TRUE);
		assertTrue(question.getIsHistorical().equals(Boolean.TRUE));

		question.initHistorical(Boolean.FALSE);
		assertTrue(question.getIsHistorical().equals(Boolean.FALSE));
	}

	public void testId() throws Exception
	{
		question.initId("1");
		assertTrue(question.getId().equals("1"));
	}

	public void testMint() throws Exception
	{
		assertTrue(question.getMint().equals(Boolean.TRUE));

		question.initMint(Boolean.FALSE);
		assertTrue(question.getMint().equals(Boolean.FALSE));

		question.initMint(Boolean.TRUE);
		assertTrue(question.getMint().equals(Boolean.TRUE));

		question.clearMint();
		assertTrue(question.getMint().equals(Boolean.FALSE));
	}

	public void testPlugin() throws Exception
	{
		assertTrue(question.getType().equals("mneme:TrueFalse"));
		assertTrue(question.getTypeName().equals("True / False"));
	}

	public void testPool() throws Exception
	{
		Pool pool = poolGetService.getPool("1");
		question.setPool(pool);
		assertTrue(question.getPool().getId().equals("1"));
	}

	public void testPresentation() throws Exception
	{
		assertTrue(question.getPresentation() != null);
		assertTrue(question.getPresentation().getText() == null);

		question.getPresentation().setText("");
		assertTrue(question.getPresentation().getText() == null);

		question.getPresentation().setText(null);
		assertTrue(question.getPresentation().getText() == null);

		question.getPresentation().setText("the question");
		assertTrue(question.getPresentation().getText().equals("the question"));

		question.getPresentation().setText("  title   ");
		assertTrue(question.getPresentation().getText().equals("title"));

		final String CHARS260 = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		question.getPresentation().setText(CHARS260);
		assertTrue(question.getPresentation().getText().equals(CHARS260));

		question.getPresentation().setText("<p>this is some <b>html</b></p>");
		assertTrue(question.getPresentation().getText().equals("<p>this is some <b>html</b></p>"));
	}

	/**
	 * @param arg0
	 */
	protected void setUp() throws Exception
	{
		super.setUp();

		questionGetService = new MyQuestionGetService();
		poolGetService = new MyPoolGetService();
		submissionUnscoredQuestionService = new MySubmissionUnscoredQuestionService();
		mnemeService = new MyMnemeService();

		// plugins
		TrueFalsePlugin tf = new TrueFalsePlugin();
		tf.setBundle("mnemeTrueFalse");
		tf.setMnemeService(mnemeService);
		tf.init();

		TaskPlugin tk = new TaskPlugin();
		tk.setBundle("mnemeTask");
		tk.setMnemeService(mnemeService);
		tk.init();

		MultipleChoicePlugin mp = new MultipleChoicePlugin();
		mp.setBundle("mnemeMultipleChoice");
		mp.setMnemeService(mnemeService);
		mp.init();

		MatchPlugin mt = new MatchPlugin();
		mt.setBundle("mnemeMatch");
		mt.setMnemeService(mnemeService);
		mt.init();

		LikertScalePlugin lk = new LikertScalePlugin();
		lk.setBundle("mnemeLikertScale");
		lk.setMnemeService(mnemeService);
		lk.init();

		FillBlanksPlugin fb = new FillBlanksPlugin();
		fb.setBundle("mnemeFillBlanks");
		fb.setMnemeService(mnemeService);
		fb.init();

		EssayPlugin ey = new EssayPlugin();
		ey.setBundle("mnemeEssay");
		ey.setMnemeService(mnemeService);
		ey.init();

		// question
		QuestionImpl q = new QuestionImpl();
		q.setQuestionService(questionGetService);
		q.setPoolService(poolGetService);
		q.setSubmissionService(submissionUnscoredQuestionService);
		q.initTypeSpecificQuestion(tf.newQuestion(q));
		q.initType(tf.getType());
		question = q;
	}

	/**
	 * @param arg0
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
}
