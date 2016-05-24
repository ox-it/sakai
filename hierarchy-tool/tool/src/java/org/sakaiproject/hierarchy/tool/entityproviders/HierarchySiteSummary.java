package org.sakaiproject.hierarchy.tool.entityproviders;

import lombok.Data;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neelam on 15-Apr-16.
 */
@Data
public class HierarchySiteSummary {
	private String siteUrl;
	private String siteId;
	private List<String> redirects;
	private List<HierarchySiteSummary> subsites;

	public HierarchySiteSummary(){}

	public HierarchySiteSummary(Site site){
		this.siteUrl = site.getUrl();
		this.siteId = site.getId();
	}
}

