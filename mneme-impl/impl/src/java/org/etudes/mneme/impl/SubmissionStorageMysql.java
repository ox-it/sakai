/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Question;

/**
 * SubmissionStorageMysql implements SubmissionStorage for MySQL.
 */
public abstract class SubmissionStorageMysql extends SubmissionStorageSql implements SubmissionStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SubmissionStorageMysql.class);

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// if we are auto-creating our schema, check and create
		if (autoDdl)
		{
			this.sqlService.ddl(this.getClass().getClassLoader(), "mneme_submission");
		}

		M_log.info("init()");
	}

	/**
	 * Insert a new pool (transaction code).
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void insertAnswerTx(AnswerImpl answer)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_ANSWER (");
		sql.append(" ANSWERED, AUTO_SCORE, GUEST, EVAL_ATRIB_DATE, EVAL_ATRIB_USER, EVAL_ATTACHMENTS, EVAL_COMMENT, EVAL_EVALUATED, EVAL_SCORE,");
		sql.append(" PART_ID, QUESTION_ID, QUESTION_TYPE, REASON, REVIEW, SUBMISSION_ID, SUBMITTED_DATE)");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[16];
		fields[0] = answer.getIsAnswered();
		fields[1] = answer.getAutoScore();
		fields[2] = SqlHelper.encodeStringArray(answer.getTypeSpecificAnswer().getData());
		fields[3] = (answer.getEvaluation().getAttribution().getDate() == null) ? null : answer.getEvaluation().getAttribution().getDate().getTime();
		fields[4] = answer.getEvaluation().getAttribution().getUserId();
		fields[5] = SqlHelper.encodeReferences(answer.getEvaluation().getAttachments());
		fields[6] = answer.getEvaluation().getComment();
		fields[7] = answer.getEvaluation().getEvaluated() ? "1" : "0";
		fields[8] = answer.getEvaluation().getScore() == null ? null : Float.valueOf(answer.getEvaluation().getScore());
		fields[9] = Long.valueOf(answer.getPartId());
		Question q = answer.getQuestion();
		fields[10] = Long.valueOf(q.getId());
		fields[11] = q.getType();
		fields[12] = answer.getReason();
		fields[13] = answer.getMarkedForReview() ? "1" : "0";
		fields[14] = Long.valueOf(answer.getSubmission().getId());
		fields[15] = (answer.getSubmittedDate() == null) ? null : answer.getSubmittedDate().getTime();

		Long id = this.sqlService.dbInsert(null, sql.toString(), fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("insertPoolTx: dbInsert failed");
		}

		// set the answer's id
		answer.initId(id.toString());
	}

	/**
	 * Insert a new submission (transaction code).
	 * 
	 * @param submission
	 *        The submission.
	 */
	protected void insertSubmissionTx(SubmissionImpl submission)
	{
		// new submissions have no answers yet

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_SUBMISSION (");
		sql.append(" ASSESSMENT_ID, COMPLETE, CONTEXT, EVAL_ATRIB_DATE, EVAL_ATRIB_USER, EVAL_ATTACHMENTS,");
		sql.append(" EVAL_COMMENT, EVAL_EVALUATED, EVAL_SCORE, RELEASED, REVIEWED_DATE, START_DATE, SUBMITTED_DATE, TEST_DRIVE, USERID)");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[15];
		fields[0] = Long.valueOf(submission.getAssessment().getId());
		fields[1] = submission.getIsComplete() ? "1" : "0";
		fields[2] = submission.getAssessment().getContext();
		fields[3] = (submission.getEvaluation().getAttribution().getDate() == null) ? null : submission.getEvaluation().getAttribution().getDate()
				.getTime();
		fields[4] = submission.getEvaluation().getAttribution().getUserId();
		fields[5] = SqlHelper.encodeReferences(submission.getEvaluation().getAttachments());
		fields[6] = submission.getEvaluation().getComment();
		fields[7] = submission.getEvaluation().getEvaluated() ? "1" : "0";
		fields[8] = submission.getEvaluation().getScore() == null ? null : Float.valueOf(submission.getEvaluation().getScore());
		fields[9] = submission.getIsReleased() ? "1" : "0";
		fields[10] = (submission.getReviewedDate() == null) ? null : submission.getReviewedDate().getTime();
		fields[11] = (submission.getStartDate() == null) ? null : submission.getStartDate().getTime();
		fields[12] = (submission.getSubmittedDate() == null) ? null : submission.getSubmittedDate().getTime();
		fields[13] = submission.getIsTestDrive() ? "1" : "0";
		fields[14] = submission.getUserId();

		Long id = this.sqlService.dbInsert(null, sql.toString(), fields, "ID");
		if (id == null)
		{
			throw new RuntimeException("insertSubmissionTx: dbInsert failed");
		}

		// set the submission's id
		submission.initId(id.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	protected void removeTestDriveSubmissionsTx(Assessment assessment)
	{
		// answer
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_ANSWER");
		sql.append(" USING MNEME_ANSWER, MNEME_SUBMISSION");
		sql.append(" WHERE MNEME_ANSWER.SUBMISSION_ID=MNEME_SUBMISSION.ID AND MNEME_SUBMISSION.ASSESSMENT_ID=? AND MNEME_SUBMISSION.TEST_DRIVE='1'");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(assessment.getId());

		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
		{
			throw new RuntimeException("removeTestDriveSubmissions(answer): db write failed");
		}

		// submission
		sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_SUBMISSION");
		sql.append(" WHERE MNEME_SUBMISSION.ASSESSMENT_ID=? AND MNEME_SUBMISSION.TEST_DRIVE='1'");

		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
		{
			throw new RuntimeException("removeTestDriveSubmissions(submission): db write failed");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void removeTestDriveSubmissionsTx(String context)
	{
		// answer
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_ANSWER");
		sql.append(" USING MNEME_ANSWER, MNEME_SUBMISSION");
		sql.append(" WHERE MNEME_ANSWER.SUBMISSION_ID=MNEME_SUBMISSION.ID AND MNEME_SUBMISSION.CONTEXT=? AND MNEME_SUBMISSION.TEST_DRIVE='1'");

		Object[] fields = new Object[1];
		fields[0] = context;

		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
		{
			throw new RuntimeException("removeTestDriveSubmissions(context,answer): db write failed");
		}

		// submission
		sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_SUBMISSION");
		sql.append(" WHERE MNEME_SUBMISSION.CONTEXT=? AND MNEME_SUBMISSION.TEST_DRIVE='1'");

		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
		{
			throw new RuntimeException("removeTestDriveSubmissions(context,submission): db write failed");
		}
	}
}
