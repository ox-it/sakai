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
 * SubmissionStorageMysql implements SubmissionStorage for Oracle.
 */
public abstract class SubmissionStorageOracle extends SubmissionStorageSql implements SubmissionStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SubmissionStorageOracle.class);

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// if we are auto-creating our schema, check and create
		if (autoDdl && this.sqlService.getVendor().equals("oracle"))
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
		// get the next id
		Long id = this.sqlService.getNextSequence("MNEME_ANSWER_SEQ", null);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_ANSWER (ID, ");
		sql.append(" ANSWERED, AUTO_SCORE, GUEST, EVAL_ATRIB_DATE, EVAL_ATRIB_USER, EVAL_ATTACHMENTS, EVAL_COMMENT, EVAL_EVALUATED, EVAL_SCORE,");
		sql.append(" PART_ID, QUESTION_ID, QUESTION_TYPE, REASON, REVIEW, SUBMISSION_ID, SUBMITTED_DATE)");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[17];
		fields[0] = id;
		fields[1] = answer.getIsAnswered();
		fields[2] = answer.getAutoScore();
		fields[3] = SqlHelper.encodeStringArray(answer.getTypeSpecificAnswer().getData());
		fields[4] = (answer.getEvaluation().getAttribution().getDate() == null) ? null : answer.getEvaluation().getAttribution().getDate().getTime();
		fields[5] = answer.getEvaluation().getAttribution().getUserId();
		fields[6] = SqlHelper.encodeReferences(answer.getEvaluation().getAttachments());
		fields[7] = answer.getEvaluation().getComment();
		fields[8] = answer.getEvaluation().getEvaluated() ? "1" : "0";
		fields[9] = answer.getEvaluation().getScore() == null ? null : Float.valueOf(answer.getEvaluation().getScore());
		fields[10] = Long.valueOf(answer.getPartId());
		Question q = answer.getQuestion();
		fields[11] = Long.valueOf(q.getId());
		fields[12] = q.getType();
		fields[13] = answer.getReason();
		fields[14] = answer.getMarkedForReview() ? "1" : "0";
		fields[15] = Long.valueOf(answer.getSubmission().getId());
		fields[16] = (answer.getSubmittedDate() == null) ? null : answer.getSubmittedDate().getTime();

		if (!this.sqlService.dbWrite(null, sql.toString(), fields))
		{
			throw new RuntimeException("insertPoolTx: dbWrite failed");
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

		// get the next id
		Long id = this.sqlService.getNextSequence("MNEME_SUBMISSION_SEQ", null);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MNEME_SUBMISSION (ID,");
		sql.append(" ASSESSMENT_ID, COMPLETE, CONTEXT, EVAL_ATRIB_DATE, EVAL_ATRIB_USER, EVAL_ATTACHMENTS,");
		sql.append(" EVAL_COMMENT, EVAL_EVALUATED, EVAL_SCORE, RELEASED, REVIEWED_DATE, START_DATE, SUBMITTED_DATE, TEST_DRIVE, USERID)");
		sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		Object[] fields = new Object[16];
		fields[0] = id;
		fields[1] = Long.valueOf(submission.getAssessment().getId());
		fields[2] = submission.getIsComplete() ? "1" : "0";
		fields[3] = submission.getAssessment().getContext();
		fields[4] = (submission.getEvaluation().getAttribution().getDate() == null) ? null : submission.getEvaluation().getAttribution().getDate()
				.getTime();
		fields[5] = submission.getEvaluation().getAttribution().getUserId();
		fields[6] = SqlHelper.encodeReferences(submission.getEvaluation().getAttachments());
		fields[7] = submission.getEvaluation().getComment();
		fields[8] = submission.getEvaluation().getEvaluated() ? "1" : "0";
		fields[9] = submission.getEvaluation().getScore() == null ? null : Float.valueOf(submission.getEvaluation().getScore());
		fields[10] = submission.getIsReleased() ? "1" : "0";
		fields[11] = (submission.getReviewedDate() == null) ? null : submission.getReviewedDate().getTime();
		fields[12] = (submission.getStartDate() == null) ? null : submission.getStartDate().getTime();
		fields[13] = (submission.getSubmittedDate() == null) ? null : submission.getSubmittedDate().getTime();
		fields[14] = submission.getIsTestDrive() ? "1" : "0";
		fields[15] = submission.getUserId();

		if (!this.sqlService.dbWrite(null, sql.toString(), fields))
		{
			throw new RuntimeException("insertSubmissionTx: dbWrite failed");
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
		sql.append(" WHERE SUBMISSION_ID IN");
		sql.append(" (SELECT ID FROM MNEME_SUBMISSION WHERE ASSESSMENT_ID=? AND TEST_DRIVE='1')");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(assessment.getId());

		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
		{
			throw new RuntimeException("removeTestDriveSubmissions(answer): db write failed");
		}

		// submission
		sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_SUBMISSION");
		sql.append(" WHERE ASSESSMENT_ID=? AND TEST_DRIVE='1'");

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
		sql.append(" WHERE SUBMISSION_ID IN");
		sql.append(" (SELECT ID FROM MNEME_SUBMISSION WHERE CONTEXT=? AND TEST_DRIVE='1')");

		Object[] fields = new Object[1];
		fields[0] = context;

		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
		{
			throw new RuntimeException("removeTestDriveSubmissions(context,answer): db write failed");
		}

		// submission
		sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_SUBMISSION");
		sql.append(" WHERE CONTEXT=? AND TEST_DRIVE='1'");

		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
		{
			throw new RuntimeException("removeTestDriveSubmissions(context,submission): db write failed");
		}
	}
}
