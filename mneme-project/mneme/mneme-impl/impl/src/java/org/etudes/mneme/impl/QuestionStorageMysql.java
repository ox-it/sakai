/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2015 Etudes, Inc.
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
 * QuestionStorageMysql implements QuestionStorage for MySQL.
 */
public abstract class QuestionStorageMysql extends QuestionStorageSql implements QuestionStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(QuestionStorageMysql.class);

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// if we are auto-creating our schema, check and create
		if (autoDdl)
		{
			this.sqlService.ddl(this.getClass().getClassLoader(), "mneme_question");
			this.sqlService.ddl(this.getClass().getClassLoader(), "mneme_question_title");
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

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_QUESTION");
		sql.append(" (CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, EXPLAIN_REASON, FEEDBACK,");
		sql.append(" HINTS, HISTORICAL, MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POOL_ID, PRESENTATION_TEXT, PRESENTATION_ATTACHMENTS,");
		sql.append(" SURVEY, TYPE, VALID, GUEST)");
		sql.append(" SELECT");
		sql.append(" '" + destination.getContext() + "', " + now.getTime() + ", '" + userId + "',");
		sql.append(" Q.DESCRIPTION, Q.EXPLAIN_REASON, Q.FEEDBACK, Q.HINTS, '1', Q.MINT,");
		sql.append(" '" + now.getTime() + "', '" + userId + "', " + destination.getId() + ",");
		sql.append(" Q.PRESENTATION_TEXT, Q.PRESENTATION_ATTACHMENTS, Q.SURVEY, Q.TYPE, Q.VALID, Q.GUEST");
		sql.append(" FROM MNEME_QUESTION Q WHERE Q.ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(qid);

		Long id = this.sqlService.dbInsert(null, sql.toString(), fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("copyQuestionTx QUESTION table : db write failed");
		}
		else
		{
			StringBuilder sqlTitle = new StringBuilder();
			sqlTitle.append("INSERT INTO MNEME_QUESTION_TITLE");
			sqlTitle.append(" (QUESTION_ID, TITLE)");
			sqlTitle.append(" SELECT");
			sqlTitle.append(" " + id.longValue() + ", ");
			sqlTitle.append(" QT.TITLE ");
			sqlTitle.append(" FROM MNEME_QUESTION_TITLE QT WHERE QT.QUESTION_ID=?");

			Long titleId = this.sqlService.dbInsert(null, sqlTitle.toString(), fields, "QUESTION_ID");
			if (titleId == null)
			{
				//throw new RuntimeException("copyQuestionTx QUESTION_TITLE table : db write failed");
			}
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

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_QUESTION");
		sql.append(" (CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, EXPLAIN_REASON, FEEDBACK,");
		sql.append(" HINTS, HISTORICAL, MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POOL_ID, PRESENTATION_TEXT, PRESENTATION_ATTACHMENTS,");
		sql.append(" SURVEY, TYPE, VALID, GUEST)");
		sql.append(" SELECT");
		sql.append(" '" + destination.getContext() + "', " + now.getTime() + ", '" + userId + "',");
		sql.append(" Q.DESCRIPTION, Q.EXPLAIN_REASON, Q.FEEDBACK, Q.HINTS, Q.HISTORICAL, Q.MINT,");
		sql.append(" '" + now.getTime() + "', '" + userId + "', " + destination.getId() + ",");
		sql.append(" Q.PRESENTATION_TEXT, Q.PRESENTATION_ATTACHMENTS, Q.SURVEY, Q.TYPE, Q.VALID, Q.GUEST");
		sql.append(" FROM MNEME_QUESTION Q WHERE Q.ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(qid);

		Long id = this.sqlService.dbInsert(null, sql.toString(), fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("copyQuestionTx: db write failed");
		}
		else
		{
			StringBuilder sqlTitle = new StringBuilder();
			sqlTitle.append("INSERT INTO MNEME_QUESTION_TITLE");
			sqlTitle.append(" (QUESTION_ID, TITLE)");
			sqlTitle.append(" SELECT");
			sqlTitle.append(" " + id.longValue() + ", ");
			sqlTitle.append(" QT.TITLE ");
			sqlTitle.append(" FROM MNEME_QUESTION_TITLE QT WHERE QT.QUESTION_ID=?");

			Long titleId = this.sqlService.dbInsert(null, sqlTitle.toString(), fields, "QUESTION_ID");
			if (titleId == null)
			{
				//throw new RuntimeException("copyQuestionTx QUESTION_TITLE table : db write failed");
			}
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
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_QUESTION (");
		sql.append(" CONTEXT, CREATED_BY_DATE, CREATED_BY_USER, DESCRIPTION, EXPLAIN_REASON, FEEDBACK,");
		sql.append(" HINTS, HISTORICAL, MINT, MODIFIED_BY_DATE, MODIFIED_BY_USER, POOL_ID, PRESENTATION_TEXT, PRESENTATION_ATTACHMENTS,");
		sql.append(" SURVEY, TYPE, VALID, GUEST )");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[18];
		fields[0] = question.getContext();
		fields[1] = question.getCreatedBy().getDate().getTime();
		fields[2] = question.getCreatedBy().getUserId();
		fields[3] = limit(question.getDescription(), 255);
		fields[4] = question.getExplainReason() ? "1" : "0";
		fields[5] = question.getFeedback();
		fields[6] = question.getHints();
		fields[7] = question.getIsHistorical() ? "1" : "0";
		fields[8] = question.getMint() ? "1" : "0";
		fields[9] = question.getModifiedBy().getDate().getTime();
		fields[10] = question.getModifiedBy().getUserId();
		fields[11] = (question.poolId == null) ? null : Long.valueOf(question.poolId);
		fields[12] = question.getPresentation().getText();
		fields[13] = SqlHelper.encodeReferences(question.getPresentation().getAttachments());
		fields[14] = question.getIsSurvey() ? "1" : "0";
		fields[15] = question.getType();
		fields[16] = question.getIsValid() ? "1" : "0";
		fields[17] = SqlHelper.encodeStringArray(question.getTypeSpecificQuestion().getData());

		Long id = this.sqlService.dbInsert(null, sql.toString(), fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("insertQuestionTx: db write failed");
		}
		else
		{
			if (question.getTitle() != null && question.getTitle().trim().length() > 0)
			{
				StringBuilder sqlTitle = new StringBuilder();
				sqlTitle.append("INSERT INTO MNEME_QUESTION_TITLE");
				sqlTitle.append(" (QUESTION_ID, TITLE)");
				sqlTitle.append(" VALUES(?,?)");

				Object[] fieldsTitle = new Object[2];
				fieldsTitle[0] = Long.valueOf(id);
				fieldsTitle[1] = question.getTitle();

				if (!this.sqlService.dbWrite(null, sqlTitle.toString(), fieldsTitle))
				{
					throw new RuntimeException("insertQuestionTx QUESTION_TITLE table : db write failed");
				}
			}
		}


		// set the question's id
		question.initId(id.toString());
	}
}
