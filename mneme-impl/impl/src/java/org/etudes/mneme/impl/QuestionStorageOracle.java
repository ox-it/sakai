/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Pool;

/**
 * QuestionStorageMysql implements QuestionStorage for Oracle.
 */
public abstract class QuestionStorageOracle extends QuestionStorageSql implements QuestionStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(QuestionStorageOracle.class);

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// if we are auto-creating our schema, check and create
		if (autoDdl)
		{
			this.sqlService.ddl(this.getClass().getClassLoader(), "mneme_question");
		}

		M_log.info("init()");
	}

	/**
	 * Insert a new question as a copy of another question, marked as history (copyPoolQuestions transaction code).
	 * 
	 * @param userId
	 *        The user id.
	 * @param qid
	 *        The source question id.
	 * @param destination
	 *        The pool for the new question.
	 */
	protected String copyQuestionHistoricalTx(String userId, String qid, Pool destination)
	{
		Date now = new Date();

		// get the next id
		Long id = this.sqlService.getNextSequence("MNEME_QUESTION_SEQ", null);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_QUESTION");
		sql.append(" (ID, CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, EXPLAIN_REASON, FEEDBACK,");
		sql.append(" HINTS, HISTORICAL, MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POOL_ID, PRESENTATION_TEXT, PRESENTATION_ATTACHMENTS,");
		sql.append(" SURVEY, TYPE, VALID, GUEST)");
		sql.append(" SELECT " + id + ",");
		sql.append(" '" + destination.getContext() + "', " + now.getTime() + ", '" + userId + "',");
		sql.append(" Q.DESCRIPTION, Q.EXPLAIN_REASON, Q.FEEDBACK, Q.HINTS, '1', Q.MINT,");
		sql.append(" '" + now.getTime() + "', '" + userId + "', " + destination.getId() + ",");
		sql.append(" Q.PRESENTATION_TEXT, Q.PRESENTATION_ATTACHMENTS, Q.SURVEY, Q.TYPE, Q.VALID, Q.GUEST");
		sql.append(" FROM MNEME_QUESTION Q WHERE Q.ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(qid);

		if (!this.sqlService.dbWrite(null, sql.toString(), fields))
		{
			throw new RuntimeException("copyQuestionTx: dbWrite failed");
		}

		return id.toString();
	}

	/**
	 * Insert a new question as a copy of another question (copyPoolQuestions transaction code).
	 * 
	 * @param userId
	 *        The user id.
	 * @param qid
	 *        The source question id.
	 * @param destination
	 *        The pool for the new question.
	 */
	protected String copyQuestionTx(String userId, String qid, Pool destination)
	{
		Date now = new Date();

		// get the next id
		Long id = this.sqlService.getNextSequence("MNEME_QUESTION_SEQ", null);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_QUESTION");
		sql.append(" (ID, CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, EXPLAIN_REASON, FEEDBACK,");
		sql.append(" HINTS, HISTORICAL, MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POOL_ID, PRESENTATION_TEXT, PRESENTATION_ATTACHMENTS,");
		sql.append(" SURVEY, TYPE, VALID, GUEST)");
		sql.append(" SELECT " + id + ",");
		sql.append(" '" + destination.getContext() + "', " + now.getTime() + ", '" + userId + "',");
		sql.append(" Q.DESCRIPTION, Q.EXPLAIN_REASON, Q.FEEDBACK, Q.HINTS, Q.HISTORICAL, Q.MINT,");
		sql.append(" '" + now.getTime() + "', '" + userId + "', " + destination.getId() + ",");
		sql.append(" Q.PRESENTATION_TEXT, Q.PRESENTATION_ATTACHMENTS, Q.SURVEY, Q.TYPE, Q.VALID, Q.GUEST");
		sql.append(" FROM MNEME_QUESTION Q WHERE Q.ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(qid);

		if (!this.sqlService.dbWrite(null, sql.toString(), fields))
		{
			throw new RuntimeException("copyQuestionTx: dbWrite failed");
		}

		return id.toString();
	}

	/**
	 * Insert a new question (transaction code).
	 * 
	 * @param question
	 *        The question.
	 */
	protected void insertQuestionTx(QuestionImpl question)
	{
		// get the next id
		Long id = this.sqlService.getNextSequence("MNEME_QUESTION_SEQ", null);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_QUESTION (ID,");
		sql.append(" CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, EXPLAIN_REASON, FEEDBACK,");
		sql.append(" HINTS, HISTORICAL, MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POOL_ID, PRESENTATION_TEXT, PRESENTATION_ATTACHMENTS,");
		sql.append(" SURVEY, TYPE, VALID, GUEST )");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[19];
		fields[0] = id;
		fields[1] = question.getContext();
		fields[2] = question.getCreatedBy().getDate().getTime();
		fields[3] = question.getCreatedBy().getUserId();
		fields[4] = limit(question.getDescription(), 255);
		fields[5] = question.getExplainReason() ? "1" : "0";
		fields[6] = question.getFeedback();
		fields[7] = question.getHints();
		fields[8] = question.getIsHistorical() ? "1" : "0";
		fields[9] = question.getMint() ? "1" : "0";
		fields[10] = question.getModifiedBy().getDate().getTime();
		fields[11] = question.getModifiedBy().getUserId();
		fields[12] = (question.poolId == null) ? null : Long.valueOf(question.poolId);
		fields[13] = question.getPresentation().getText();
		fields[14] = SqlHelper.encodeReferences(question.getPresentation().getAttachments());
		fields[15] = question.getIsSurvey() ? "1" : "0";
		fields[16] = question.getType();
		fields[17] = question.getIsValid() ? "1" : "0";
		fields[18] = SqlHelper.encodeStringArray(question.getTypeSpecificQuestion().getData());

		if (!this.sqlService.dbWrite(null, sql.toString(), fields))
		{
			throw new RuntimeException("insertQuestionTx: dbWrite failed");
		}

		// set the question's id
		question.initId(id.toString());
	}
}
