package org.sakaiproject.exporter.util;

import java.util.Set;

public class Resource {
	// The path within Sakai to find the resource (without the site ID)
	private String sakaiId;
	
	// A made up value to identify an object with the IMSCC file and link objects and their attachments together (sample res100007).
	private String resourceId;
	
	// The location to save the object in the IMSCC file.
	private String location;
	private String use;
	private String title;
	private String url;
	// ID for any metadata file.
	private String metaId;
	private boolean islink;
	private boolean isbank;
	private Set<String> dependencies;

	public String getSakaiId() {
		return sakaiId;
	}

	public void setSakaiId(String sakaiId) {
		this.sakaiId = sakaiId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
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

	public String getMetaId() { return metaId; }

	public void setMetaId(String metaId) { this.metaId = metaId; }

	public boolean isIslink() {
		return islink;
	}

	public void setIslink(boolean islink) {
		this.islink = islink;
	}

	public boolean isIsbank() {
		return isbank;
	}

	public void setIsbank(boolean isbank) {
		this.isbank = isbank;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Set<String> dependencies) {
		this.dependencies = dependencies;
	}
}