package org.sakaiproject.hierarchy.tool.vm;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class DeleteSiteController extends SimpleFormController {

	public DeleteSiteController() {
		setCommandClass(Object.class);
	}

	public void init() {
		
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		PortalHierarchyService phs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		PortalNode node = phs.getCurrentPortalNode();
		List<PortalNode> nodes = phs.getNodesFromRoot(node.getId());
		String parentPath = nodes.get(nodes.size()-1).getPath();
		try {
			phs.deleteNode(node.getId());
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
		boolean hasChildren = false;
		if (current != null) {
			List<PortalNode> children = phs.getNodeChildren(current.getId());
			hasChildren = children.size() > 0;
		}
		canDelete = phs.canDeleteNode(current.getId());
		data.put("hasChildren", hasChildren);
		data.put("canDelete", canDelete);
		data.put("rootUrl", request.getContextPath()+request.getServletPath());

		return data;
	}
	
}
