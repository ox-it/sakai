/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.QuestionPoolService.FindQuestionsSort;
import org.etudes.util.api.Translation;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.util.StringUtil;

/**
 * QuestionStorageMysql handles storage for questions under SQL databases.
 */
public abstract class QuestionStorageSql implements QuestionStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(QuestionStorageSql.class);

	/** Dependency: AttachmentService */
	protected AttachmentService attachmentService = null;

	/** Configuration: to run the ddl on init or not. */
	protected boolean autoDdl = false;

	/** Dependency: PoolService */
	protected PoolService poolService = null;

	/** Dependency: QuestionService */
	protected QuestionServiceImpl questionService = null;

	/** Dependency: SqlService. */
	protected SqlService sqlService = null;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * {@inheritDoc}
	 */
	public void clearContext(final String context)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				clearContextTx(context);
			}
		}, "clearContext: " + context.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> clearStaleMintQuestions(final Date stale)
	{
		final List<String> rv = new ArrayList<String>();

		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				clearStaleMintQuestionsTx(stale, rv);
			}
		}, "clearStaleMintQuestions: " + stale.toString());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public QuestionImpl clone(QuestionImpl question)
	{
		QuestionImpl rv = new QuestionImpl(question);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> copyPoolQuestions(final String userId, final Pool source, final Pool destination, final boolean asHistory,
			final Map<String, String> oldToNew, final List<Translation> attachmentTranslations, boolean merge, Set<String> includeQuestions)
	{
		final List<String> rv = new ArrayList<String>();

		// if merging, we need to do this internally, rather than in the db
		if (merge || (includeQuestions != null))
		{
			rv.addAll(copyPoolQuestionsInternally(userId, source, destination, asHistory, oldToNew, attachmentTranslations, merge, includeQuestions));
		}

		// otherwise we can use the db transactions
		else
		{
			// get source's question ids
			final List<String> poolQids = source.getAllQuestionIds(null, null);

			this.sqlService.transact(new Runnable()
			{
				public void run()
				{
					for (String qid : poolQids)
					{
						String newId = null;
						if (asHistory)
						{
							newId = copyQuestionHistoricalTx(userId, qid, destination);
							if (oldToNew != null)
							{
								oldToNew.put(qid, newId);
							}
						}
						else
						{
							newId = copyQuestionTx(userId, qid, destination);
							if (oldToNew != null)
							{
								oldToNew.put(qid, newId);
							}
						}

						rv.add(newId);

						// translate attachments
						if (attachmentTranslations != null)
						{
							translateQuestionAttachmentsTx(newId, attachmentTranslations);
						}
					}
				}
			}, "copyPoolQuestions: " + source.getId());
		}
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer countContextQuestions(String context, String questionType, Boolean survey, Boolean valid)
	{
		int extras = 0;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(1) FROM MNEME_QUESTION Q");
		sql.append(" LEFT OUTER JOIN MNEME_POOL P ON Q.POOL_ID=P.ID");
		sql.append(" WHERE Q.MINT='0' AND Q.HISTORICAL='0' AND P.CONTEXT=?");
		if (survey != null)
		{
			sql.append(" AND Q.SURVEY=?");
			extras++;
		}
		else
		{
			sql.append(" AND Q.SURVEY IN ('0','1')");
		}
		if (valid != null)
		{
			sql.append(" AND Q.VALID=?");
			extras++;
		}
		if (questionType != null)
		{
			sql.append(" AND Q.TYPE=?");
			extras++;
		}

		Object[] fields = new Object[1 + extras];
		fields[0] = context;
		int pos = 1;
		if (survey != null)
		{
			fields[pos++] = survey ? "1" : "0";
		}
		if (valid != null)
		{
			fields[pos++] = valid ? "1" : "0";
		}
		if (questionType != null)
		{
			fields[pos++] = questionType;
		}

		List results = this.sqlService.dbRead(sql.toString(), fields, null);
		if (results.size() > 0)
		{
			return Integer.valueOf((String) results.get(0));
		}

		return Integer.valueOf(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool.PoolCounts countPoolQuestions(Pool pool, String questionType, Boolean valid)
	{
		int extras = 0;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT Q.SURVEY, COUNT(1) FROM MNEME_QUESTION Q");
		sql.append(" WHERE Q.MINT='0' AND Q.HISTORICAL IN ('0','1') AND Q.POOL_ID=?");
		if (valid != null)
		{
			sql.append(" AND Q.VALID=?");
			extras++;
		}
		if (questionType != null)
		{
			sql.append(" AND Q.TYPE=?");
			extras++;
		}
		sql.append(" GROUP BY Q.SURVEY");

		Object[] fields = new Object[1 + extras];
		fields[0] = Long.valueOf(pool.getId());
		int pos = 1;
		if (valid != null)
		{
			fields[pos++] = valid ? "1" : "0";
		}
		if (questionType != null)
		{
			fields[pos++] = questionType;
		}

		final Pool.PoolCounts rv = new Pool.PoolCounts();
		rv.assessment = 0;
		rv.survey = 0;
		List results = this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					Boolean survey = SqlHelper.readBoolean(result, 1);
					Integer count = SqlHelper.readInteger(result, 2);
					if (count != null)
					{
						if ((survey != null) && survey)
						{
							rv.survey += count;
						}
						else
						{
							rv.assessment += count;
						}
					}

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("countPoolQuestions: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Pool.PoolCounts> countPoolQuestions(String context, Boolean valid)
	{
		int extras = 0;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.ID, Q.SURVEY, COUNT(Q.ID)");
		sql.append(" FROM MNEME_POOL P");
		sql.append(" LEFT OUTER JOIN MNEME_QUESTION Q");
		sql.append(" ON Q.MINT='0' AND Q.HISTORICAL='0' AND Q.POOL_ID=P.ID");
		if (valid != null)
		{
			sql.append(" AND Q.VALID=?");
			extras++;
		}
		sql.append(" WHERE P.CONTEXT=? AND P.MINT='0' AND P.HISTORICAL='0'");
		sql.append(" GROUP BY P.ID, Q.SURVEY");

		Object[] fields = new Object[1 + extras];
		int pos = 0;
		if (valid != null)
		{
			fields[pos++] = valid ? "1" : "0";
		}
		fields[pos++] = context;

		final Map<String, Pool.PoolCounts> rv = new HashMap<String, Pool.PoolCounts>();
		List results = this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					String id = SqlHelper.readId(result, 1);
					Boolean survey = SqlHelper.readBoolean(result, 2);
					Integer count = SqlHelper.readInteger(result, 3);

					Pool.PoolCounts counts = rv.get(id);
					if (counts == null)
					{
						counts = new Pool.PoolCounts();
						counts.assessment = 0;
						counts.survey = 0;
						rv.put(id, counts);
					}

					if (count != null)
					{
						if ((survey != null) && survey)
						{
							counts.survey += count;
						}
						else
						{
							counts.assessment += count;
						}
					}

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("countPoolQuestions: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean existsQuestion(String id)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(1) FROM MNEME_QUESTION Q");
		sql.append(" WHERE Q.ID=?");
		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(id);
		List results = this.sqlService.dbRead(sql.toString(), fields, null);
		if (results.size() > 0)
		{
			int size = Integer.parseInt((String) results.get(0));
			return Boolean.valueOf(size == 1);
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> findAllNonHistoricalIds()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT Q.ID");
		sql.append(" FROM MNEME_QUESTION Q ");
		sql.append(" WHERE Q.MINT IN ('0', '1') AND Q.HISTORICAL='0'");
		sql.append(" ORDER BY Q.ID ASC");

		final List<String> rv = new ArrayList<String>();
		this.sqlService.dbRead(sql.toString(), null, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					String qid = SqlHelper.readId(result, 1);
					rv.add(qid);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("findAllNonHistoricalIds: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<QuestionImpl> findContextQuestions(String context, QuestionService.FindQuestionsSort sort, String questionType, Integer pageNum,
			Integer pageSize, Boolean survey, Boolean valid)
	{
		// the where and order by
		StringBuilder whereOrder = new StringBuilder();
		whereOrder.append("LEFT OUTER JOIN MNEME_POOL P ON Q.POOL_ID=P.ID WHERE Q.MINT='0' AND Q.HISTORICAL='0' AND P.CONTEXT=?"
				+ ((survey != null) ? " AND Q.SURVEY=?" : " AND Q.SURVEY IN ('0','1')") + ((valid != null) ? " AND Q.VALID=?" : "")
				+ ((questionType != null) ? " AND Q.TYPE=?" : "") + " ORDER BY ");
		whereOrder.append(sortToSql(sort));

		int extras = 0;
		if (questionType != null) extras++;
		if (survey != null) extras++;
		if (valid != null) extras++;

		Object[] fields = new Object[1 + extras];
		fields[0] = context;
		int pos = 1;
		if (survey != null)
		{
			fields[pos++] = survey ? "1" : "0";
		}
		if (valid != null)
		{
			fields[pos++] = valid ? "1" : "0";
		}
		if (questionType != null)
		{
			fields[pos++] = questionType;
		}

		List<QuestionImpl> rv = readQuestions(whereOrder.toString(), fields);

		// TODO: page in the SQL...
		if ((pageNum != null) && (pageSize != null))
		{
			// start at ((pageNum-1)*pageSize)
			int start = ((pageNum - 1) * pageSize);
			if (start < 0) start = 0;
			if (start > rv.size()) start = rv.size() - 1;

			// end at ((pageNum)*pageSize)-1, or max-1, (note: subList is not inclusive for the end position)
			int end = ((pageNum) * pageSize);
			if (end < 0) end = 0;
			if (end > rv.size()) end = rv.size();

			rv = rv.subList(start, end);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<QuestionImpl> findPoolQuestions(Pool pool, QuestionService.FindQuestionsSort sort, String questionType, Integer pageNum,
			Integer pageSize, Boolean survey, Boolean valid)
	{
		// the where and order by
		StringBuilder whereOrder = new StringBuilder();
		whereOrder.append("LEFT OUTER JOIN MNEME_POOL P ON Q.POOL_ID=P.ID WHERE Q.MINT='0' AND Q.HISTORICAL=P.HISTORICAL AND Q.POOL_ID=?"
				+ ((survey != null) ? " AND Q.SURVEY=?" : " AND Q.SURVEY IN ('0','1')") + ((valid != null) ? " AND Q.VALID=?" : "")
				+ ((questionType != null) ? " AND Q.TYPE=?" : "") + " ORDER BY ");
		whereOrder.append(sortToSql(sort));

		int extras = 0;
		if (questionType != null) extras++;
		if (survey != null) extras++;
		if (valid != null) extras++;

		Object[] fields = new Object[1 + extras];
		fields[0] = Long.valueOf(pool.getId());
		int pos = 1;
		if (survey != null)
		{
			fields[pos++] = survey ? "1" : "0";
		}
		if (valid != null)
		{
			fields[pos++] = valid ? "1" : "0";
		}
		if (questionType != null)
		{
			fields[pos++] = questionType;
		}

		List<QuestionImpl> rv = readQuestions(whereOrder.toString(), fields);

		// TODO: page in the SQL...
		if ((pageNum != null) && (pageSize != null))
		{
			// start at ((pageNum-1)*pageSize)
			int start = ((pageNum - 1) * pageSize);
			if (start < 0) start = 0;
			if (start > rv.size()) start = rv.size() - 1;

			// end at ((pageNum)*pageSize)-1, or max-1, (note: subList is not inclusive for the end position)
			int end = ((pageNum) * pageSize);
			if (end < 0) end = 0;
			if (end > rv.size()) end = rv.size();

			rv = rv.subList(start, end);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getPoolQuestions(Pool pool, Boolean survey, Boolean valid)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT Q.ID");
		sql.append(" FROM MNEME_QUESTION Q ");
		sql.append(" WHERE Q.MINT='0' AND Q.HISTORICAL IN ('0','1') AND Q.POOL_ID=?");
		if (survey != null)
		{
			if (survey)
			{
				sql.append(" AND Q.SURVEY='1'");
			}
			else
			{
				sql.append(" AND Q.SURVEY='0'");
			}
		}
		if (valid != null)
		{
			if (valid)
			{
				sql.append(" AND Q.VALID='1'");
			}
			else
			{
				sql.append(" AND Q.VALID='0'");
			}
		}
		sql.append(" ORDER BY Q.ID ASC");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(pool.getId());

		final List<String> rv = new ArrayList<String>();
		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					String qid = SqlHelper.readId(result, 1);
					rv.add(qid);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getPoolQuestions: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public QuestionImpl getQuestion(String id)
	{
		return readQuestion(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveQuestion(final Question question, final Pool pool)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				moveQuestionTx(question, pool);
			}
		}, "moveQuestion: question: " + question.getId() + " pool: " + pool.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public abstract QuestionImpl newQuestion();

	/**
	 * {@inheritDoc}
	 */
	public void removeQuestion(QuestionImpl question)
	{
		deleteQuestion(question);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveQuestion(QuestionImpl question)
	{
		// for new questions
		if (question.getId() == null)
		{
			insertQuestion(question);
		}

		// for existing questions
		else
		{
			updateQuestion(question);
		}
	}

	/**
	 * Dependency: AttachmentService.
	 * 
	 * @param service
	 *        The AttachmentService.
	 */
	public void setAttachmentService(AttachmentService service)
	{
		attachmentService = service;
	}

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		autoDdl = new Boolean(value).booleanValue();
	}

	/**
	 * Dependency: PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setPoolService(PoolService service)
	{
		this.poolService = service;
	}

	/**
	 * Dependency: QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionServiceImpl service)
	{
		this.questionService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSqlService(SqlService service)
	{
		this.sqlService = service;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		threadLocalManager = service;
	}

	/**
	 * Transaction code for clearContext()
	 */
	protected void clearContextTx(String context)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_QUESTION");
		sql.append(" WHERE CONTEXT=?");

		Object[] fields = new Object[1];
		fields[0] = context;

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("clearContext: dbWrite failed");
		}
	}

	/**
	 * Transaction code for clearStaleMintQuestions()
	 */
	protected void clearStaleMintQuestionsTx(Date stale, List<String> ids)
	{
		StringBuilder sql = new StringBuilder();

		Object[] fields = new Object[1];
		fields[0] = stale.getTime();

		sql.append("SELECT ID FROM MNEME_QUESTION");
		sql.append(" WHERE MINT='1' AND CREATED_BY_DATE < ?");
		List<String> rv = this.sqlService.dbRead(sql.toString(), fields, null);
		ids.addAll(rv);

		sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_QUESTION");
		sql.append(" WHERE MINT='1' AND CREATED_BY_DATE < ?");

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("clearStaleMintQuestionsTx: db write failed");
		}
	}

	/**
	 * Create a new question that is a copy of each question in the pool, supporting merges
	 * 
	 * @param userId
	 *        The user to own the questions.
	 * @param source
	 *        The pool of questions to copy.
	 * @param destination
	 *        the pool where the question will live.
	 * @param asHistory
	 *        If set, make the questions historical.
	 * @param oldToNew
	 *        A map, which, if present, will be filled in with the mapping of the source question id to the destination question id for each question copied.
	 * @param attachmentTranslations
	 *        A list of Translations for attachments and embedded media.
	 * @param merge
	 *        if true, if there is question already in the pool that matches one to be copied, don't copy it and create a new question.
	 * @param includeQuestions
	 *        if not null, only import the pool's question if its id is in the set.
	 * @return A List of the ids of the new questions created.
	 */
	protected List<String> copyPoolQuestionsInternally(String userId, Pool source, Pool destination, boolean asHistory, Map<String, String> oldToNew,
			List<Translation> attachmentTranslations, boolean merge, Set<String> includeQuestions)
	{
		List<String> rv = new ArrayList<String>();

		List<QuestionImpl> questions = findPoolQuestions(source, QuestionService.FindQuestionsSort.cdate_a, null, null, null, null, null);
		for (QuestionImpl question : questions)
		{
			// skip if we are being selective and don't want this one
			if ((includeQuestions != null) && (!includeQuestions.contains(question.getId()))) continue;

			QuestionImpl q = new QuestionImpl(question);

			// set the destination as the pool
			q.setPool(destination);

			// clear the id to make it new
			q.id = null;

			Date now = new Date();

			// set the new created and modified info
			q.getCreatedBy().setUserId(userId);
			q.getCreatedBy().setDate(now);
			q.getModifiedBy().setUserId(userId);
			q.getModifiedBy().setDate(now);

			if (asHistory) q.makeHistorical();

			// translate attachments and embedded media using attachmentTranslations
			if (attachmentTranslations != null)
			{
				q.getPresentation()
						.setText(this.attachmentService.translateEmbeddedReferences(q.getPresentation().getText(), attachmentTranslations));
				List<Reference> attachments = q.getPresentation().getAttachments();
				List<Reference> newAttachments = new ArrayList<Reference>();
				for (Reference ref : attachments)
				{
					String newRef = ref.getReference();
					for (Translation t : attachmentTranslations)
					{
						newRef = t.translate(newRef);
					}
					newAttachments.add(this.attachmentService.getReference(newRef));
				}
				q.getPresentation().setAttachments(newAttachments);
				q.setFeedback(this.attachmentService.translateEmbeddedReferences(q.getFeedback(), attachmentTranslations));
				q.setHints(this.attachmentService.translateEmbeddedReferences(q.getHints(), attachmentTranslations));

				String[] data = q.getTypeSpecificQuestion().getData();
				for (int i = 0; i < data.length; i++)
				{
					data[i] = this.attachmentService.translateEmbeddedReferences(data[i], attachmentTranslations);
				}
				q.getTypeSpecificQuestion().setData(data);
			}

			// if merging, if there is a question in the pool that "matches" this one, use it and skip the import
			boolean skipping = false;
			if (merge)
			{
				List<QuestionImpl> existingQuestions = findPoolQuestions(destination, FindQuestionsSort.cdate_a, q.getType(), null, null, null, null);
				for (Question candidate : existingQuestions)
				{
					if (candidate.matches(q))
					{
						// will map references to this question.getId() , artifact.getProperties().get("id");
						if (oldToNew != null)
						{
							oldToNew.put(question.getId(), candidate.getId());
						}

						// return without saving the new question - it will stay mint and be cleared
						skipping = true;

						rv.add(candidate.getId());
					}
				}
			}

			// save
			if (!skipping)
			{
				saveQuestion(q);

				rv.add(q.getId());

				if (oldToNew != null)
				{
					oldToNew.put(question.getId(), q.getId());
				}
			}
		}

		return rv;
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
	protected abstract String copyQuestionHistoricalTx(String userId, String qid, Pool destination);

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
	protected abstract String copyQuestionTx(String userId, String qid, Pool destination);

	/**
	 * Delete a question.
	 * 
	 * @param question
	 *        The question.
	 */
	protected void deleteQuestion(final QuestionImpl question)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				deleteQuestionTx(question);
			}
		}, "deleteQuestion: " + question.getId());
	}

	/**
	 * Delete a question (transaction code).
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void deleteQuestionTx(QuestionImpl question)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM MNEME_QUESTION");
		sql.append(" WHERE ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(question.getId());

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("deleteQuestionTx: db write failed");
		}
	}

	/**
	 * Insert a new question.
	 * 
	 * @param question
	 *        The question.
	 */
	protected void insertQuestion(final QuestionImpl question)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				insertQuestionTx(question);
			}
		}, "insertQuestion: " + question.getId());
	}

	/**
	 * Insert a new question (transaction code).
	 * 
	 * @param question
	 *        The question.
	 */
	protected abstract void insertQuestionTx(QuestionImpl question);

	/**
	 * If the string is non-nul and longer than length, return a trimmed version, else return it.
	 * 
	 * @param value
	 *        The value to work on.
	 * @param length
	 *        The maximum length.
	 * @return The value trimmed to the maximum length, or unchanged if null or shorter than that maximum.
	 */
	protected String limit(String value, int length)
	{
		if (value == null) return null;
		if (value.length() > length)
		{
			return value.substring(0, length);
		}
		return value;
	}

	/**
	 * Transaction code for moveQuestion()
	 */
	protected void moveQuestionTx(Question question, Pool pool)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE MNEME_QUESTION SET");
		sql.append(" POOL_ID=?");
		sql.append(" WHERE ID=?");

		Object[] fields = new Object[2];
		fields[0] = Long.valueOf(pool.getId());
		fields[1] = Long.valueOf(question.getId());

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("moveQuestionTx: db write failed");
		}
	}

	/**
	 * Read a question
	 * 
	 * @param id
	 *        The question id.
	 * @return The question.
	 */
	protected QuestionImpl readQuestion(String id)
	{
		String whereOrder = "WHERE Q.ID = ?";
		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(id);
		List<QuestionImpl> rv = readQuestions(whereOrder, fields);
		if (rv.size() > 0)
		{
			return rv.get(0);
		}

		return null;
	}

	/**
	 * Read a selection of questions.
	 * 
	 * @param whereOrder
	 *        The WHERE and ORDER BY sql clauses
	 * @param fields
	 *        The bind variables.
	 * @return The questions.
	 */
	protected List<QuestionImpl> readQuestions(String whereOrder, Object[] fields)
	{
		final List<QuestionImpl> rv = new ArrayList<QuestionImpl>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT Q.CONTEXT, Q.CREATED_BY_DATE, Q.CREATED_BY_USER, Q.EXPLAIN_REASON, Q.FEEDBACK,");
		sql.append(" Q.HINTS, Q.HISTORICAL, Q.ID, Q.MINT, Q.MODIFIED_BY_DATE, Q.MODIFIED_BY_USER, Q.POOL_ID,");
		sql.append(" Q.PRESENTATION_TEXT, Q.PRESENTATION_ATTACHMENTS, Q.SURVEY, Q.TYPE, Q.GUEST");
		sql.append(" FROM MNEME_QUESTION Q ");
		sql.append(whereOrder);

		final QuestionServiceImpl qService = this.questionService;
		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					QuestionImpl question = newQuestion();
					question.initContext(SqlHelper.readString(result, 1));
					question.getCreatedBy().setDate(SqlHelper.readDate(result, 2));
					question.getCreatedBy().setUserId(SqlHelper.readString(result, 3));
					question.setExplainReason(SqlHelper.readBoolean(result, 4));
					question.setFeedback(SqlHelper.readString(result, 5));
					question.setHints(SqlHelper.readString(result, 6));
					question.initHistorical(SqlHelper.readBoolean(result, 7));
					question.initId(SqlHelper.readId(result, 8));
					question.initMint(SqlHelper.readBoolean(result, 9));
					question.getModifiedBy().setDate(SqlHelper.readDate(result, 10));
					question.getModifiedBy().setUserId(SqlHelper.readString(result, 11));
					question.initPool(SqlHelper.readId(result, 12));
					question.getPresentation().setText(SqlHelper.readString(result, 13));
					question.getPresentation().setAttachments(SqlHelper.readReferences(result, 14, attachmentService));
					question.setIsSurvey(SqlHelper.readBoolean(result, 15));
					qService.setType(SqlHelper.readString(result, 16), question);
					question.getTypeSpecificQuestion().setData(SqlHelper.decodeStringArray(StringUtil.trimToNull(result.getString(17))));

					question.clearChanged();
					rv.add(question);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("readQuestions: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * Convert a FindQuestionsSort to a sql sort clause
	 * 
	 * @param sort
	 *        The sort.
	 * @return The SQL.
	 */
	protected String sortToSql(QuestionService.FindQuestionsSort sort)
	{
		switch (sort)
		{
			case type_a:
			{
				// TODO: localized
				return "Q.TYPE ASC, Q.DESCRIPTION ASC, Q.CREATED_BY_DATE ASC";
			}
			case type_d:
			{
				// TODO: localized
				return "Q.TYPE DESC, Q.DESCRIPTION DESC, Q.CREATED_BY_DATE DESC";
			}
			case description_a:
			{
				return "Q.DESCRIPTION ASC, Q.CREATED_BY_DATE ASC";
			}
			case description_d:
			{
				return "Q.DESCRIPTION DESC, Q.CREATED_BY_DATE DESC";
			}
			case pool_difficulty_a:
			{
				return "P.DIFFICULTY ASC, Q.DESCRIPTION ASC, Q.CREATED_BY_DATE ASC";
			}
			case pool_difficulty_d:
			{
				return "P.DIFFICULTY DESC, Q.DESCRIPTION DESC, Q.CREATED_BY_DATE DESC";
			}
			case pool_points_a:
			{
				return "P.POINTS ASC, Q.DESCRIPTION ASC, Q.CREATED_BY_DATE ASC";
			}
			case pool_points_d:
			{
				return "P.POINTS DESC, Q.DESCRIPTION DESC, Q.CREATED_BY_DATE DESC";
			}
			case pool_title_a:
			{
				return "P.TITLE ASC, Q.DESCRIPTION ASC, Q.CREATED_BY_DATE ASC";
			}
			case pool_title_d:
			{
				return "P.TITLE DESC, Q.DESCRIPTION DESC, Q.CREATED_BY_DATE DESC";
			}
			case cdate_a:
			{
				return "Q.CREATED_BY_DATE ASC";
			}
			case cdate_d:
			{
				return "Q.CREATED_BY_DATE DESC";
			}
		}
		return "";
	}

	/**
	 * Translate any embedded attachments in the question presentation text or guest area
	 * 
	 * @param qid
	 *        The question id.
	 * @param attachmentTranslations
	 *        The translations.
	 */
	protected void translateQuestionAttachmentsTx(String qid, List<Translation> attachmentTranslations)
	{
		// read the question's text
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT Q.PRESENTATION_TEXT, Q.GUEST, Q.HINTS, Q.FEEDBACK, Q.PRESENTATION_ATTACHMENTS");
		sql.append(" FROM MNEME_QUESTION Q ");
		sql.append(" WHERE Q.ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(qid);

		final Object[] fields2 = new Object[6];
		fields2[5] = fields[0];

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					fields2[0] = SqlHelper.readString(result, 1);
					fields2[1] = SqlHelper.decodeStringArray(StringUtil.trimToNull(result.getString(2)));
					fields2[2] = SqlHelper.readString(result, 3);
					fields2[3] = SqlHelper.readString(result, 4);
					fields2[4] = SqlHelper.decodeStringArray(StringUtil.trimToNull(result.getString(5)));

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("translateQuestionAttachmentsTx(read): " + e);
					return null;
				}
			}
		});

		// translate
		fields2[0] = this.attachmentService.translateEmbeddedReferences((String) fields2[0], attachmentTranslations);
		fields2[2] = this.attachmentService.translateEmbeddedReferences((String) fields2[2], attachmentTranslations);
		fields2[3] = this.attachmentService.translateEmbeddedReferences((String) fields2[3], attachmentTranslations);
		for (int i = 0; i < ((String[]) fields2[1]).length; i++)
		{
			((String[]) fields2[1])[i] = this.attachmentService.translateEmbeddedReferences(((String[]) fields2[1])[i], attachmentTranslations);
		}
		for (int i = 0; i < ((String[]) fields2[4]).length; i++)
		{
			((String[]) fields2[4])[i] = this.attachmentService.translateEmbeddedReferences(((String[]) fields2[4])[i], attachmentTranslations);
		}

		fields2[1] = SqlHelper.encodeStringArray(((String[]) fields2[1]));
		fields2[4] = SqlHelper.encodeStringArray(((String[]) fields2[4]));

		// update
		sql = new StringBuilder();
		sql.append("UPDATE MNEME_QUESTION SET PRESENTATION_TEXT=?, GUEST=?, HINTS=?, FEEDBACK=?, PRESENTATION_ATTACHMENTS=? WHERE ID=?");
		if (!this.sqlService.dbWrite(sql.toString(), fields2))
		{
			throw new RuntimeException("translateQuestionAttachmentsTx(write): db write failed");
		}
	}

	/**
	 * Update an existing pool.
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void updateQuestion(final QuestionImpl question)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				updateQuestionTx(question);
			}
		}, "updateQuestion: " + question.getId());
	}

	/**
	 * Update an existing pool (transaction code).
	 * 
	 * @param question
	 *        The pool.
	 */
	protected void updateQuestionTx(QuestionImpl question)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE MNEME_QUESTION SET");
		sql.append(" CONTEXT=?, DESCRIPTION=?, EXPLAIN_REASON=?, FEEDBACK=?, HINTS=?, HISTORICAL=?,");
		sql.append(" MINT=?, MODIFIED_BY_DATE=?, MODIFIED_BY_USER=?, POOL_ID=?,");
		sql.append(" PRESENTATION_TEXT=?, PRESENTATION_ATTACHMENTS=?, SURVEY=?, VALID=?, GUEST=?, TYPE=?");
		sql.append(" WHERE ID=?");

		Object[] fields = new Object[17];
		fields[0] = question.getContext();
		fields[1] = limit(question.getDescription(), 255);
		fields[2] = question.getExplainReason() ? "1" : "0";
		fields[3] = question.getFeedback();
		fields[4] = question.getHints();
		fields[5] = question.getIsHistorical() ? "1" : "0";
		fields[6] = question.getMint() ? "1" : "0";
		fields[7] = question.getModifiedBy().getDate().getTime();
		fields[8] = question.getModifiedBy().getUserId();
		fields[9] = (question.poolId == null) ? null : Long.valueOf(question.poolId);
		fields[10] = question.getPresentation().getText();
		fields[11] = SqlHelper.encodeReferences(question.getPresentation().getAttachments());
		fields[12] = question.getIsSurvey() ? "1" : "0";
		fields[13] = question.getIsValid() ? "1" : "0";
		fields[14] = SqlHelper.encodeStringArray(question.getTypeSpecificQuestion().getData());
		fields[15] = question.getType();
		fields[16] = Long.valueOf(question.getId());

		if (!this.sqlService.dbWrite(sql.toString(), fields))
		{
			throw new RuntimeException("updateQuestionTx: db write failed");
		}
	}
}
