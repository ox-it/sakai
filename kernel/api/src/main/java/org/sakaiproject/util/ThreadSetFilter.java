package org.sakaiproject.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sakaiproject.thread_local.cover.ThreadLocalManager;

/**
 * A simple filter that just sets a thread local based on the configuration.
 * 
 * @author buckett
 *
 */
public class ThreadSetFilter implements Filter {

	private String key;
	private String value;

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			ThreadLocalManager.set(key,value);
			chain.doFilter(request, response);
		} finally {
			ThreadLocalManager.set(key, null);
		}

	}

	public void init(FilterConfig filterConfig) throws ServletException {
		key = filterConfig.getInitParameter("key");
		value = filterConfig.getInitParameter("value");
	}

}
