/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.login.tool;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Calendar;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.TwoFactorAuthentication;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Web;

/**
 * This is the login handler to deal with two factor logins.
 *  
 */
public class TwoFactorLogin extends HttpServlet
{
	/**
	 * Attribute set when container login went well.
	 */
	public static final String ATTR_TWOFACTOR_SUCCESS = TwoFactorLogin.class.getName()+"#container.success";

	/** Session attribute set and shared with ContainerLoginTool: if set we have failed container and need to check internal. */
	public static final String ATTR_TWOFACTOR_CHECKED = "sakai.login.container.checked";
	
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(TwoFactorLogin.class);
	
	private String defaultReturnUrl;

	private String redirectParameter = "redirect";

	private String twoFactorCheckPath;
	
	private String shibbolethUrl;

	private TwoFactorAuthentication twoFactorAuthentication;

	private long gracePeriod = 5000; // 5 seconds

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Two Factor Login";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		M_log.info("init()");
		defaultReturnUrl = ServerConfigurationService.getString("portalPath", "/portal");
		twoFactorCheckPath = config.getInitParameter("twofactor");
		shibbolethUrl = config.getInitParameter("shibbolethUrl");
		twoFactorAuthentication = (TwoFactorAuthentication)ComponentManager.get(TwoFactorAuthentication.class);
		
		if (config.getInitParameter("gracePeriod") != null) {
			gracePeriod = Long.parseLong(config.getInitParameter("gracePeriod"));
		}
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = SessionManager.getCurrentSession();
		
		boolean onCorrectUrl = twoFactorCheckPath.startsWith(req.getContextPath());
		// This url may not have been processed
		if (!onCorrectUrl) {
			String twoFactorCheckUrl = Web.serverUrl(req) + twoFactorCheckPath;

			String queryString = req.getQueryString();
			if (queryString != null) twoFactorCheckUrl = twoFactorCheckUrl + "?" + queryString;

			String redirectUrl = shibbolethUrl+"?target="+ URLEncoder.encode(twoFactorCheckUrl, "UTF-8");
			res.sendRedirect(res.encodeRedirectURL(redirectUrl));
			return;
		}
		
		validate(req, res);
		
		if (!res.isCommitted()) {
			twoFactorAuthentication.markTwoFactor();

			String url = getUrl(req, session, Tool.HELPER_DONE_URL);
			res.sendRedirect(res.encodeRedirectURL(url));
		}
	}

	/**
	 * Gets a URL from the session, if not found returns the portal URL.
	 * @param session The users HTTP session.
	 * @param sessionAttribute The attribute the URL is stored under.
	 * @return The URL.
	 */
	private String getUrl(HttpServletRequest request, Session session, String sessionAttribute) {
		String url = request.getParameter(redirectParameter);
		if (url == null || url.length() == 0) 
		{
			url = (String) session.getAttribute(sessionAttribute);
			if (url == null || url.length() == 0)
			{
				M_log.debug("No "+ sessionAttribute + " URL, redirecting to portal URL.");
				url = defaultReturnUrl;
			}
		}
		return url;
	}
	
	/** 
	 * Called to validate that the request should be allowed, should handle error itself by committing the response.
	 */
	protected void validate(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		// This will be something like username@OX.AC.UK
		String remoteUser = req.getRemoteUser();
		if (remoteUser == null) {
			throw new RuntimeException("No username was passed");
		}

		boolean goodUser = false;
		try {
			User loginUser = UserDirectoryService.getUserByAid(remoteUser);
			User currentUser = UserDirectoryService.getCurrentUser();
			goodUser = loginUser.equals(currentUser);
		} catch (UserNotDefinedException e) {
			M_log.warn("Failed to find user to check two factor with: "+ remoteUser);
		}
		if (!goodUser) {
			throw new RuntimeException("You are not currently logged in or your usernames don't match.");
		}
		
		// Check the timestamp.
		boolean goodInstant = false;
		String instantAttr = (String)req.getAttribute("Shib-Authentication-Instant");
		if (instantAttr != null) {
			try {
				Calendar parseDateTime = DatatypeConverter.parseDateTime(instantAttr);
				Date limit = new Date(System.currentTimeMillis()-gracePeriod);
				Date instant = parseDateTime.getTime();
				goodInstant = limit.before(instant);
			} catch (IllegalArgumentException iae) {
				M_log.warn("Failed to parse timestamp: "+instantAttr);
			}
		}
		if (!goodInstant) {
			throw new RuntimeException("You login took too long to process, please try again.");
		}
	}
}
