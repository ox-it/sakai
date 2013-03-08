package org.sakaiproject.hierarchy.tool.vm;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DeleteSiteController {

	private SiteService siteService;
	private PortalHierarchyService portalHierarchyService;

	@Autowired
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService portalHierarchyService) {
	    this.portalHierarchyService = portalHierarchyService;
	}
	
	@ModelAttribute("command")
	public DeleteSiteCommand getDeleteSiteCommand() {
	    return new DeleteSiteCommand();
	}

	public void init() {
		
	}
	

    @RequestMapping(method = RequestMethod.GET)
    public String showForm() {
        return "delete";
    }
	
    
    @RequestMapping(method = RequestMethod.POST)
    protected String doSubmitAction(HttpServletRequest request,
			HttpServletResponse response, @ModelAttribute("command") DeleteSiteCommand object, BindingResult result, ModelMap model)
			throws Exception {
		PortalNode node = portalHierarchyService.getCurrentPortalNode();
		DeleteSiteCommand command = (DeleteSiteCommand) object;
		List<PortalNodeSite> nodes = portalHierarchyService.getNodesFromRoot(node.getId());
		String parentPath = nodes.get(nodes.size()-1).getPath();
		try {
		    portalHierarchyService.deleteNode(node.getId());
			// Do we want to remove the site?
			if (command.isDeleteSite() && node instanceof PortalNodeSite) {
				siteService.removeSite(((PortalNodeSite)node).getSite());
			}
			
			model.put("siteUrl", ServerConfigurationService.getPortalUrl()+"/hierarchy"+ parentPath);
			
			return "redirect";
		} catch (IllegalStateException ise) {
			result.reject("delete.error.children");
			return showForm();
		}

	}
	
	@ModelAttribute
	public void referenceData(HttpServletRequest request, ModelMap model) {
		Map<String, Object> data = VelocityControllerUtils.referenceData(request);
		PortalHierarchyService phs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		PortalNode current = phs.getCurrentPortalNode();
		boolean canDelete = true;
		boolean canDeleteSite = false;
		boolean hasChildren = false;
		boolean isSiteUsedAgain = false;
		if (current != null) {
			List<PortalNode> children = phs.getNodeChildren(current.getId());
			hasChildren = children.size() > 0;
		}
		canDelete = phs.canDeleteNode(current.getId());
		boolean isSiteNode = current instanceof PortalNodeSite;
		canDeleteSite = isSiteNode && 
				siteService.allowRemoveSite(((PortalNodeSite)current).getSite().getId());
		isSiteUsedAgain = isSiteNode && 
				phs.getNodesWithSite(((PortalNodeSite)current).getSite().getId()).size() > 1;
		data.put("hasChildren", hasChildren);
		data.put("canDelete", canDelete);
		data.put("canDeleteSite", canDeleteSite);
		data.put("isSiteUsedAgain", isSiteUsedAgain);
		data.put("rootUrl", request.getContextPath()+request.getServletPath());

		model.putAll(data);
	}
	
}
