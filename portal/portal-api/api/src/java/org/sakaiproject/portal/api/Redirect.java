package org.sakaiproject.portal.api;

/**
 * This is to store redirect url and appendPath information
 * for redirect of a site in hierarchy and it's subsites
 */
public class Redirect {
	private String url;
	private boolean appendPath;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isAppendPath() {
		return appendPath;
	}

	public void setAppendPath(boolean appendPath) {
		this.appendPath = appendPath;
	}
}
