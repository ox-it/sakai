/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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

package org.etudes.mneme.tool;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * The /courier view for the mneme tool.
 */
public class CourierView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(CourierView.class);

	/** The session manager. */
	protected SessionManager sessionManager = null;

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
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// undo any changes to state from setting up for this request... as if it never happened!
		uiService.undoPrepareGet(req, res);

		// res.setContentType("text/plain; charset=UTF-8");
		// res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		// res.addDateHeader("Last-Modified", System.currentTimeMillis());
		// res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		// res.addHeader("Pragma", "no-cache");

		// mark the session as active
		Session s = sessionManager.getCurrentSession();
		if (s != null)
		{
			s.setActive();
		}
		else
		{
			M_log.warn("courierGet: no session");
		}

		// send a do-nothing response
		context.getResponseWriter().println("//");
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
	 * Set the session manager.
	 * 
	 * @param manager
	 *        The session manager.
	 */
	public void setSessionManager(SessionManager manager)
	{
		sessionManager = manager;
	}
}
