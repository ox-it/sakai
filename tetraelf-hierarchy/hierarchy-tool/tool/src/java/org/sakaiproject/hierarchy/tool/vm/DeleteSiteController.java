package org.sakaiproject.hierarchy.tool.vm;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
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
import org.springframework.web.servlet.mvc.annotation.TargettedController;

@Controller("deleteSiteController")
@RequestMapping("/delete")
public class DeleteSiteController {

	private SiteService siteService;
	private PortalHierarchyService portalHierarchyService;
	private VelocityControllerUtils velocityControllerUtils;
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService portalHierarchyService) {
		this.portalHierarchyService = portalHierarchyService;
	}

	@Autowired
	public void setVelocityControllerUtils(VelocityControllerUtils velocityControllerUtils) {
		this.velocityControllerUtils = velocityControllerUtils;
	}

	@Autowired
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
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

	@RequestMapping(value = "/remove/site", method = RequestMethod.POST)
	public String doSubmitAction(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute("command") DeleteSiteCommand object, BindingResult result, ModelMap model) throws Exception {
		PortalNode node = portalHierarchyService.getCurrentPortalNode();
		DeleteSiteCommand command = (DeleteSiteCommand) object;
		List<PortalNodeSite> nodes = portalHierarchyService.getNodesFromRoot(node.getId());
		String parentUrl = nodes.get(nodes.size() - 1).getSite().getUrl();
		try {
			portalHierarchyService.deleteNode(node.getId());
			// Do we want to remove the site?
			if (command.isDeleteSite() && node instanceof PortalNodeSite) {
				siteService.removeSite(((PortalNodeSite) node).getSite());
			}

			model.put("siteUrl", parentUrl);

			return "redirect";
		} catch (IllegalStateException ise) {
			result.reject("delete.error.children");
			return showForm();
		}

	}

	//Adding cancel feature into the Remove Site screen
	@RequestMapping(value = "/cancel/remove", method = RequestMethod.POST)
	public String doCancelAction(ModelMap model) throws Exception {
		PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();
		model.put("siteUrl", node.getSite().getUrl());
		return "redirect";
	}

	@ModelAttribute
	public void referenceData(HttpServletRequest request, ModelMap model) {
		Map<String, Object> data = velocityControllerUtils.referenceData(request);
		PortalNode current = portalHierarchyService.getCurrentPortalNode();
		boolean canDelete = true;
		boolean canDeleteSite = false;
		boolean hasChildren = false;
		boolean isSiteUsedAgain = false;
		if (current != null) {
			List<PortalNode> children = portalHierarchyService.getNodeChildren(current.getId());
			hasChildren = children.size() > 0;
		}
		canDelete = portalHierarchyService.canDeleteNode(current.getId());
		boolean isSiteNode = current instanceof PortalNodeSite;
		String siteId = ((PortalNodeSite) current).getSite().getId();
		canDeleteSite = isSiteNode && siteService.allowRemoveSite(siteId);
		isSiteUsedAgain = isSiteNode
				&& portalHierarchyService.getNodesWithSite(siteId).size() > 1;
		data.put("hasChildren", hasChildren);
		data.put("canDelete", canDelete);
		data.put("canDeleteSite", canDeleteSite);
		data.put("isSiteUsedAgain", isSiteUsedAgain);
		data.put("rootUrl", request.getContextPath() + request.getServletPath());

		model.putAll(data);
	}

}
