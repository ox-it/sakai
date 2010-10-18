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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.SecurityService;
import org.etudes.util.api.Translation;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * PoolServiceImpl implements PoolService
 * </p>
 */
public class PoolServiceImpl implements PoolService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(PoolServiceImpl.class);

	protected AssessmentServiceImpl assessmentService = null;

	/** Dependency: AttachmentService */
	protected AttachmentService attachmentService = null;

	/** Messages bundle name. */
	protected String bundle = null;

	/** Dependency: EventTrackingService */
	protected EventTrackingService eventTrackingService = null;

	/** Messages. */
	protected transient InternationalizedMessages messages = null;

	protected QuestionServiceImpl questionService = null;

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** Dependency: SqlService */
	protected SqlService sqlService = null;

	/** Storage handler. */
	protected PoolStorage storage = null;

	/** Storage option map key for the option to use. */
	protected String storageKey = null;

	/** Map of registered PoolStorage options. */
	protected Map<String, PoolStorage> storgeOptions;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * {@inheritDoc}
	 */
	public Boolean allowManagePools(String context)
	{
		if (context == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("allowManagePools: " + context);

		// check permission - user must have MANAGE_PERMISSION in the context
		boolean ok = securityService.checkSecurity(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, context);

		return ok;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearStaleMintPools()
	{
		if (M_log.isDebugEnabled()) M_log.debug("clearStaleMintPools");

		// give it a day
		Date stale = new Date();
		stale.setTime(stale.getTime() - (1000l * 60l * 60l * 24l));

		// get the list of pools that are stale mint
		List<PoolImpl> pools = this.storage.getStaleMintPools(stale);

		// delete each one
		for (PoolImpl pool : pools)
		{
			doRemove(pool);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool copyPool(String context, Pool pool) throws AssessmentPermissionException
	{
		if (context == null) throw new IllegalArgumentException();
		if (pool == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("copyPool: context: " + context + " id: " + pool.getId());

		// security check
		this.securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, context);

		Pool rv = doCopyPool(context, pool, false, null, true, null, false, null);

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
	public Boolean existsPool(String poolId)
	{
		if (M_log.isDebugEnabled()) M_log.debug("existsPool: id: " + poolId);

		return this.storage.existsPool(poolId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Pool> findPools(String context, FindPoolsSort sort, String search)
	{
		if (context == null) throw new IllegalArgumentException();
		if (sort == null) sort = PoolService.FindPoolsSort.title_a;

		if (M_log.isDebugEnabled()) M_log.debug("findPools: context: " + context + " sort: " + sort + " search: " + search);

		// TODO: search

		List<Pool> rv = new ArrayList<Pool>(storage.findPools(context, sort));

		// thread-local cache each found pool
		for (Pool pool : rv)
		{
			String key = cacheKey(pool.getId());
			this.threadLocalManager.set(key, this.storage.clone((PoolImpl) pool));
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Pool> getAllPools(String context)
	{
		if (context == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("getAllPools: " + context);

		// get all the pools for the context
		List<Pool> rv = new ArrayList<Pool>(this.storage.getPools(context, true));

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool getPool(String poolId)
	{
		if (poolId == null) throw new IllegalArgumentException();

		// for thread-local caching
		String key = cacheKey(poolId);
		PoolImpl rv = (PoolImpl) this.threadLocalManager.get(key);
		if (rv != null)
		{
			// return a copy
			return this.storage.clone(rv);
		}

		if (M_log.isDebugEnabled()) M_log.debug("getPool: " + poolId);

		rv = this.storage.getPool(poolId);

		// thread-local cache (a copy)
		if (rv != null) this.threadLocalManager.set(key, this.storage.clone(rv));

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Pool> getPools(String context)
	{
		if (context == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("getPools: " + context);

		// get all the pools for the context
		List<Pool> rv = new ArrayList<Pool>(this.storage.getPools(context, false));

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

			if (this.storage == null) M_log.warn("no storage set: " + this.storageKey);

			// messages
			if (this.bundle != null) this.messages = new ResourceLoader(this.bundle);

			storage.init();

			M_log.info("init(): storage: " + this.storage);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool newPool(String context) throws AssessmentPermissionException
	{
		if (context == null) throw new IllegalArgumentException();
		String userId = sessionManager.getCurrentSessionUserId();

		if (M_log.isDebugEnabled()) M_log.debug("newPool: " + context);

		// security check
		securityService.secure(userId, MnemeService.MANAGE_PERMISSION, context);

		PoolImpl pool = storage.newPool();

		// set the context
		pool.setContext(context);

		// save (also sets attribution)
		doSave(pool);

		return pool;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePool(Pool pool) throws AssessmentPermissionException
	{
		if (pool == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("removePool: " + pool.getId());

		// security check
		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, pool.getContext());

		doRemove((PoolImpl) pool);
	}

	/**
	 * {@inheritDoc}
	 */
	public void savePool(Pool pool) throws AssessmentPermissionException
	{
		if (pool == null) throw new IllegalArgumentException();

		// if any changes made, clear mint
		if (((PoolImpl) pool).getChanged())
		{
			((PoolImpl) pool).clearMint();
		}

		// otherwise we don't save: but if mint, we delete
		else
		{
			// if mint, delete instead of save
			if (((PoolImpl) pool).getMint())
			{
				if (M_log.isDebugEnabled()) M_log.debug("savePool: deleting mint: " + pool.getId());

				// make sure any questions are also removed
				doRemove((PoolImpl) pool);
			}

			return;
		}

		if (M_log.isDebugEnabled()) M_log.debug("savePool: " + pool.getId());

		// security check
		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, pool.getContext());

		doSave((PoolImpl) pool);
	}

	/**
	 * Set the AssessmentService.
	 * 
	 * @param service
	 *        the AssessmentService.
	 */
	public void setAssessmentService(AssessmentServiceImpl service)
	{
		this.assessmentService = service;
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
	 * Set the message bundle.
	 * 
	 * @param bundle
	 *        The message bundle.
	 */
	public void setBundle(String name)
	{
		this.bundle = name;
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
	 * Add a formatted date to a source string, using a message selector.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param source
	 *        The original string.
	 * @param date
	 *        The date to format.
	 * @return The source and date passed throught the selector message.
	 */
	protected String addDate(String selector, String source, Date date)
	{
		// format the date
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		String fmt = format.format(date);

		// the args
		Object[] args = new Object[2];
		args[0] = source;
		args[1] = fmt;

		// format the works
		String rv = this.messages.getFormattedMessage(selector, args);

		return rv;
	}

	/**
	 * Form a key for caching a pool.
	 * 
	 * @param poolId
	 *        The pool id.
	 * @return The cache key.
	 */
	protected String cacheKey(String poolId)
	{
		String key = "mneme:pool:" + poolId;
		return key;
	}

	/**
	 * Copy a pool and all questions
	 * 
	 * @param context
	 *        The destination context.
	 * @param pool
	 *        The source pool.
	 * @param asHistory
	 *        If set, make the pool and questions all historical.
	 * @param oldToNew
	 *        A map, which, if present, will be filled in with the mapping of the source question id to the destination question id for each question copied.
	 * @param appendTitle
	 *        if true, append text to the title, else leave the title an exact copy.
	 * @param attachmentTranslations
	 *        A list of Translations for attachments and embedded media.
	 * @param merge
	 *        if true, if there is an existing pool with the same title, use it and don't create a new pool.
	 * @param includeQuestions
	 *        if not null, only import the pool's question if its id is in the set.
	 * @return The copied pool.
	 */
	protected Pool doCopyPool(String context, Pool pool, boolean asHistory, Map<String, String> oldToNew, boolean appendTitle,
			List<Translation> attachmentTranslations, boolean merge, Set<String> includeQuestions)
	{
		String userId = sessionManager.getCurrentSessionUserId();
		Date now = new Date();

		Pool rv = null;

		// if merging, find an existing pool with the same title and use it (for history, we always need a new pool)
		if (merge && (!asHistory))
		{
			// do we have a pool already?
			List<Pool> pools = getPools(context);
			for (Pool existingPool : pools)
			{
				if (!StringUtil.different(existingPool.getTitle(), pool.getTitle()))
				{
					rv = existingPool;
					break;
				}
			}
		}

		// if we don't have a pool yet, make one
		if (rv == null)
		{
			// make a copy of the pool
			rv = storage.clone((PoolImpl) pool);

			// clear the id to make it a new one
			((PoolImpl) rv).id = null;

			// set the context
			rv.setContext(context);

			// update created and last modified information
			rv.getCreatedBy().setDate(now);
			rv.getCreatedBy().setUserId(userId);
			rv.getModifiedBy().setDate(now);
			rv.getModifiedBy().setUserId(userId);

			// add to the title
			if (appendTitle)
			{
				rv.setTitle(addDate("copy-text", rv.getTitle(), now));
			}

			// translate the description embedded references
			rv.setDescription(this.attachmentService.translateEmbeddedReferences(rv.getDescription(), attachmentTranslations));

			// clear the changed settings
			((PoolImpl) rv).clearChanged();

			// save
			storage.savePool((PoolImpl) rv);
		}

		// make a copy of the questions
		this.questionService.copyPoolQuestions(pool, rv, asHistory, oldToNew, attachmentTranslations, merge, includeQuestions);

		if (asHistory)
		{
			((PoolImpl) rv).makeHistorical(rv);
			storage.savePool((PoolImpl) rv);
		}

		// event for non-history pools
		else
		{
			eventTrackingService.post(eventTrackingService.newEvent(MnemeService.POOL_NEW, getPoolReference(rv.getId()), true));
		}

		return rv;
	}

	/**
	 * Remove the pool, the questions, and deal with dependencies
	 * 
	 * @param pool
	 *        The pool to remove.
	 */
	protected void doRemove(PoolImpl pool)
	{
		// remove each of our questions
		List<String> qids = pool.getAllQuestionIds(null, null);
		for (String qid : qids)
		{
			Question q = this.questionService.getQuestion(qid);
			if (q != null)
			{
				this.questionService.doRemoveQuestion(q);
			}
		}

		// remove any assessment dependencies on this pool
		this.assessmentService.removeDependency(pool);

		// clear the cache
		this.threadLocalManager.set(cacheKey(pool.getId()), null);

		// remove the pool
		storage.removePool(pool);

		// event
		eventTrackingService.post(eventTrackingService.newEvent(MnemeService.POOL_DELETE, getPoolReference(pool.getId()), true));
	}

	/**
	 * Save the pool.
	 * 
	 * @param pool
	 *        The pool.
	 */
	protected void doSave(PoolImpl pool)
	{
		String userId = sessionManager.getCurrentSessionUserId();
		Date now = new Date();

		String event = MnemeService.POOL_EDIT;

		// if the pool is new (i.e. no id), set the createdBy information, if not already set
		if (pool.getId() == null)
		{
			if (pool.getCreatedBy().getUserId() == null)
			{
				pool.getCreatedBy().setDate(now);
				pool.getCreatedBy().setUserId(userId);
			}

			event = MnemeService.POOL_NEW;
		}

		// update last modified information
		pool.getModifiedBy().setDate(now);
		pool.getModifiedBy().setUserId(userId);

		// clear the changed settings
		pool.clearChanged();

		// clear the cache
		this.threadLocalManager.set(cacheKey(pool.getId()), null);

		// save
		storage.savePool(pool);

		// event
		eventTrackingService.post(eventTrackingService.newEvent(event, getPoolReference(pool.getId()), true));
	}

	/**
	 * Form an pool reference for this pool id.
	 * 
	 * @param poolId
	 *        the pool id.
	 * @return the pool reference for this pool id.
	 */
	protected String getPoolReference(String poolId)
	{
		String ref = MnemeService.REFERENCE_ROOT + "/" + MnemeService.POOL_TYPE + "/" + poolId;
		return ref;
	}

	/**
	 * Make a copy of this pool and its questions as historical.
	 * 
	 * @param pool
	 *        The pool to copy.
	 * @param oldToNew
	 *        A map, which, if present, will be filled in with the mapping of the source question id to the destination question id for each question copied.
	 * @return The historical pool.
	 */
	protected Pool makePoolHistory(Pool pool, Map<String, String> oldToNew)
	{
		if (pool == null) throw new IllegalArgumentException();

		if (M_log.isDebugEnabled()) M_log.debug("makePoolHistory: " + pool.getId());

		Pool rv = doCopyPool(pool.getContext(), pool, true, oldToNew, false, null, false, null);

		return rv;
	}
}
