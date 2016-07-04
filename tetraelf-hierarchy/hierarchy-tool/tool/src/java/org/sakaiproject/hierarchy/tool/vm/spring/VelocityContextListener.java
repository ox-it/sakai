package org.sakaiproject.hierarchy.tool.vm.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class VelocityContextListener implements ServletContextListener
{

	private static final Log log = LogFactory
			.getLog(VelocityContextListener.class);

	public void contextInitialized(ServletContextEvent event)
	{
		ServletContext servletContext = event.getServletContext();
		WebApplicationContext wac = WebApplicationContextUtils
				.getWebApplicationContext(servletContext);
		if (wac == null)
		{
			log
					.error("Please ensure that the VelocityContextListener is listed After the Spring Context Listener ");
		}
		ServletContextHolder servletContextHolder = (ServletContextHolder) wac
				.getBean("servletContextHolder");
		if ( servletContextHolder == null ) {
			log
			.error("Please ensure that there is a bean of id servletContextHolder and type ServletContextHolder in the applicationContext.xml ");
			
		}
		servletContextHolder.setServletContext(servletContext);

	}

	public void contextDestroyed(ServletContextEvent arg0)
	{
	}

}
