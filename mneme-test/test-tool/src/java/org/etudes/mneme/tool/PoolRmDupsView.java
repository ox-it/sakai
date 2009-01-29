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
import java.util.Collections;
import java.util.Comparator;
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
import org.sakaiproject.util.StringUtil;

/**
 * The /pool_rm_dups view for the mneme test tool.
 */
public class PoolRmDupsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(PoolRmDupsView.class);

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

		// remove duplicate pools in the context
		try
		{
			String rv = removeDups(contextId);

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
	 * Remove any pool that has the same title, description, difficulty and points as another pool, keeping only the one that has the latest create
	 * date.
	 * 
	 * @param context
	 *        The context in which to work.
	 * @return Results string.
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission.
	 */
	protected String removeDups(String context) throws AssessmentPermissionException
	{
		StringBuilder results = new StringBuilder();

		// find all pools
		List<Pool> pools = this.poolService.getPools(context);

		// sort so that the titles are together, and these are further ordered by create date
		Collections.sort(pools, new Comparator<Pool>()
		{
			public int compare(Pool arg0, Pool arg1)
			{
				// first on title
				String t0 = arg0.getTitle() == null ? "" : arg0.getTitle();
				String t1 = arg1.getTitle() == null ? "" : arg1.getTitle();
				int rv = t0.compareTo(t1);

				if (rv == 0)
				{
					// next by description
					String d0 = arg0.getDescription() == null ? "" : arg0.getDescription();
					String d1 = arg1.getDescription() == null ? "" : arg1.getDescription();
					rv = d0.compareTo(d1);

					if (rv == 0)
					{
						// next by points
						rv = arg0.getPoints().compareTo(arg1.getPoints());

						if (rv == 0)
						{
							// next by difficulty
							rv = arg0.getDifficulty().compareTo(arg1.getDifficulty());

							if (rv == 0)
							{
								// finally, by create date, descending
								rv = -1 * arg0.getCreatedBy().getDate().compareTo(arg1.getCreatedBy().getDate());
							}
						}
					}
				}

				return rv;
			}
		});

		Pool target = null;
		for (Pool pool : pools)
		{
			if (target == null)
			{
				// see if any further pools match this target
				target = pool;
			}
			else
			{
				// we detect a duplicate as one with an exact title, description, points and difficulty match
				if ((!StringUtil.different(pool.getTitle(), target.getTitle()))
						&& (!StringUtil.different(pool.getDescription(), target.getDescription()))
						&& (!StringUtil.different(pool.getPoints().toString(), target.getPoints().toString()))
						&& (!StringUtil.different(pool.getDifficulty().toString(), target.getDifficulty().toString())))
				{
					// delete this pool
					results.append("Pool: " + pool.getId() + " \"" + pool.getTitle() + "\" removed.<br />");
					this.poolService.removePool(pool);
				}

				// if not a duplicate of the target, we are done with the target's possible duplicates,
				// and need to start again on this one
				else
				{
					target = pool;
				}
			}
		}

		return results.toString();
	}
}
