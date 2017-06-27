package org.sakaiproject.hierarchy.tool.entityproviders;

import lombok.Data;
import org.sakaiproject.hierarchy.api.model.PortalNode;

@Data
public class PortalNodeSummary {
	private String nodeUrl;
	private boolean primary = false;

	public PortalNodeSummary(){};

	public PortalNodeSummary(PortalNode portalNode){
		this.nodeUrl = portalNode.getUrlPath();
	}

}