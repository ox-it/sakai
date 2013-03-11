package org.sakaiproject.hierarchy.tool.vm;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UrlPathHelper;

/**
 * Uses the pathinfo as the lookup in the controller.
 * @author buckett
 *
 */
public class PathInfoHelper extends UrlPathHelper {

	
	public String getLookupPathForRequest(HttpServletRequest request) {
		String lookup = request.getPathInfo();
		if (lookup == null)
			lookup = "/";
		return lookup;
	}
}
