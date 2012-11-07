package org.sakaiproject.hierarchy.impl.portal.dao;

import java.util.Date;

/**
 * This is the persistent node that gets written to the DB.
 * It shouldn't be used by any calling code in tools.
 * It's just in the API so that we don't have to have a second
 * project just for hibernate stuff.
 * 
 * @author buckett
 *
 */
public class PortalPersistentNode {

	private String id;
	private String siteId;
	private String managementSiteId;
	private String name;
	private String path;
	private String pathHash;
	private String redirectUrl;
	private String redirectTitle;
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
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public String getRedirectTitle() {
		return redirectTitle;
	}
	public void setRedirectTitle(String redirectTitle) {
		this.redirectTitle = redirectTitle;
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
