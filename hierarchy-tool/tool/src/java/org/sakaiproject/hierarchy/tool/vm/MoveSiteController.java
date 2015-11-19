package org.sakaiproject.hierarchy.tool.vm;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.hierarchy.tool.vm.VelocityControllerUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller("moveSiteController")
public class MoveSiteController {
	
	private SessionManager sessionManager;
	private PortalHierarchyService portalHierarchyService;
	private ServerConfigurationService serverConfigurationService;
	private VelocityControllerUtils velocityControllerUtils;
	static final String CUT_ID = "#CUT_ID";
	
		
	@Autowired
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService phs) {
		this.portalHierarchyService = phs;
	}

	@Autowired
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	@Autowired
	public void setVelocityControllerUtils(VelocityControllerUtils velocityControllerUtils) {
		this.velocityControllerUtils = velocityControllerUtils;
	}
	
	@ModelAttribute
	public void referenceData(HttpServletRequest request, Model model) {
		model.addAllAttributes(velocityControllerUtils.referenceData(request));
	}
	
	@RequestMapping("/*")
	protected ModelAndView handleAllRequests(HttpServletRequest request, Model model) {
		if(sessionManager.getCurrentSession().getAttribute(CUT_ID) != null){
			return new ModelAndView("cut",populateModelCut(model).asMap());
		}
		model.addAttribute("node", portalHierarchyService.getCurrentPortalNode());
		return new ModelAndView("move", model.asMap());
		
	}

	@RequestMapping(value = "/cut", method = RequestMethod.POST)
	public ModelAndView cutSite(HttpServletRequest request, Model model) {
		Session session = sessionManager.getCurrentSession();
		session.setAttribute(CUT_ID, getCurrentNode(request.getPathInfo()).getId());
		// Have to update the model as we've set the cut-id now.
		return new ModelAndView("cut", populateModelCut(model).asMap());
	}

	@RequestMapping(value = "/cancel", method = RequestMethod.POST)
	public ModelAndView cancel(HttpServletRequest request, Model model) {
		Session session = sessionManager.getCurrentSession();
		session.removeAttribute(CUT_ID);
		return handleAllRequests(request, model);
	}

	@RequestMapping(value = "/cancel/move", method = RequestMethod.POST)
	public ModelAndView displayHome(Model model) {
		PortalNode node = portalHierarchyService.getCurrentPortalNode();
		model.addAttribute("siteUrl", serverConfigurationService.getPortalUrl() + "/hierarchy" + node.getPath());
		return new ModelAndView("redirect", populateModelCut(model).asMap());
	}
	
	@RequestMapping(value = "/paste", method = RequestMethod.POST)
	public ModelAndView pasteSite(HttpServletRequest request, Model model) {
		Session session = sessionManager.getCurrentSession();
		String cutId = (String) session.getAttribute(CUT_ID);
		PortalNodeSite node = getCurrentNode(null);
		try {
			portalHierarchyService.moveNode(cutId, node.getId());
		} catch (PermissionException e) {
			// Get nice name of entity on which we failed.
			model.addAttribute("errorMessage", new DefaultMessageSourceResolvable(
					new String[]{"error.no.permission.for"}, new Object[]{e.getResource()}, null));
			return new ModelAndView("cut", populateModelCut(model).asMap());
		} catch (Exception exception){
			model.addAttribute("errorMessage", new String[]{"error.general"});
			return new ModelAndView("cut", populateModelCut(model).asMap());			
		}
		session.removeAttribute(MoveSiteController.CUT_ID);
		model.addAttribute("siteUrl", serverConfigurationService.getPortalUrl() + "/hierarchy" + node.getPath());
		return new ModelAndView("redirect", model.asMap());
	}
			
	private Model populateModelCut(Model model) {
		PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();
		Map<String, Object> site = new HashMap<String, Object>();
		site.putAll(velocityControllerUtils.createNodeMap(node));
		model.addAttribute("current", site);
		Session session = sessionManager.getCurrentSession();
		String cutId = (String) session.getAttribute(MoveSiteController.CUT_ID);

		if (cutId != null) {
			PortalNode cutNode = portalHierarchyService.getNodeById(cutId);
			if (cutNode != null) {
				model.addAttribute("cutId", cutId);
				model.addAttribute("cutChild", node.getPath().startsWith(cutNode.getPath()));
				model.addAttribute("cutNode", cutNode);
			}
		}
		return model;
	}
	
	private PortalNodeSite getCurrentNode(String currentPath) {
		PortalNode node = null;
		if (currentPath != null && currentPath.length() > 0) {
			node = portalHierarchyService.getNode(currentPath);
		}
		if (node == null) {
			node = portalHierarchyService.getCurrentPortalNode();
			if (node == null) {
				node = portalHierarchyService.getNode(null);
			}
		}
		if (node instanceof PortalNodeSite) {
			return (PortalNodeSite) node;
		}
		throw new IllegalStateException("You can't manage a non site node");
	}

}
