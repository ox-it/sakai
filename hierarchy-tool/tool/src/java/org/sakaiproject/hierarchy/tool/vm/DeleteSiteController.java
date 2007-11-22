package org.sakaiproject.hierarchy.tool.vm;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.springframework.validation.BindException;
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
		Hierarchy node = phs.getCurrentPortalNode();
		String parentPath = node.getParent().getPath();
		phs.begin();
		phs.deleteNode(node);
		phs.end();
				
		Map model = new HashMap();
		model.put("siteUrl", ServerConfigurationService.getPortalUrl()+"/hierarchy"+ parentPath);
		
		return new ModelAndView(getSuccessView(), model);
	}
	
}
