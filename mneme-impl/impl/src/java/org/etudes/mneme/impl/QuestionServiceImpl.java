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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.etudes.util.api.Translation;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * QuestionServiceImpl implements QuestionService
 * </p>
 */
public class QuestionServiceImpl implements QuestionService
{
	public class QuestionCountsContext
	{
		Map<String, Integer> map;

		public QuestionCountsContext()
		{
			this.map = new HashMap<String, Integer>();
		}
	}

	public class QuestionCountsPool
	{
		Map<String, Pool.PoolCounts> map;

		public QuestionCountsPool()
		{
			this.map = new HashMap<String, Pool.PoolCounts>();
		}
	}

	public class Questions
	{
		Map<String, List<String>> map;

		public Questions()
		{
			this.map = new HashMap<String, List<String>>();
		}
	}

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(QuestionServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentServiceImpl assessmentService = null;

	/** Dependency: EventTrackingService */
	protected EventTrackingService eventTrackingService = null;

	/** Dependency: MnemeService */
	protected MnemeService mnemeService = null;

	/** Dependency: PoolService */
	protected PoolServiceImpl poolService = null;

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** Dependency: SqlService */
	protected SqlService sqlService = null;

	/** Storage handler. */
	protected QuestionStorage storage = null;

	/** Storage option map key for the option to use. */
	protected String storageKey = null;

	/** Map of registered QuestionStorage options. */
	protected Map<String, QuestionStorage> storgeOptions;

	/** Dependency: SubmissionService */
	protected SubmissionServiceImpl submissionService = null;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * {@inheritDoc}
	 */
	public Boolean allowEditQuestion(Question question)
	{
		if (question == null) throw new IllegalArgumentException();
		String userId = sessionManager.getCurrentSessionUserId();

		if (M_log.isDebugEnabled()) M_log.debug("allowEditQuestion: " + question.getId());

		// check permission - user must have MANAGE_PERMISSION in the context
		boolean ok = securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, question.getContext());

		return ok;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean allowManageQuestions(String context, String userId)
	{
		if (context == null) throw new IllegalArgumentException();
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		if (M_log.isDebugEnabled()) M_log.debug("allowManageQuestions: " + context + ": " + userId);

		// check permission - user must have MANAGE_PERMISSION in the context
		boolean ok = securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context);
		return ok;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearStaleMintQuestions()
	{
		if (M_log.isDebugEnabled()) M_log.debug("clearStaleMintQuestions");

		// give it a day
		Date stale = new Date();
		stale.setTime(stale.getTime() - (1000l * 60l * 60l * 24l));

		List<String> ids = this.storage.clearStaleMintQuestions(stale);

		// generate events for any deleted
		for (String id : ids)
		{
			eventTrackingService.post(eventTrackingService.newEvent(MnemeService.QUESTION_DELETE, getQuestionReference(id), true));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void copyPoolQuestions(Pool source, Pool destination) throws AssessmentPermissionException
	{
		if (source == null) throw new IllegalArgumentException();
		if (destination == null) throw new IllegalArgumentException();
		if (source.equals(destination)) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("copyPoolQuestions: source: " + source.getId() + " destination: " + destination.getId());

		String userId = sessionManager.getCurrentSessionUserId();

		// security check
		securityService.secure(userId, MnemeService.MANAGE_PERMISSION, destination.getContext());

		// the new questions in the destination pool may invalidate test-drive submissions in the context
		this.submissionService.removeTestDriveSubmissions(destination.getContext());

		List<String> ids = this.storage.copyPoolQuestions(userId, source, destination, false, null, null, false, null);

		// generate events for any created
		for (String id : ids)
		{
			eventTrackingService.post(eventTrackingService.newEvent(MnemeService.QUESTION_NEW, getQuestionReference(id), true));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Question copyQuestion(Question question, Pool pool) throws AssessmentPermissionException
	{
		if (question == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("copyQuestion: " + question.getId() + ((pool == null) ? "" : (" to pool: " + pool.getId())));

		String userId = sessionManager.getCurrentSessionUserId();
		Date now = new Date();

		// security check
		Pool destination = (pool != null) ? pool : question.getPool();
		securityService.secure(userId, MnemeService.MANAGE_PERMISSION, destination.getContext());

		// the new question in the destination pool may invalidate test-drive submissions in the context
		this.submissionService.removeTestDriveSubmissions(destination.getContext());

		// create a copy of the question
		QuestionImpl rv = this.storage.clone((QuestionImpl) question);

		// clear the id to make it new
		rv.id = null;

		// update created and last modified information
		rv.getCreatedBy().setDate(now);
		rv.getCreatedBy().setUserId(userId);
		rv.getModifiedBy().setDate(now);
		rv.getModifiedBy().setUserId(userId);

		// set the new pool, if needed
		if (pool != null)
		{
			rv.setPool(pool);
		}

		// save
		((QuestionImpl) rv).clearChanged();
		this.storage.saveQuestion((QuestionImpl) rv);

		// event
		eventTrackingService.post(eventTrackingService.newEvent(MnemeService.QUESTION_NEW, getQuestionReference(rv.getId()), true));

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer countQuestions(Pool pool, String search, String questionType, Boolean survey, Boolean valid)
	{
		// TODO: special case, for no search, questionType, survey or valid, pre-count all the pools in the context and cache the results

		if (pool == null) throw new IllegalArgumentException();

		String key = cacheKeyPoolCount(pool.getId());
		String secondaryKey = questionType + ":" + valid;
		Pool.PoolCounts counts = null;

		// check the thread-local cache
		QuestionCountsPool cached = (QuestionCountsPool) this.threadLocalManager.get(key);
		if (cached != null)
		{
			// see if we have done this combination already
			counts = cached.map.get(secondaryKey);
		}

		// if not, check storage
		if (counts == null)
		{
			counts = this.storage.countPoolQuestions(pool, questionType, valid);

			// cache
			if (cached == null)
			{
				cached = new QuestionCountsPool();
				this.threadLocalManager.set(key, cached);
			}
			cached.map.put(secondaryKey, counts);
		}

		// get the part of counts we want based on survey
		if (survey == null)
		{
			return Integer.valueOf(counts.assessment + counts.survey);
		}
		else if (survey)
		{
			return counts.survey;
		}
		else
		{
			return counts.assessment;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer countQuestions(String context, String search, String questionType, Boolean survey, Boolean valid)
	{
		if (context == null) throw new IllegalArgumentException();

		// TODO: search

		String key = cacheKeyContextCount(context);
		String secondaryKey = questionType + ":" + survey + ":" + valid;
		Integer count = null;

		// check the thread-local cache
		QuestionCountsContext cached = (QuestionCountsContext) this.threadLocalManager.get(key);
		if (cached != null)
		{
			// see if we have done this combination already
			count = cached.map.get(secondaryKey);
		}

		// if not, check storage
		if (count == null)
		{
			if (M_log.isDebugEnabled())
				M_log.debug("countQuestions: context: " + context + " search: " + search + "questionType: " + questionType + " survey: " + survey
						+ " valid: " + valid);
			count = this.storage.countContextQuestions(context, questionType, survey, valid);

			// cache
			if (cached == null)
			{
				cached = new QuestionCountsContext();
				this.threadLocalManager.set(key, cached);
			}
			cached.map.put(secondaryKey, count);
		}

		return count;
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
	public Boolean existsQuestion(String questionId)
	{
		if (questionId == null) return null;

		// for thread-local caching
		String key = cacheKey(questionId);
		QuestionImpl rv = (QuestionImpl) this.threadLocalManager.get(key);
		if (rv != null)
		{
			return true;
		}

		if (M_log.isDebugEnabled()) M_log.debug("existsQuestion: " + questionId);

		// assume we are going to need the question
		// return this.storage.existsQuestion(questionId);
		boolean found = (getQuestion(questionId) != null);
		return found;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> findAllNonHistoricalIds()
	{
		return this.storage.findAllNonHistoricalIds();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> findQuestions(Pool pool, FindQuestionsSort sort, String search, String questionType, Integer pageNum, Integer pageSize,
			Boolean survey, Boolean valid)
	{
		if (pool == null) throw new IllegalArgumentException();

		// TODO: search

		if (M_log.isDebugEnabled()) M_log.debug("findQuestions: pool: " + pool.getId());

		return new ArrayList<Question>(this.storage.findPoolQuestions(pool, sort, questionType, pageNum, pageSize, survey, valid));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Question> findQuestions(String context, FindQuestionsSort sort, String search, String questionType, Integer pageNum,
			Integer pageSize, Boolean survey, Boolean valid)
	{
		if (context == null) throw new IllegalArgumentException();

		// TODO: search

		if (M_log.isDebugEnabled()) M_log.debug("findQuestions: context: " + context);

		return new ArrayList<Question>(this.storage.findContextQuestions(context, sort, questionType, pageNum, pageSize, survey, valid));
	}

	/**
	 * {@inheritDoc}
	 */
	public void forceSave(Question question) throws AssessmentPermissionException
	{
		// security check
		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, question.getContext());

		// save
		((QuestionImpl) question).clearChanged();
		this.storage.saveQuestion((QuestionImpl) question);

		// clear thread-local caches
		this.threadLocalManager.set(cacheKey(question.getId()), null);
		this.threadLocalManager.set(this.cacheKeyPoolCount(question.getPool().getId()), null);
		this.threadLocalManager.set(this.cacheKeyContextCount(question.getContext()), null);
		this.threadLocalManager.set(this.cacheKeyPoolQuestions(question.getPool().getId()), null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getPoolQuestionIds(Pool pool, Boolean survey, Boolean valid)
	{
		String key = cacheKeyPoolQuestions(pool.getId());
		String secondaryKey = survey + ":" + valid;
		List<String> questions = null;

		// check the cache
		Questions cached = (Questions) this.threadLocalManager.get(key);
		if (cached != null)
		{
			questions = cached.map.get(secondaryKey);
		}

		// if not, get from storage
		if (questions == null)
		{
			questions = this.storage.getPoolQuestions(pool, survey, valid);

			// cache
			if (cached == null)
			{
				cached = new Questions();
				this.threadLocalManager.set(key, cached);
			}
			cached.map.put(secondaryKey, questions);
		}

		// return a copy
		List<String> rv = new ArrayList<String>(questions);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getQuestion(String questionId)
	{
		if (questionId == null) throw new IllegalArgumentException();

		// for thread-local caching
		String key = cacheKey(questionId);
		QuestionImpl rv = (QuestionImpl) this.threadLocalManager.get(key);
		if (rv != null)
		{
			// return a copy
			return this.storage.clone(rv);
		}

		if (M_log.isDebugEnabled()) M_log.debug("getQuestion: " + questionId);

		rv = this.storage.getQuestion(questionId);

		// thread-local cache (a copy)
		if (rv != null) this.threadLocalManager.set(key, this.storage.clone(rv));

		return rv;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// storage - as configured
			if (this.storageKey != null)
			{
				// if set to "SQL", replace with the current SQL vendor
				if ("SQL".equals(this.storageKey))
				{
					this.storageKey = sqlService.getVendor();
				}

				this.storage = this.storgeOptions.get(this.storageKey);
			}

			// use "default" if needed
			if (this.storage == null)
			{
				this.storage = this.storgeOptions.get("default");
			}

			if (storage == null) M_log.warn("no storage set: " + this.storageKey);

			storage.init();

			M_log.info("init() storage: " + this.storage);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveQuestion(Question question, Pool pool) throws AssessmentPermissionException
	{
		if (question == null) throw new IllegalArgumentException();
		if (pool == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("moveQuestion: " + question.getId() + " to pool: " + pool.getId());

		// security check
		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, question.getContext());
		if (!question.getContext().equals(pool.getContext()))
		{
			securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, pool.getContext());
		}

		// if to the same pool, do nothing
		if (question.getPool().equals(pool)) return;

		// moving a question removes it from one pool, adds it to another, and in both cases,
		// any test-drive submissions from the context (s) may become invalid
		this.submissionService.removeTestDriveSubmissions(question.getPool().getContext());
		if (!question.getPool().getContext().equals(pool.getContext()))
		{
			this.submissionService.removeTestDriveSubmissions(pool.getContext());
		}

		// clear the cache
		String key = cacheKey(question.getId());
		this.threadLocalManager.set(key, null);

		// do the move
		this.storage.moveQuestion(question, pool);

		// event
		eventTrackingService.post(eventTrackingService.newEvent(MnemeService.QUESTION_EDIT, getQuestionReference(question.getId()), true));
	}

	/**
	 * {@inheritDoc}
	 */
	public Question newQuestion(Pool pool, String type) throws AssessmentPermissionException
	{
		if (pool == null) throw new IllegalArgumentException();
		if (type == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("newQuestion: pool: " + pool.getId() + " type: " + type);

		String userId = sessionManager.getCurrentSessionUserId();

		// security check
		securityService.secure(userId, MnemeService.MANAGE_PERMISSION, pool.getContext());

		// adding a question may invalidate test-drive submissions from the context
		this.submissionService.removeTestDriveSubmissions(pool.getContext());

		QuestionImpl question = this.storage.newQuestion();
		question.setPool(pool);

		// set the new created info
		question.getCreatedBy().setUserId(userId);
		question.getCreatedBy().setDate(new Date());

		// set the type, building a type-specific handler
		setType(type, question);

		doSave(question);

		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public void preCountContextQuestions(String context, Boolean valid)
	{
		Map<String, Pool.PoolCounts> rv = this.storage.countPoolQuestions(context, valid);
		for (String poolId : rv.keySet())
		{
			Pool.PoolCounts counts = rv.get(poolId);

			String key = cacheKeyPoolCount(poolId);
			String secondaryKey = null + ":" + valid;

			QuestionCountsPool cached = (QuestionCountsPool) this.threadLocalManager.get(key);
			if (cached == null)
			{
				cached = new QuestionCountsPool();
				this.threadLocalManager.set(key, cached);
			}
			cached.map.put(secondaryKey, counts);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeQuestion(Question question) throws AssessmentPermissionException
	{
		if (question == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("removeQuestion: " + question.getId());

		// security check
		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, question.getContext());

		doRemoveQuestion(question);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveQuestion(Question question) throws AssessmentPermissionException
	{
		if (((QuestionImpl) question).getIsHistorical()) throw new IllegalArgumentException();
		saveTheQuestion(question, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveQuestion(Question question, Boolean allowHistorical) throws AssessmentPermissionException
	{
		if (((allowHistorical == null) || (!allowHistorical.booleanValue())) && ((QuestionImpl) question).getIsHistorical())
			throw new IllegalArgumentException();
		saveTheQuestion(question, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveQuestionAsType(Question question, String newType) throws AssessmentPermissionException
	{
		if (((QuestionImpl) question).getIsHistorical()) throw new IllegalArgumentException();

		// if there is really a type change
		if (!question.getType().equals(newType))
		{
			// special cases
			TypeSpecificQuestion oldHandler = question.getTypeSpecificQuestion();

			// fillin's question text is not in the presentation
			String presentationText = null;
			if (oldHandler instanceof FillBlanksQuestionImpl)
			{
				presentationText = ((FillBlanksQuestionImpl) oldHandler).getText();
			}

			// change the question type - preserve any data we can
			setType(newType, (QuestionImpl) question);

			// special cases
			TypeSpecificQuestion newHandler = question.getTypeSpecificQuestion();

			// fillin's question text is not in the presentation
			if (newHandler instanceof FillBlanksQuestionImpl)
			{
				((FillBlanksQuestionImpl) newHandler).setText(question.getPresentation().getText());
				question.getPresentation().setText(null);
			}
			else if (oldHandler instanceof FillBlanksQuestionImpl)
			{
				question.getPresentation().setText(presentationText);
			}

			// when changing from likert, remove the survey setting
			if (oldHandler instanceof LikertScaleQuestionImpl)
			{
				question.setIsSurvey(Boolean.FALSE);
			}
		}

		// save, but don't clear mint
		saveTheQuestion(question, false);
	}

	/**
	 * Dependency: AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentServiceImpl service)
	{
		assessmentService = service;
	}

	/**
	 * Dependency: EventTrackingService.
	 * 
	 * @param service
	 *        The EventTrackingService.
	 */
	public void setEventTrackingService(EventTrackingService service)
	{
		eventTrackingService = service;
	}

	/**
	 * Dependency: MnemeService.
	 * 
	 * @param service
	 *        The MnemeService.
	 */
	public void setMnemeService(MnemeService service)
	{
		mnemeService = service;
	}

	/**
	 * Dependency: PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setPoolService(PoolServiceImpl service)
	{
		poolService = service;
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		securityService = service;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		sessionManager = service;
	}

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		sqlService = service;
	}

	/**
	 * Set the storage class options.
	 * 
	 * @param options
	 *        The PoolStorage options.
	 */
	public void setStorage(Map options)
	{
		this.storgeOptions = options;
	}

	/**
	 * Set the storage option key to use, selecting which PoolStorage to use.
	 * 
	 * @param key
	 *        The storage option key.
	 */
	public void setStorageKey(String key)
	{
		this.storageKey = key;
	}

	/**
	 * Dependency: SubmissionService.
	 * 
	 * @param service
	 *        The SubmissionService.
	 */
	public void setSubmissionService(SubmissionServiceImpl service)
	{
		submissionService = service;
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
	 * Form a key for caching a question.
	 * 
	 * @param questionId
	 *        The question id.
	 * @return The cache key.
	 */
	protected String cacheKey(String questionId)
	{
		String key = "mneme:question:" + questionId;
		return key;
	}

	/**
	 * Form the cache key for caching questions-in-context count.
	 * 
	 * @param context
	 *        The context.
	 * @return The cache key.
	 */
	protected String cacheKeyContextCount(String context)
	{
		return "mneme:question:context:count:" + context;
	}

	/**
	 * Form the cache key for caching questions-in-pool count.
	 * 
	 * @param poolId
	 *        The pool id.
	 * @return The cache key.
	 */
	protected String cacheKeyPoolCount(String poolId)
	{
		return "mneme:question:pool:count:" + poolId;
	}

	/**
	 * Form the cache key for caching question ids in pool.
	 * 
	 * @param poolId
	 *        The pool id.
	 * @return The cache key.
	 */
	protected String cacheKeyPoolQuestions(String poolId)
	{
		return "mneme:question:pool:questions:" + poolId;
	}

	/**
	 * Copy the questions from source to destination, possibly marked as historical, possibly with attachment translation.
	 * 
	 * @param source
	 *        The source pool.
	 * @param destination
	 *        The destination pool.
	 * @param asHistory
	 *        If set, copy the questions as historical
	 * @param oldToNew
	 *        A map, which, if present, will be filled in with the mapping of the source question id to the destination question id for each question copied.
	 * @param attachmentTranslations
	 *        A list of Translations for attachments and embedded media.
	 * @param merge
	 *        if true, if a question already exists in the pool that matches one to be copied, don't copy.
	 * @param includeQuestions
	 *        if not null, only import the pool's question if its id is in the set.
	 */
	protected void copyPoolQuestions(Pool source, Pool destination, boolean asHistory, Map<String, String> oldToNew,
			List<Translation> attachmentTranslations, boolean merge, Set<String> includeQuestions)
	{
		if (M_log.isDebugEnabled()) M_log.debug("copyPoolQuestionsHistorical: source: " + source.getId() + " destination: " + destination.getId());

		List<String> ids = this.storage.copyPoolQuestions(sessionManager.getCurrentSessionUserId(), source, destination, asHistory, oldToNew,
				attachmentTranslations, merge, includeQuestions);

		if (!asHistory)
		{
			// generate events for any created
			for (String id : ids)
			{
				eventTrackingService.post(eventTrackingService.newEvent(MnemeService.QUESTION_NEW, getQuestionReference(id), true));
			}
		}
	}

	/**
	 * Remove the question
	 * 
	 * @param question
	 *        The question
	 * @param historyPool
	 *        if the pool's history pool already made, or null if there is none.
	 */
	protected void doRemoveQuestion(Question question)
	{
		if (M_log.isDebugEnabled()) M_log.debug("doRemoveQuestion: " + question.getId());

		// get the current from storage
		QuestionImpl current = (question.getId() == null) ? null : this.storage.getQuestion(question.getId());

		// if we don't have one, or we are trying to delete history, that's bad!
		if (current == null) throw new IllegalArgumentException();
		if (current.getIsHistorical()) throw new IllegalArgumentException();

		// removed any assessment dependencies on the question
		this.assessmentService.removeDependency(question);

		// delete
		this.storage.removeQuestion((QuestionImpl) question);

		// clear caches
		this.threadLocalManager.set(cacheKey(question.getId()), null);
		this.threadLocalManager.set(cacheKeyPoolCount(question.getPool().getId()), null);
		this.threadLocalManager.set(cacheKeyContextCount(question.getContext()), null);
		this.threadLocalManager.set(cacheKeyPoolQuestions(question.getPool().getId()), null);

		// event
		eventTrackingService.post(eventTrackingService.newEvent(MnemeService.QUESTION_DELETE, getQuestionReference(question.getId()), true));
	}

	/**
	 * Save the question.
	 * 
	 * @param question
	 *        The question to save.
	 */
	protected void doSave(Question question)
	{
		String userId = sessionManager.getCurrentSessionUserId();
		Date now = new Date();

		String event = MnemeService.QUESTION_EDIT;

		// if the question is new (i.e. no id), set the createdBy information, if not already set
		if (question.getId() == null)
		{
			if (question.getCreatedBy().getUserId() == null)
			{
				question.getCreatedBy().setDate(now);
				question.getCreatedBy().setUserId(userId);
			}

			event = MnemeService.QUESTION_NEW;
		}

		// update last modified information
		question.getModifiedBy().setDate(now);
		question.getModifiedBy().setUserId(userId);

		// save
		((QuestionImpl) question).clearChanged();
		this.storage.saveQuestion((QuestionImpl) question);

		// clear thread-local caches
		this.threadLocalManager.set(cacheKey(question.getId()), null);
		this.threadLocalManager.set(this.cacheKeyPoolCount(question.getPool().getId()), null);
		this.threadLocalManager.set(this.cacheKeyContextCount(question.getContext()), null);
		this.threadLocalManager.set(this.cacheKeyPoolQuestions(question.getPool().getId()), null);

		// event
		eventTrackingService.post(eventTrackingService.newEvent(event, getQuestionReference(question.getId()), true));
	}

	/**
	 * Form an question reference for this question id.
	 * 
	 * @param questionId
	 *        the question id.
	 * @return the pool reference for this pool id.
	 */
	protected String getQuestionReference(String questionId)
	{
		String ref = MnemeService.REFERENCE_ROOT + "/" + MnemeService.QUESTION_TYPE + "/" + questionId;
		return ref;
	}

	/**
	 * Save changes made to this question.
	 * 
	 * @param question
	 *        The question to save.
	 * @param processMint
	 *        If false, skip mint processing.
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to edit this question.
	 */
	protected void saveTheQuestion(Question question, boolean processMint) throws AssessmentPermissionException
	{
		if (question == null) throw new IllegalArgumentException();

		// if any changes made, clear mint
		if (question.getIsChanged())
		{
			// if other than just a survey change
			if (!((QuestionImpl) question).getSurveyOnlyChanged())
			{
				((QuestionImpl) question).clearMint();
			}
		}

		// otherwise we don't save: but if mint, we delete
		if (processMint && (!question.getIsChanged()))
		{
			// if mint, delete instead of save
			if (((QuestionImpl) question).getMint())
			{
				if (M_log.isDebugEnabled()) M_log.debug("saveQuestion: deleting mint: " + question.getId());
				this.removeQuestion(question);
			}

			return;
		}

		if (M_log.isDebugEnabled()) M_log.debug("saveQuestion: " + question.getId());

		// the changed question might invalidate test-drive submissions
		this.submissionService.removeTestDriveSubmissions(question.getContext());

		// security check
		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, question.getContext());

		doSave(question);
	}

	/**
	 * Set the question type, and set it up with a type-specific handler.
	 * 
	 * @param type
	 *        The type.
	 * @param question
	 *        The question.
	 */
	protected void setType(String type, QuestionImpl question)
	{
		question.initType(type);

		// build a type-specific handler
		QuestionPlugin plugin = this.mnemeService.getQuestionPlugin(type);
		TypeSpecificQuestion handler = null;
		if (plugin != null)
		{
			handler = plugin.newQuestion(question);
		}
		if (handler != null)
		{
			question.initTypeSpecificQuestion(handler);
		}
		else
		{
			M_log.warn("setTypeHandler: no plugin for type: " + type);
		}
	}
}
