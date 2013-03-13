package org.sakaiproject.hierarchy.tool.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.hierarchy.tool.vm.AddRedirectController.AddRedirectCommand;
import org.sakaiproject.hierarchy.tool.vm.AddRedirectController.AddRedirectCommandValidator;
import org.sakaiproject.hierarchy.tool.vm.DeleteRedirectController.DeleteRedirectCommand;
import org.sakaiproject.hierarchy.tool.vm.DeleteRedirectController.DeleteRedirectCommandValidator;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.TargettedController;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@TargettedController("sakai.hierarchy-manager")
public class ManagerController{

	private static final Log log = LogFactory.getLog(ManagerController.class);


	static final String ACT_SAVE_SITE = "act_savesite";

	static final String ACT_SETPROPERTY = "act_setproperty";

	static final String ACT_SELECT_SITE = "act_selectsite";

	static final String ACT_CUT = "act_cut";

	static final String ACT_PASTE = "act_paste";

	static final String ACT_CANCEL = "act_cancel";

	static final String ACT_NEWREDIRECT = "act_newredirect";

	static final String ACT_DELETEREDIRECT = "act_deleteredirect";

	static final String REQUEST_ACTION = "_action";

	static final String REQUEST_SITES = "_sites";

	static final String REQUEST_SITE = "_site";

	private static final String CUT_ID = ManagerController.class.getName()
			+ "#CUT_ID";

