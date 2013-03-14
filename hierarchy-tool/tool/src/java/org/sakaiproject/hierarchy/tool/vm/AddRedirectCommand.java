package org.sakaiproject.hierarchy.tool.vm;

/**
 * Command class for adding a new redirect.
 */
public class AddRedirectCommand {
	private String name;
	private String title;
	private String url;
	private boolean appendPath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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