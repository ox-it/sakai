package org.sakaiproject.hierarchy.impl;

import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;

/**
 * This class is the implementation of a redirect node which is exposed through the API.
 * @author buckett
 *
 */
public class PortalNodeRedirectImpl extends PortalNodeImpl implements PortalNodeRedirect{

	private String title;
	private String url;
	private final PortalHierarchyService portalHierarchyService;
	
	public PortalNodeRedirectImpl(PortalHierarchyService portalHierarchyService) {
		this.portalHierarchyService = portalHierarchyService;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public boolean canView() {
		return true;
	}

	public boolean canModify() {
		// TODO We should move this out of here.
		return portalHierarchyService.canDeleteNode(getId());
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

}
