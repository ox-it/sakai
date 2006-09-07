/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.hierarchy.tool;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Tool;

/**
 * @author andrew
 */
// FIXME: Tool
public class ControllerServlet extends HttpServlet
{
	public static Log log = LogFactory.getLog(ControllerServlet.class);

	/**
	 * Required for serialization... also to stop eclipse from giving me a
	 * warning!
	 */
	private static final long serialVersionUID = 676743152200357708L;

	public static final String SAVED_REQUEST_URL = "org.sakaiproject.search.api.last-request-url";

	private static final String PANEL = "panel";

	private static final Object TITLE_PANEL = "Title";


	public void init(ServletConfig servletConfig) throws ServletException
	{

		super.init(servletConfig);

		ServletContext sc = servletConfig.getServletContext();


	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		execute(request, response);
	}

	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{


		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		addLocalHeaders(request);

		if (TITLE_PANEL.equals(request.getParameter(PANEL)))
		{

			String targetPage = "/WEB-INF/pages/title.jsp";
			RequestDispatcher rd = request.getRequestDispatcher(targetPage);
			rd.forward(request, response);

		}
		else
		{
			String path = request.getPathInfo();
			if (path == null || path.length() == 0)
			{
				path = "/index";
			}
			if (!path.startsWith("/"))
			{
				path = "/" + path;
			}
			String targetPage = "/WEB-INF/pages" + path + ".jsp";
			RequestDispatcher rd = request.getRequestDispatcher(targetPage);
			rd.forward(request, response);

		}

		request.removeAttribute(Tool.NATIVE_URL);
	}

	public void addLocalHeaders(HttpServletRequest request)
	{
		String sakaiHeader = (String) request.getAttribute("sakai.html.head");
		String skin = "default/"; // this could be changed in the future to make search skin awaire
		String localStylesheet = "<link href=\"/sakai-search-tool/styles/"+skin+"searchStyle.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />";
		request.setAttribute("sakai.html.head", localStylesheet + sakaiHeader);
	}


	/**
	 * Check to see if the reques represents the Tool default page. This is not
	 * the same as the view Home. It is the same as first entry into a Tool or
	 * when the page is refreshed
	 * 
	 * @param request
	 * @return true if the page is the Tool default page
	 */
	private boolean isPageToolDefault(HttpServletRequest request)
	{
		if (TITLE_PANEL.equals(request.getParameter(PANEL))) return false;
		String pathInfo = request.getPathInfo();
		String queryString = request.getQueryString();
		String method = request.getMethod();
		return ("GET".equalsIgnoreCase(method)
				&& (pathInfo == null || request.getPathInfo().length() == 0) && (queryString == null || queryString
				.length() == 0));
	}

	/**
	 * Check to see if the request represents a page that can act as a restor
	 * point.
	 * 
	 * @param request
	 * @return true if it is possible to restore to this point.
	 */
	private boolean isPageRestorable(HttpServletRequest request)
	{
		if (TITLE_PANEL.equals(request.getParameter(PANEL))) return false;

		if ("GET".equalsIgnoreCase(request.getMethod())) return true;

		return false;
	}

}