	private SessionManager sessionManager;
	private PortalHierarchyService portalHierarchyService;
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService phs) {
		this.portalHierarchyService = phs;
	}

	@Autowired
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	@ModelAttribute("redirect-add")
	public AddRedirectCommand getAddRedirectCommand() {
		return new AddRedirectCommand();
	}

	@ModelAttribute("redirect-remove")
	public DeleteRedirectCommand getRemoveRedirectCommand() {
		return new DeleteRedirectCommand();
	}

	public ManagerController() {
		super();
	}


	@Autowired
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@PostConstruct
	public void init() {
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

	protected StringBuilder buildUrl(HttpServletRequest request, String pathInfo) {
		StringBuilder doneUrl = new StringBuilder();
		if (request.getContextPath() != null)
			doneUrl.append(request.getContextPath());
		if (request.getServletPath() != null)
			doneUrl.append(request.getServletPath());
		if (pathInfo != null)
			doneUrl.append(pathInfo);
		return doneUrl;
	}

	protected void populateSite(Map<String, Object> editModel,
			PortalNodeSite node) {
		Map<String, Object> site = createSiteMap(node.getSite());
		site.putAll(createNodeMap(node));

		List<PortalNode> nodes = portalHierarchyService.getNodesWithSite(node
				.getSite().getId());
		List<Map<String, Object>> paths = new ArrayList<Map<String, Object>>(
				nodes.size());
		for (PortalNode currentNode : nodes) {
			if (!node.getPath().equals(currentNode.getPath()))
				paths.add(createNodeMap(currentNode));
		}
		editModel.put("other", paths);
		editModel.put("current", site);
	}

	protected Map<String, Object> createSiteMap(Site site) {
		Map<String, Object> siteMap = new HashMap<String, Object>();
		siteMap.put("siteTitle", site.getTitle());
		siteMap.put("siteId", site.getId());
		siteMap.put("siteShortDescription", site.getShortDescription());
		// TODO Should also add contact.
		return siteMap;
	}

	protected Map<String, Object> createNodeMap(PortalNode node) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nodePath", node.getPath());
		map.put("nodeId", node.getId());
		map.put("nodeUrl", serverConfigurationService.getPortalUrl()
				+ "/hierarchy" + node.getPath());
		return map;
	}

	@ModelAttribute
	public void referenceData(HttpServletRequest request, ModelMap model) {
		populateModel(model, request);
	}

	protected void populateModel(Map<String, Object> model,
			HttpServletRequest request) {
		model.put("sakai_fragment", "false");
		model.put("sakai_head",
				(String) request.getAttribute("sakai.html.head"));
		model.put("sakai_onload",
				(String) request.getAttribute("sakai.html.body.onload"));

		model.put("toolTitle", "Hierarchy Manager");
		String editor = serverConfigurationService.getString("wysiwyg.editor");
		model.put("sakai_editor", editor);
		model.put("sakai_library_path", "/library/");
		model.put("titleMaxLength",
				serverConfigurationService.getInt("site.title.maxlength", 25));

		model.put("rootUrl",
				request.getContextPath() + request.getServletPath());

		PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();

		Map<String, Object> site = createSiteMap(node.getSite());
		site.putAll(createNodeMap(node));
		model.put("current", site);

		List<PortalNode> nodes = portalHierarchyService.getNodesWithSite(node
				.getSite().getId());
		List<Map<String, Object>> paths = new ArrayList<Map<String, Object>>(
				nodes.size());
		for (PortalNode currentNode : nodes) {
			if (!node.getPath().equals(currentNode.getPath()))
				paths.add(createNodeMap(currentNode));
		}
		model.put("other", paths);

		model.put("canDelete",
				portalHierarchyService.canDeleteNode(node.getId()));
		model.put("canMove", portalHierarchyService.canMoveNode(node.getId()));
		model.put("canReplace",
				portalHierarchyService.canChangeSite(node.getId()));
		// Need to list the redirect nodes.
		List<PortalNode> nodeChildren = portalHierarchyService
				.getNodeChildren(node.getId());
		List<Map<String, String>> redirectNodes = new ArrayList<Map<String, String>>();
		for (PortalNode nodeChild : nodeChildren) {
			if (nodeChild instanceof PortalNodeRedirect) {
				PortalNodeRedirect redirectNode = (PortalNodeRedirect) nodeChild;
				Map<String, String> redirectDetails = new HashMap<String, String>();
				redirectDetails.put("id", redirectNode.getId());
				redirectDetails.put("path", redirectNode.getPath());
				redirectDetails.put("title", redirectNode.getTitle());
				redirectDetails.put("url", redirectNode.getUrl());
				redirectDetails.put("appendPath",
						redirectNode.isAppendPath() ? "true" : null);
				redirectNodes.add(redirectDetails);
			}
		}
		model.put("redirectNodes", redirectNodes);

	}

	@RequestMapping(value = "/redirect/add", method = RequestMethod.POST)
	public String addRedirect(
			@ModelAttribute("redirect-add") AddRedirectCommand redirect,
			BindingResult result, ModelMap model) {
		new AddRedirectCommandValidator().validate(redirect, result);
		if (result.hasErrors()) {
			return "show";
		}
		try {
			String parentId = portalHierarchyService.getCurrentPortalNode()
					.getId();
			portalHierarchyService.newRedirectNode(parentId,
					redirect.getName(), redirect.getUrl(), redirect.getTitle(),
					redirect.isAppendPath());
			return "refresh";
		} catch (IllegalArgumentException iae) {
			result.rejectValue("name", "error.name.exists");
		} catch (PermissionException e) {
			result.reject("error.no.permission");
		}
		return "show";
	}

	@RequestMapping(value = "/redirect/delete", method = RequestMethod.POST)
	public String deleteRedirect(
			@ModelAttribute("redirect-remove") DeleteRedirectCommand command,
			BindingResult errors, ModelMap model) {
		new DeleteRedirectCommandValidator().validate(command, errors);
		if (errors.hasErrors()) {
			return "show";
		}
		try {
			portalHierarchyService.deleteNode(command.getId());
			return "refresh";
		} catch (IllegalStateException e) {
			throw new RuntimeException(
					"Redirects should never have children so shouldn't see this exception.",
					e);
		} catch (PermissionException e) {
			errors.rejectValue("id", "error.no.permission");
		}
		return "show";
	}
	
	@RequestMapping(value = "/site/change", method = RequestMethod.POST)
	public ModelAndView changeSite(HttpServletRequest request) {
	    ToolSession toolSession = sessionManager.getCurrentToolSession();

	    toolSession.setAttribute(Tool.HELPER_DONE_URL, buildUrl(request, "/site/save"));
	    toolSession.setAttribute(SiteHelper.SITE_PICKER_PERMISSION,
	            org.sakaiproject.site.api.SiteService.SelectionType.UPDATE);

	    // Go to the helper
	    RedirectView redirectView = new RedirectView("/sites", true);
	    // We don't want to pass through all the model data.
	    redirectView.setExposeModelAttributes(false);
	    return new ModelAndView(redirectView);
	}
	
    // We throw an exception (MissingServletRequestParameterException) when siteId isn't present
    @RequestMapping(value = "/site/save", method = RequestMethod.GET)
    public String saveSite(HttpServletRequest request, @RequestParam(REQUEST_SITE) String siteId) {
        PortalNode node = portalHierarchyService.getCurrentPortalNode();

        if (siteId != null && siteId.length() > 0) {
            try {
                portalHierarchyService.changeSite(node.getId(), siteId);
            } catch (PermissionException e) {
                // error? we have a redirect so will anything we do persist?
                throw new IllegalStateException(
                        "You shouldn't have been able to select a site as you don't have permission.", e);
            }
        }
        return "refresh";
    }
	
	@RequestMapping("/*")
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String currentPath = request.getPathInfo();
		PortalNodeSite node = getCurrentNode(currentPath);

		Session session = sessionManager.getCurrentSession();
		String cutId = (String) session.getAttribute(ManagerController.CUT_ID);

		Map<String, Object> model = new HashMap<String, Object>();
		populateModel(model, request);

		boolean topRefresh = false;

		String action = request.getParameter(REQUEST_ACTION);
		if (ACT_CUT.equals(action)) {
			cutId = node.getId();
			session.setAttribute(ManagerController.CUT_ID, cutId);
		} else if (ACT_PASTE.equals(action)) {
			if (cutId != null) {
				portalHierarchyService.moveNode(cutId, node.getId());
				session.removeAttribute(ManagerController.CUT_ID);
				cutId = null;
				topRefresh = true;
			}
		} else if (ACT_CANCEL.equals(action)) {
			cutId = null;
			session.removeAttribute(ManagerController.CUT_ID);
		}

		Map<String, Object> showModel = new HashMap<String, Object>();
		populateModel(showModel, request);
		populateSite(showModel, node);
		showModel.put("topRefresh", topRefresh);
		if (cutId != null) {
			showModel.put("cutId", cutId);
			PortalNode cutNode = portalHierarchyService.getNodeById(cutId);
			showModel.put("cutChild",
					node.getPath().startsWith(cutNode.getPath()));
			showModel.put("cutNode", cutNode);
			return new ModelAndView("cut", showModel);
		} else {

			return new ModelAndView("show", showModel);
		}
	}

}
