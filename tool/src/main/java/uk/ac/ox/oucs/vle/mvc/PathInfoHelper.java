package uk.ac.ox.oucs.vle.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UrlPathHelper;

public class PathInfoHelper extends UrlPathHelper {

	public String getLookupPathForRequest(HttpServletRequest request) {
		String lookup = request.getPathInfo();
		if (lookup == null)
			lookup = "/";
		return lookup;
	}
}

