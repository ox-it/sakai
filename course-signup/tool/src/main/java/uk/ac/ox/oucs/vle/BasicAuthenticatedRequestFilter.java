/*
 * #%L
 * Course Signup Webapp
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.RequestFilter;

/**
 * Simple filter which attempts to use any basic auth headers present as long as the request
 * has been setup by the standard Sakai request filter first.
 *
 * @author buckett
 */
public class BasicAuthenticatedRequestFilter implements Filter {
	
	protected BasicAuth basicAuth = null;
	protected UserDirectoryService userDirectoryService = null;
	protected SessionManager sessionManager = null;

	public void init(FilterConfig filterConfig) throws ServletException {
		basicAuth = new BasicAuth();
		basicAuth.init();
		userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
		sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		if (response instanceof HttpServletResponse) {
			// If we do a login through basic auth then we want to invalidate at the end to stop our session
			// lasting any longer than it needs to.
			if (request.getAttribute(RequestFilter.ATTR_FILTERED) == null) {
				throw new IllegalStateException(RequestFilter.class.getName() + " has not yet processed request.");
			}
			Session session= sessionManager.getCurrentSession();
			boolean invalidate = doBasicAuthLogin((HttpServletRequest) request, session);
			try {
				chain.doFilter(request, response);
			} finally {
				if (invalidate) {
					session.invalidate();
				}
			}
		}
	}

	/**
	 * Attempts the basic auth login.
	 * @return <code>true</code> if the request was authenticated with basic auth and so should be invalidated
	 * at the end.
	 */
	public boolean doBasicAuthLogin(HttpServletRequest request, Session session) throws IOException {
		boolean invalidate =  (userDirectoryService.getAnonymousUser().getId().equals(session.getUserId()));
		basicAuth.doLogin(request);
		invalidate = invalidate && ! userDirectoryService.getAnonymousUser().getId().equals(session.getUserId());
		return invalidate;
	}

}
