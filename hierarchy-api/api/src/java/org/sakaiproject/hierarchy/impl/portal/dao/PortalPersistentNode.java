package org.sakaiproject.hierarchy.impl.portal.dao;

public class PortalPersistentNode {

	private String id;
	private String siteId;
	private String managementSiteId;
	private String name;
	private String path;
	private String pathHash;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public String getManagementSiteId() {
		return managementSiteId;
	}
	public void setManagementSiteId(String managementSiteId) {
		this.managementSiteId = managementSiteId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPathHash() {
		return pathHash;
	}
	public void setPathHash(String pathHash) {
		this.pathHash = pathHash;
	}
	
}
