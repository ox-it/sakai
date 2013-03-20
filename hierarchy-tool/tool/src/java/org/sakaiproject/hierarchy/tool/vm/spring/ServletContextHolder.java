package org.sakaiproject.hierarchy.tool.vm.spring;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServletContextHolder
{
	private static final Log log = LogFactory
	.getLog(ServletContextHolder.class);
	private ServletContext servletContext = null;
	
	public ServletContextHolder() {
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
		log.info("Servlet Context initialised on "+servletContext.getRealPath("/"));
	}
}
