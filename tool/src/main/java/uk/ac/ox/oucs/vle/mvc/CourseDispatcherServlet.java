package uk.ac.ox.oucs.vle.mvc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.Tool;
import org.springframework.web.servlet.DispatcherServlet;

public class CourseDispatcherServlet extends DispatcherServlet{

	/**
	 * this is overridden to ensure that the Native URL is present.
	 */
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		try
		{
			super.service(request, response);
		}
		finally
		{
			request.removeAttribute(Tool.NATIVE_URL);
		}

	}
}
