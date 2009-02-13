/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/tool/sakai-sdata-impl/src/main/java/org/sakaiproject/sdata/tool/StreamRequestFilter.java $
 * $Id: StreamRequestFilter.java 49082 2008-05-16 00:30:59Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2008 Timefields Ltd
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.RequestFilter;

/**
 * An extension of the standard request filter to remove the parts that prevent
 * the file upload streaming api from working.
 * 
 * @author ieb
 */
public class StreamRequestFilter extends RequestFilter
{
	private static final Log log = LogFactory.getLog(StreamRequestFilter.class);

	private boolean timeOn = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.RequestFilter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException
	{
		super.init(arg0);
		timeOn = "true".equals(arg0.getInitParameter("time-requests"));
		m_toolPlacement = false; // disable tool placement handelling on this
		// request
		m_uploadEnabled = false; // disable upload handling on this request
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException
	{
		if (timeOn)
		{
			long start = System.currentTimeMillis();
			try
			{
				super.doFilter(request, arg1, arg2);
			}
			finally
			{
				long end = System.currentTimeMillis();
				HttpServletRequest hrequest = (HttpServletRequest) request;
				log.info("Request took " + hrequest.getMethod() + " "
						+ hrequest.getPathInfo() + " " + (end - start) + " ms");
			}
		}
		else
		{
			super.doFilter(request, arg1, arg2);

		}
	}

	/*
	 * To enable streaming, we cant call getParam or anything that goes beyond
	 * the headers of the request, hence we have to override this method
	 * 
	 * @see org.sakaiproject.util.RequestFilter#assureSession(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected Session assureSession(HttpServletRequest req, HttpServletResponse res)
	{
		Session s = null;
		String sessionId = null;
		SessionManager sessionManager = org.sakaiproject.tool.cover.SessionManager.getInstance();

		// compute the session cookie suffix, based on this configured server id
		String suffix = System.getProperty(SAKAI_SERVERID);
		if ((suffix == null) || (suffix.length() == 0))
		{
			if (m_displayModJkWarning)
			{
				log
						.info("no sakai.serverId system property set - mod_jk load balancing will not function properly");

				// only display warning once
				// FYI this is not thread safe, but the side effects are
				// negligible and not worth the overhead of synchronizing
				// -lance
				m_displayModJkWarning = false;
			}

			suffix = "sakai";
		}

		// automatic, i.e. not from user activite, request?
		// we cant do this and stream boolean auto =
		// req.getParameter(PARAM_AUTO) != null;
		boolean auto = true;

		//sessionId = req.getParameter(ATTR_SESSION);

		// find our session id from our cookie
		Cookie c = findCookie(req, SESSION_COOKIE, suffix);

		if (sessionId == null && c != null)
		{
			// get our session id
			sessionId = c.getValue();
		}

		if (sessionId != null)
		{
			// remove the server id suffix
			final int dotPosition = sessionId.indexOf(DOT);
			if (dotPosition > -1)
			{
				sessionId = sessionId.substring(0, dotPosition);
			}
			if (log.isDebugEnabled())
			{
				log.debug("assureSession found sessionId in cookie: " + sessionId);
			}

			// find the session
			s = sessionManager.getSession(sessionId);
		}

		// if no cookie, try finding a non-cookie session based on the remote
		// user / principal
		else
		{
			// Note: use principal instead of remote user to avoid any possible
			// confusion with the remote user set by single-signon
			// auth.
			// Principal is set by our Dav interface, which this is desined to
			// cover. -ggolden
			// String remoteUser = req.getRemoteUser();
			Principal principal = req.getUserPrincipal();

			if ((principal != null) && (principal.getName() != null))
			{
				// set our session id to the remote user id
				sessionId = principal.getName();

				// find the session
				s = sessionManager.getSession(sessionId);

				// if not found, make a session for this user
				if (s == null)
				{
					s = sessionManager.startSession(sessionId);
				}
			}
		}

		// if found and not automatic, mark it as active
		if ((s != null) && (!auto))
		{
			s.setActive();
		}

		// if missing, make one
		if (s == null)
		{
			s = sessionManager.startSession();

			// if we have a cookie, but didn't find the session and are creating
			// a new one, mark this
			if (c != null)
			{
				ThreadLocalManager.getInstance().set(SessionManager.CURRENT_INVALID_SESSION,
						SessionManager.CURRENT_INVALID_SESSION);
			}
		}

		// put the session in the request attribute
		req.setAttribute(ATTR_SESSION, s);

		// set this as the current session
		sessionManager.setCurrentSession(s);

		// if we had a cookie and we have no session, clear the cookie 
		// detect closed session in the request
		if ((s == null) && (c != null))
		{
			// remove the cookie
			c = new Cookie(SESSION_COOKIE, "");
			c.setPath("/");
			c.setMaxAge(0);
			res.addCookie(c);
		}

		// if we have a session and had no cookie,
		// or the cookie was to another session id, set the cookie
		if (s != null)
		{
			// the cookie value we need to use
			sessionId = s.getId() + DOT + suffix;

			if ((c == null) || (!c.getValue().equals(sessionId)))
			{
				// set the cookie
				c = new Cookie(SESSION_COOKIE, sessionId);
				c.setPath("/");
				c.setMaxAge(-1);
				res.addCookie(c);
			}
		}

		return s;
	}

}