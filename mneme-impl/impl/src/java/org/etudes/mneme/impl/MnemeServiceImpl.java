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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * MnemeServiceImpl implements MnemeService
 */
public class MnemeServiceImpl implements MnemeService, Runnable
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(MnemeServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService = null;

	/** The checker thread. */
	protected Thread checkerThread = null;

	/** Dependency: FunctionManager */
	protected FunctionManager functionManager = null;

	/** Dependency: PoolService */
	protected PoolService poolService = null;

	/** Question type plugins. */
	protected Map<String, QuestionPlugin> questionPlugins = new HashMap<String, QuestionPlugin>();

	/** Dependency: QuestionService */
	protected QuestionService questionService = null;

	/** Dependency: ThreadLocalManager */
	protected ThreadLocalManager threadLocalManager = null;

	/** The thread quit flag. */
	protected boolean threadStop = false;

	/** How long to wait (ms) between checks for timed-out submission in the db. 0 disables. */
	protected long timeoutCheckMs = 1000L * 60L * 60L * 12L;

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		// stop the checking thread
		stop();

		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public QuestionPlugin getQuestionPlugin(String type)
	{
		return this.questionPlugins.get(type);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<QuestionPlugin> getQuestionPlugins()
	{
		List<QuestionPlugin> rv = new ArrayList<QuestionPlugin>(this.questionPlugins.values());

		// sort- popularity (desc), then localized type name (asc)
		Collections.sort(rv, new Comparator()
		{
			public int compare(Object arg0, Object arg1)
			{
				// compare based on the localized type name
				int rv = -1 * ((QuestionPlugin) arg0).getPopularity().compareTo(((QuestionPlugin) arg1).getPopularity());
				if (rv == 0)
				{
					rv = ((QuestionPlugin) arg0).getTypeName().compareTo(((QuestionPlugin) arg1).getTypeName());
				}
				return rv;
			}
		});

		return rv;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// register functions
			functionManager.registerFunction(GRADE_PERMISSION);
			functionManager.registerFunction(GUEST_PERMISSION);
			functionManager.registerFunction(MANAGE_PERMISSION);
			functionManager.registerFunction(SUBMIT_PERMISSION);
			functionManager.registerFunction(COURSE_EVAL_PERMISSION);

			// start the checking thread
			if (timeoutCheckMs > 0)
			{
				start();
			}

			M_log.info("init(): timout check seconds: " + timeoutCheckMs / 1000);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerQuestionPlugin(QuestionPlugin plugin)
	{
		this.questionPlugins.put(plugin.getType(), plugin);
	}

	/**
	 * Run the event checking thread.
	 */
	public void run()
	{
		// since we might be running while the component manager is still being created and populated,
		// such as at server startup, wait here for a complete component manager
		ComponentManager.waitTillConfigured();

		// loop till told to stop
		while ((!threadStop) && (!Thread.currentThread().isInterrupted()))
		{
			try
			{
				// ask the various services to clear their stale mints
				this.questionService.clearStaleMintQuestions();
				this.poolService.clearStaleMintPools();
				this.assessmentService.clearStaleMintAssessments();
			}
			catch (Throwable e)
			{
				M_log.warn("run: will continue: ", e);
			}
			finally
			{
				// clear out any current current bindings
				this.threadLocalManager.clear();
			}

			// take a small nap
			try
			{
				Thread.sleep(timeoutCheckMs);
			}
			catch (Exception ignore)
			{
			}
		}
	}

	/**
	 * Dependency: AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		assessmentService = service;
	}

	/**
	 * Dependency: FunctionManager.
	 * 
	 * @param service
	 *        The FunctionManager.
	 */
	public void setFunctionManager(FunctionManager service)
	{
		functionManager = service;
	}

	/**
	 * Dependency: PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setPoolService(PoolService service)
	{
		poolService = service;
	}

	/**
	 * Dependency: QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionService service)
	{
		questionService = service;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The ThreadLocalManager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		this.threadLocalManager = service;
	}

	/**
	 * Set the # seconds to wait between db checks for timed-out submissions.
	 * 
	 * @param time
	 *        The # seconds to wait between db checks for timed-out submissions.
	 */
	public void setTimeoutCheckSeconds(String time)
	{
		this.timeoutCheckMs = Integer.parseInt(time) * 1000L;
	}

	/**
	 * Start the clean and report thread.
	 */
	protected void start()
	{
		threadStop = false;

		checkerThread = new Thread(this, getClass().getName());
		checkerThread.start();
	}

	/**
	 * Stop the clean and report thread.
	 */
	protected void stop()
	{
		if (checkerThread == null) return;

		// signal the thread to stop
		threadStop = true;

		// wake up the thread
		checkerThread.interrupt();

		checkerThread = null;
	}
}
