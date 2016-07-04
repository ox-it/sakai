package org.sakaiproject.hierarchy.tool.entityproviders;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hierarchy entity used to access subsites and other details of a site
 * Created by neelam on 15-Apr-16.
 */
public class HierarchyEntityProviderImpl extends AbstractEntityProvider
		implements AutoRegisterEntityProvider, Describeable, EntityProvider, ActionsExecutable, Outputable, Sampleable {

	public final static String ENTITY_PREFIX = "portal-hierarchy";

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON, Formats.XML, Formats.HTML };
	}

	@Override
	public Object getSampleEntity() {
		return new HierarchySiteSummary();
	}

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@Setter
	private transient PortalHierarchyService portalHierarchyService;

	/**
	 * /portal-hierarchy/site.json?portalpath=/path(e.g. /sitename/name)
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public HierarchySiteSummary getDetailsForSite(EntityView view, Map<String, Object> params){
		//get portalpath
		String portalPath =  (String)params.get("portalpath");

		//Check if portalpath is supplied
		if(StringUtils.isBlank(portalPath)){
			throw new IllegalArgumentException("portal path must be set in order to get the site details");
		}
		HierarchySiteSummary hierarchySiteSummary = null;
		PortalNode portalNode = portalHierarchyService.getNode(portalPath);

		//Check if the portalNode is of type PortalNodeSite
		if(portalNode instanceof PortalNodeSite){
			PortalNodeSite siteNode = (PortalNodeSite)portalNode;
			//check if user has permission to view this site
			if(siteNode.canView()){
				hierarchySiteSummary = new HierarchySiteSummary(siteNode.getSite());
				List<PortalNode> nodeChildren = portalHierarchyService.getNodeChildren(portalNode.getId());
				//list to store redirects for the site
				List<String> redirectList = new ArrayList<String>();
				//This list stores all the subsite for the site
				List<HierarchySiteSummary> subsitesList = new ArrayList<HierarchySiteSummary>();
				for(PortalNode nodeChild : nodeChildren){
					//if this node is accessible to current user
					if(nodeChild.canView()){
						//If child node is of type redirect then add to the list of redirects
						if (nodeChild instanceof PortalNodeRedirect ){
							PortalNodeRedirect redirectNode = (PortalNodeRedirect) nodeChild;
							redirectList.add(redirectNode.getUrl());
						}
						//If child node is of type site then add it to the subsites list
						else if(nodeChild instanceof PortalNodeSite){
							PortalNodeSite subsiteNode = (PortalNodeSite)nodeChild;
							subsitesList.add(new HierarchySiteSummary(subsiteNode.getSite()));
						}
					}
				}
				hierarchySiteSummary.setRedirects(redirectList);
				hierarchySiteSummary.setSubsites(subsitesList);
			}
			//user does not have permission to view the site
			else {
				throw new IllegalArgumentException("You do not have permission to view this page");
			}
		}
		else{
			throw new IllegalArgumentException("No site found for the given portal path");
		}
		return hierarchySiteSummary;
	}
}
