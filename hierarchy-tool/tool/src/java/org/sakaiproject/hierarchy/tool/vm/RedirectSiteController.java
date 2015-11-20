package org.sakaiproject.hierarchy.tool.vm;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.PortalNodeComparator;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller("redirectSiteController")
public class RedirectSiteController {
	
	private SessionManager sessionManager;
	private PortalHierarchyService portalHierarchyService;
	private SiteService siteService;
	private VelocityControllerUtils velocityControllerUtils;
	private ServerConfigurationService serverConfigurationService;
	private PortalNodeComparator nodeComparator = new PortalNodeComparator();
	
		
	@Autowired
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService phs) {
		this.portalHierarchyService = phs;
	}
	
	@Autowired
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Autowired
	public void setVelocityControllerUtils(VelocityControllerUtils velocityControllerUtils) {
		this.velocityControllerUtils = velocityControllerUtils;
	}
	
	@Autowired
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	@ModelAttribute
	public void referenceData(HttpServletRequest request, Model model) {
		model.addAllAttributes(velocityControllerUtils.referenceData(request));
		populateModelRedirect(model, request);
	}
	@ModelAttribute("redirect-add")
	public AddRedirectCommand getAddRedirectCommand() {
		AddRedirectCommand command = new AddRedirectCommand();
		// Default is to append path
		command.setAppendPath(true);
		return command;
	}

	@ModelAttribute("redirect-remove")
	public DeleteRedirectCommand getRemoveRedirectCommand() {
		return new DeleteRedirectCommand();
	}
	
	@RequestMapping("/*")
	private ModelAndView displayRedirect(Model model, HttpServletRequest request) {
		return new ModelAndView("addRedirect", model.asMap());
	}
	
	private Model populateModelRedirect(Model model, HttpServletRequest request){
		PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();
		Map<String, Object> site = velocityControllerUtils.createSiteMap(node.getSite());
		site.putAll(velocityControllerUtils.createNodeMap(node));
		model.addAttribute("current", site);
		List<PortalNode> nodeChildren = portalHierarchyService.getNodeChildren(node.getId());

		Collections.sort(nodeChildren, nodeComparator);
		List<Map<String, Object>> redirectNodes = new ArrayList<Map<String, Object>>();
		for (PortalNode nodeChild : nodeChildren) {
			if (nodeChild instanceof PortalNodeRedirect) {
				PortalNodeRedirect redirectNode = (PortalNodeRedirect) nodeChild;
				Map<String, Object> redirectDetails = new HashMap<String, Object>();
				redirectDetails.put("id", redirectNode.getId());
				redirectDetails.put("path", redirectNode.getPath());
				redirectDetails.put("title", redirectNode.getTitle());
				redirectDetails.put("url", redirectNode.getUrl());
				redirectDetails.put("appendPath", Boolean.valueOf(redirectNode.isAppendPath()));
				redirectDetails.put("hidden", Boolean.valueOf(redirectNode.isHidden()));
				redirectNodes.add(redirectDetails);
			}
		}

		model.addAttribute("redirectNodes", redirectNodes);

		model.addAttribute("titleMaxLength", serverConfigurationService.getInt("site.title.maxlength", 20));
		return model;
	}

	@RequestMapping(value = "/redirect/add", method = RequestMethod.POST)
	public String addRedirect(@ModelAttribute("redirect-add") AddRedirectCommand redirect, BindingResult result,
							  ModelMap model) {
		new AddRedirectCommandValidator().validate(redirect, result);
		if (result.hasErrors()) {
			return "addRedirect";
		}
		try {
			String parentId = portalHierarchyService.getCurrentPortalNode().getId();
			String url = redirect.getUrl();
			String serverUrl = serverConfigurationService.getServerUrl();
			// Make the URL relative if we can.
			if (url.toLowerCase().startsWith(serverUrl.toLowerCase())) {
				url = url.substring(serverUrl.length());
				if (!url.startsWith("/")) {
					url = "/" + url;
				}
			}
			portalHierarchyService.newRedirectNode(parentId, redirect.getName(), url,
					redirect.getTitle(), redirect.isAppendPath(), redirect.isHidden());
			return "refresh";
		} catch (IllegalArgumentException iae) {
			result.rejectValue("name", "error.name.exists");
		} catch (PermissionException e) {
			result.reject("error.no.permission");
		}
		return "addRedirect";
	}
	
	@RequestMapping(value = "/redirect/delete", method = RequestMethod.POST)
	public ModelAndView deleteRedirect(@ModelAttribute("redirect-remove") DeleteRedirectCommand command,
								 BindingResult errors, Model model) {
		new DeleteRedirectCommandValidator().validate(command, errors);
		if (errors.hasErrors()) {
			return new ModelAndView("addRedirect", model.asMap());
		}
		try {
			portalHierarchyService.deleteNode(command.getId());
			return new ModelAndView("refresh", model.asMap());
		} catch (IllegalStateException e) {
			throw new RuntimeException("Redirects should never have children so shouldn't see this exception.", e);
		} catch (PermissionException e) {
			errors.rejectValue("id", "error.no.permission");
		}
		return new ModelAndView("addRedirect", model.asMap());
	}
	
	@RequestMapping(value = "/cancel", method = RequestMethod.POST)
	public String doCancelAction(ModelMap model) throws Exception {
		PortalNode node = portalHierarchyService.getCurrentPortalNode();
		model.put("siteUrl", serverConfigurationService.getPortalUrl() + "/hierarchy" + node.getPath());
		return "redirect";
	}

}
