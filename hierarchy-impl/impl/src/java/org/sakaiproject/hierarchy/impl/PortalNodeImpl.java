package org.sakaiproject.hierarchy.impl;

import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.site.api.Site;

public class PortalNodeImpl implements PortalNode {

	private String id;
	private String name;
	private String path;
	
	private Site site;
	private Site managementSite;
	
	
	public boolean canModify() {
		// TODO Auto-generated method stub
		
		return false;
	}

	public boolean canView() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getId() {
		return id;
	}

	public Site getManagementSite() {
		return managementSite;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public Site getSite() {
		return site;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public void setManagementSite(Site managementSite) {
		this.managementSite = managementSite;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((managementSite == null) ? 0 : managementSite.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((site == null) ? 0 : site.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PortalNodeImpl other = (PortalNodeImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (managementSite == null) {
			if (other.managementSite != null)
				return false;
		} else if (!managementSite.equals(other.managementSite))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (site == null) {
			if (other.site != null)
				return false;
		} else if (!site.equals(other.site))
			return false;
		return true;
	}

	
}
