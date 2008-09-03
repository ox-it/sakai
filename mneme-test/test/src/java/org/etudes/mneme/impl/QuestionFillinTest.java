/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolGetService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionGetService;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionUnscoredQuestionService;

/**
 * Test Fill In Question type.
 */
public class QuestionFillinTest extends TestCase
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
			rv.setPoints(Float.valueOf(10));
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

	public class MyAnswerImpl extends AnswerImpl
	{
		public MyAnswerImpl(MnemeService service)
		{
			setMnemeService(service);
		}

		/**
		 * {@inheritDoc}
		 */
		public Question getQuestion()
		{
			return question;
		}
	}

	/** Logger. */
	private static final Log log = LogFactory.getLog(QuestionFillinTest.class);

	protected FillBlanksPlugin fillBlanksPlugin = null;

	protected MnemeService mnemeService = null;

	protected PoolGetService poolGetService = null;

	protected QuestionImpl question = null;

	protected QuestionGetService questionGetService = null;

	protected SubmissionUnscoredQuestionService submissionUnscoredQuestionService = null;

	/**
	 * @param arg0
	 */
	public QuestionFillinTest(String arg0)
	{
		super(arg0);
	}

	public void testValidate() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {blue|purple}."; // text
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());

		data[3] = "roses are {red } and violets are {blue |purple}."; // text
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());

		data[3] = "roses are {&nbsp;red} and violets are {blue |&nbsp;purple }."; // text
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());

		data[3] = "roses are {red and violets are {blue|purple}.";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		data[3] = "roses are red} and violets are {blue|purple}.";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		data[3] = "roses are {red}";
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());

		data[3] = "roses are {red";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		data[3] = "roses are red}";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		data[3] = "roses are red{";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		data[3] = "roses are red";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		data[3] = "{red}";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		data[3] = "{}";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());

		// we accept this now, assuming the missing "*"s
		data[3] = "roses are {} and violets are {}.";
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());

		data[3] = "roses are {  } and violets are {  }.";
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());

		data[3] = "roses are {&nbsp;} and violets are {&nbsp; }.";
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());

		data[3] = "";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());
	}

	public void testValidateB() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "Violets are {blue).";
		question.getTypeSpecificQuestion().setData(data);
		assertFalse(question.getIsValid());
		assertTrue(question.getDescription(), question.getDescription().equals("Violets are {blue)."));

		data[3] = "Violets are {blue}.";
		question.getTypeSpecificQuestion().setData(data);
		assertTrue(question.getIsValid());
		assertTrue(question.getDescription(), question.getDescription().equals("Violets are {}."));
	}

	public void test001() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {blue|purple}."; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test002() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {blue | purple}."; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test003() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {blue |     purple}"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test004() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {       blue             |     purple             }"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test005() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {blue|5}"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test006() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {blue| 5}"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test007() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {red} and violets are {blue|  5}"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test008() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "1+1={2|   3}"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[1];
		answers[0] = "2";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "3";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "1";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "2.5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test009() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "false"; // textual
		data[3] = "1+1={2|   3}"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[1];
		answers[0] = "2";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "3";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "1";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "2.5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));
	}

	public void test010() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "<p>2*2={4|&nbsp;&nbsp;&nbsp; 5}</p>"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[1];
		answers[0] = "4";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "1";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "4.5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
	}

	public void test011() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "false"; // textual
		data[3] = "<p>2*2={4|&nbsp;&nbsp;&nbsp; 5}</p>"; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[1];
		answers[0] = "4";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "1";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "4.5";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));
	}

	public void test012() throws Exception
	{
		String[] data = new String[4];
		data[0] = "false"; // any order
		data[1] = "false"; // case sensitive
		data[2] = "true"; // textual
		data[3] = "roses are {&nbsp;&nbsp;red&nbsp; &nbsp;} and violets are {&nbsp;&nbsp; blue &nbsp; | &nbsp;  purple&nbsp;&nbsp;&nbsp;}."; // text
		question.getTypeSpecificQuestion().setData(data);

		AnswerImpl answer = new MyAnswerImpl(null);
		answer.answerHandler = fillBlanksPlugin.newAnswer(answer);

		String[] answers = new String[2];
		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "red";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "red";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(5)));

		answers[0] = "redx";
		answers[1] = "bluex";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));

		answers[0] = "red";
		answers[1] = "purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(10)));

		answers[0] = "redx";
		answers[1] = "blue|purple";
		answer.answerHandler.setData(answers);
		assertTrue(answer.answerHandler.getAutoScore().equals(Float.valueOf(0)));
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
		FillBlanksPlugin fb = new FillBlanksPlugin();
		fb.setBundle("mnemeFillBlanks");
		fb.setMnemeService(mnemeService);
		fb.init();
		fillBlanksPlugin = fb;

		// question
		QuestionImpl q = new QuestionImpl();
		q.setQuestionService(questionGetService);
		q.setPoolService(poolGetService);
		q.setSubmissionService(submissionUnscoredQuestionService);
		q.initTypeSpecificQuestion(fb.newQuestion(q));
		q.initType(fb.getType());
		Pool p = this.poolGetService.getPool("1");
		q.setPool(p);

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
