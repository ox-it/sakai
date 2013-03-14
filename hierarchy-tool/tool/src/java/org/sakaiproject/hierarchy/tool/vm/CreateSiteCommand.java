package org.sakaiproject.hierarchy.tool.vm;

public class CreateSiteCommand {

	private String name;
	private String siteId;
	private boolean cancelled;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public String toString() {
		return "Name: " + name + ", SiteId: " + siteId + ", Cancelled: " + cancelled;
	}

}
