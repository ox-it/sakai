package uk.ac.ox.oucs.vle.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UrlPathHelper;

public class PathInfoHelper extends UrlPathHelper {

	/**
	 * Sakai wrapper around request returns pathInfo of null
	 * which breaks spring mvc
	 */
	@Override
	public String getLookupPathForRequest(HttpServletRequest request) {
		String lookup = request.getPathInfo();
		if (lookup == null)
			lookup = "/";
		return lookup;
	}
}

