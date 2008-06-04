package org.sakaiproject.hierarchy.impl.portal.dao;

import java.util.Date;

public class PortalPersistentNode {

	private String id;
	private String siteId;
	private String managementSiteId;
	private String name;
	private String path;
	private String pathHash;
	private Date created;
	private Date updated;
	
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
	public Date getCreated()
	{
		return created;
	}
	public void setCreated(Date created)
	{
		this.created = created;
	}
	public Date getUpdated()
	{
		return updated;
	}
	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}
	
}
