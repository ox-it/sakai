/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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

package org.etudes.mneme.tool;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * The /pool_rm view for the mneme test tool.
 */
public class PoolRmView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(PoolRmView.class);

	/** The pool service. */
	protected PoolService poolService = null;

	/** The security service. */
	protected SecurityService securityService = null;

	/** The ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
	{
		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			throw new IllegalArgumentException();
		}

		// one parameter expected
		if (params.length != 3)
		{
			throw new IllegalArgumentException();
		}

		String contextId = params[2];

		// remove all pools in the context
		try
		{
			String rv = removePools(contextId);

			context.put("rv", rv);
		}
		catch (AssessmentPermissionException e)
		{
			M_log.warn(e);
		}

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Set the PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setPoolService(PoolService service)
	{
		this.poolService = service;
	}

	/**
	 * Set the security service.
	 * 
	 * @param service
	 *        The security service.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Set the thread local manager.
	 * 
	 * @param service
	 *        The thread local manager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		this.threadLocalManager = service;
	}

	/**
	 * Remove all pools in the context.
	 * 
	 * @param context
	 *        The context in which to work.
	 * @return Results string.
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission.
	 */
	protected String removePools(String context) throws AssessmentPermissionException
	{
		StringBuilder results = new StringBuilder();

		// find all pools
		List<Pool> pools = this.poolService.getPools(context);

		for (Pool pool : pools)
		{
			// delete this pool
			results.append("Pool: " + pool.getId() + " \"" + pool.getTitle() + "\" removed.<br />");
			this.poolService.removePool(pool);
		}

		return results.toString();
	}
}
