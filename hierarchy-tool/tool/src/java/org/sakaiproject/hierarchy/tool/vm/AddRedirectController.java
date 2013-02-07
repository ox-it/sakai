package org.sakaiproject.hierarchy.tool.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This class handles the add a redirect. Both the form and success view take
 * you back to the main page.
 */
@Controller
@RequestMapping("/redirect/add")
public class AddRedirectController {

	private PortalHierarchyService portalHierarchyService;
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService portalHierarchyService) {
		this.portalHierarchyService = portalHierarchyService;
	}

	@Autowired
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	@ModelAttribute("command")
	public AddRedirectCommand getAddRedirectCommand() {
		return new AddRedirectCommand();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showForm() {
		return "show";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String doSubmitAction(@ModelAttribute("command") AddRedirectCommand redirect, BindingResult result,
			ModelMap model) throws Exception {
		new AddRedirectCommandValidator().validate(redirect, result);
		if (result.hasErrors()) {
			return "show";
		}
		try {
			String parentId = portalHierarchyService.getCurrentPortalNode().getId();
			portalHierarchyService.newRedirectNode(parentId, redirect.getName(), redirect.getUrl(),
					redirect.getTitle(), redirect.isAppendPath());
			// Make sure we refresh the frameset.
			model.put("topRefresh", true);
		} catch (IllegalArgumentException iae) {
			result.rejectValue("name", "error.name.exists");
		}

		return "show";
	}

	/**
	 * This loads the reference data for the page. It's flagged a ModelAttribute
	 * so to gets called before the request mapped method. We get passed the
	 * full model so we can add multiple bits of data to it.
	 * 
	 * @param request
	 *            The servlet request
	 * @param model
	 *            The Model for the page.
	 */
	@ModelAttribute
	public void referenceData(HttpServletRequest request, ModelMap model) {

		// Put in the generic sakai stuff.
		model.put("sakai_fragment", "false");
		model.put("sakai_head", (String) request.getAttribute("sakai.html.head"));
		model.put("sakai_onload", (String) request.getAttribute("sakai.html.body.onload"));
		model.put("toolTitle", "Hierarchy Manager");
		String editor = serverConfigurationService.getString("wysiwyg.editor");
		model.put("sakai_editor", editor);
		model.put("sakai_library_path", "/library/");
		model.put("rootUrl", request.getContextPath() + request.getServletPath());

		PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();

		Map<String, Object> site = createSiteMap(node.getSite());
		site.putAll(createNodeMap(node));
		model.put("current", site);

		List<PortalNode> nodes = portalHierarchyService.getNodesWithSite(node.getSite().getId());
		List<Map<String, Object>> paths = new ArrayList<Map<String, Object>>(nodes.size());
		for (PortalNode currentNode : nodes) {
			if (!node.getPath().equals(currentNode.getPath()))
				paths.add(createNodeMap(currentNode));
		}
		model.put("other", paths);

		model.put("canDelete", portalHierarchyService.canDeleteNode(node.getId()));
		model.put("canMove", portalHierarchyService.canMoveNode(node.getId()));
		model.put("canReplace", portalHierarchyService.canChangeSite(node.getId()));
		// Need to list the redirect nodes.
		List<PortalNode> nodeChildren = portalHierarchyService.getNodeChildren(node.getId());
		List<Map<String, String>> redirectNodes = new ArrayList<Map<String, String>>();
		for (PortalNode nodeChild : nodeChildren) {
			if (nodeChild instanceof PortalNodeRedirect) {
				PortalNodeRedirect redirectNode = (PortalNodeRedirect) nodeChild;
				Map<String, String> redirectDetails = new HashMap<String, String>();
				redirectDetails.put("id", redirectNode.getId());
				redirectDetails.put("path", redirectNode.getPath());
				redirectDetails.put("title", redirectNode.getTitle());
				redirectDetails.put("url", redirectNode.getUrl());
				redirectDetails.put("appendPath", redirectNode.isAppendPath() ? "true" : null);
				redirectNodes.add(redirectDetails);
			}
		}
		model.put("redirectNodes", redirectNodes);

	}

	private Map<String, Object> createSiteMap(Site site) {
		Map<String, Object> siteMap = new HashMap<String, Object>();
		siteMap.put("siteTitle", site.getTitle());
		siteMap.put("siteId", site.getId());
		siteMap.put("siteShortDescription", site.getShortDescription());
		// TODO Should also add contact.
		return siteMap;
	}

	private Map<String, Object> createNodeMap(PortalNode node) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nodePath", node.getPath());
		map.put("nodeId", node.getId());
		map.put("nodeUrl", serverConfigurationService.getPortalUrl() + "/hierarchy" + node.getPath());
		return map;
	}

	/**
	 * Command class for adding a new redirect.
	 */
	public static class AddRedirectCommand {
		private String name;
		private String title;
		private String url;
		private boolean appendPath;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public boolean isAppendPath() {
			return appendPath;
		}

		public void setAppendPath(boolean appendPath) {
			this.appendPath = appendPath;
		}
	}

	/**
	 * Validator for adding a redirect.
	 */
	public static class AddRedirectCommandValidator extends BaseSiteValidator {

		@SuppressWarnings("rawtypes")
		public boolean supports(Class clazz) {
			return AddRedirectCommand.class.isAssignableFrom(clazz);
		}

		public void validate(Object target, Errors errors) {
			if (target instanceof AddRedirectCommand) {
				AddRedirectCommand command = (AddRedirectCommand) target;
				checkName(errors, command.getName());
				checkTitle(errors, command.getTitle());
				ValidationUtils.rejectIfEmpty(errors, "url", "validator.url.empty");
			}
		}

	}
}
