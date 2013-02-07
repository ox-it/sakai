package org.sakaiproject.hierarchy.tool;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.Tool;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Makes sure @{link Tool#NATIVE_URL} is set for all requests handled by spring
 * and removes it afterwards. *
 */
public class SpringDispatcherServlet extends DispatcherServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		try {
			super.service(request, response);
		} finally {
			request.removeAttribute(Tool.NATIVE_URL);
		}

	}

}
