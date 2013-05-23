package org.sakaiproject.hierarchy.impl;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public class PortalNodeSiteImpl extends PortalNodeImpl implements PortalNodeSite {

	private Site site;
	private Site managementSite;

	private SecurityService securityService;
	private SiteService siteService;
	
	public PortalNodeSiteImpl(SecurityService securityService, SiteService siteService) {
		this.securityService = securityService;
		this.siteService = siteService;
	}
	public boolean canModify() {
		return securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference());
	}

	public boolean canView() {
		if (site == null) {
			return false;
			
		}
		return siteService.allowAccessSite(site.getId());
	}
	public Site getManagementSite() {
		return managementSite;
	}

	public Site getSite() {
		assert(site != null);
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public void setManagementSite(Site managementSite) {
		this.managementSite = managementSite;
	}
	public String getTitle() {
		Site site = getSite();
		return (site != null)? site.getTitle(): null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((managementSite == null) ? 0 : managementSite.getId().hashCode());
		result = prime * result + ((site == null) ? 0 : site.getId().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PortalNodeSiteImpl other = (PortalNodeSiteImpl) obj;
		if (managementSite == null) {
			if (other.managementSite != null)
				return false;
		} else if (!managementSite.getId().equals(other.managementSite.getId()))
			return false;
		if (site == null) {
			if (other.site != null)
				return false;
		} else if (!site.getId().equals(other.site.getId()))
			return false;
		return true;
	}
	
}
