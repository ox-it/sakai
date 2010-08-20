package uk.ac.cam.caret.sakai;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.Tool;

/**
 * From: https://saffron.caret.cam.ac.uk/svn/projects/sakai-webapp-tool/trunk/
 * but changed so that we don't change the context path as this broke the Jersey path calculations.
 * Also don't include the sakai.placement in the redirect.
 * 
 * @author buckett
 * 
 */
public class WebappToolServlet extends HttpServlet {
	/**
	 * This init parameter should contain an url to the welcome page
	 */
	public static final String FIRST_PAGE = "main-page";

	public static final String TOOL_NATIVE_URL = "tool-native-url";

	protected void service(final HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		final String contextPath = request.getContextPath();
		HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(
				request);
		if (Boolean.valueOf(getInitParameter(TOOL_NATIVE_URL))) {
			request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		}

		if (request.getPathInfo() == null
				&& getInitParameter(FIRST_PAGE) != null
				&& !getInitParameter(FIRST_PAGE).equals("/")) {
			// Do redirect to first-page
			// We don't use welcome-file in the web.xml as it means that
			// realtive URLs don't work if your
			// index.html is outside the toplevel.
			request.removeAttribute(Tool.NATIVE_URL); // This is so we don't get
														// sakai.placement in
														// the URL.
			response.sendRedirect(contextPath + getInitParameter(FIRST_PAGE));
		} else if (request.getPathInfo() == null
				&& !request.getRequestURI().endsWith("/")) {
			// we should do the default redirect to "/"
			response.sendRedirect(contextPath + "/");
		} else if (request.getPathInfo() != null
				&& (request.getPathInfo().startsWith("/WEB-INF/") || request
						.getPathInfo().equals("/WEB-INF"))) {
			// Can't allow people to see WEB-INF
			response.sendRedirect(contextPath + "/");
		} else {
			// otherwise do the dispatch
			RequestDispatcher dispatcher;
			if (request.getPathInfo() == null) {
				dispatcher = request.getRequestDispatcher("");
			} else {
				dispatcher = request
						.getRequestDispatcher(request.getPathInfo());
			}

			dispatcher.forward(wrappedRequest, response);
		}

	}

}
