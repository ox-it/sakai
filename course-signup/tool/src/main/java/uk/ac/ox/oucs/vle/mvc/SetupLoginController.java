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
package uk.ac.ox.oucs.vle.mvc;

import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.RequestFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This sets up the URL to return to after performing the login and then redirects
 * to the controller that handles the login.
 *
 * @author Matthew Buckett
 */
public class SetupLoginController extends AbstractController {

	private SessionManager sessionManager;
	private ActiveToolManager toolManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setToolManager(ActiveToolManager toolManager) {
		this.toolManager = toolManager;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {
		Session session = sessionManager.getCurrentSession();
		String returnUrl = request.getParameter("returnUrl");
		// With the upgrade to Sakai 10 the return URL for the login tool has to be a full URL
		// previously it could just be the path.
		String serverUrl = RequestFilter.serverUrl(request);
		session.setAttribute(Tool.HELPER_DONE_URL, serverUrl+returnUrl);
		return new ModelAndView(new RedirectView("/pages/login/do", true));
	}


}
