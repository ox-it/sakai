package org.sakaiproject.hierarchy.tool.vm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public interface ExceptionHandler {

	public ModelAndView handleException(HttpServletRequest request, HttpServletResponse response, Exception ex);
}
