package org.sakaiproject.hierarchy.tool.vm;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.site.api.SiteService;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class DeleteSiteController extends SimpleFormController {

	private SiteService siteService;
	
	public DeleteSiteController() {
		setCommandClass(DeleteSiteCommand.class);
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void init() {
		
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object object, BindException errors)
			throws Exception {
		PortalHierarchyService phs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		PortalNode node = phs.getCurrentPortalNode();
		DeleteSiteCommand command = (DeleteSiteCommand) object;
		List<PortalNode> nodes = phs.getNodesFromRoot(node.getId());
		String parentPath = nodes.get(nodes.size()-1).getPath();
		try {
			phs.deleteNode(node.getId());
			// Do we want to remove the site?
			if (command.isDeleteSite()) {
				siteService.removeSite(node.getSite());
			}
			Map<String, Object> model = referenceData(request, command, errors);
			
			model.put("siteUrl", ServerConfigurationService.getPortalUrl()+"/hierarchy"+ parentPath);
			
			return new ModelAndView(getSuccessView(), model);
		} catch (IllegalStateException ise) {
			errors.reject("delete.error.children");
			return showForm(request, response, errors);
		}
				
		
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object command,
			Errors errors) throws Exception {
		Map<String, Object> data = VelocityControllerUtils.referenceData(request, command, errors);
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
		canDeleteSite = siteService.allowRemoveSite(current.getSite().getId());
		isSiteUsedAgain = phs.getNodesWithSite(current.getSite().getId()).size() > 1;
		data.put("hasChildren", hasChildren);
		data.put("canDelete", canDelete);
		data.put("canDeleteSite", canDeleteSite);
		data.put("isSiteUsedAgain", isSiteUsedAgain);
		data.put("rootUrl", request.getContextPath()+request.getServletPath());

		return data;
	}
	
}
