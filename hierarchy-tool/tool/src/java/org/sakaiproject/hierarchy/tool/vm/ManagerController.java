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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ManagerController
{

	private static final Log log = LogFactory.getLog(ManagerController.class);
	
	static final String ACT_EDIT_SITE = "act_editsite";
	
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

	private static final String CUT_ID = ManagerController.class.getName() + "#CUT_ID";

	private PortalHierarchyService portalHierarchyService;
	private SiteService siteService;
	private SessionManager sessionManager;
	private ServerConfigurationService serverConfigurationService;

	private int titleMaxLength;

	public ManagerController()
	{
		super();
	}
	
	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService phs)
	{
		this.portalHierarchyService = phs;
	}
	
	@Autowired
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}
	
	@Autowired
	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	@Autowired
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}
	
	@Autowired
	public void setTitleMaxLength(int titleMaxLength)
	{
		this.titleMaxLength = titleMaxLength;
	}

	@PostConstruct
	public void init()
	{
	}
	
	@ModelAttribute("remove")
	public RemoveRedirectCommand getRemoveRedirectCommand() {
		return new RemoveRedirectCommand();
	}
	
	@ModelAttribute("command")
	public AddRedirectCommand getAddRedirectComment() {
		return new AddRedirectCommand();
	}
	
	@RequestMapping("/redirect/delete")
	public String deleteRedirect(@ModelAttribute("remove") RemoveRedirectCommand command, BindingResult errors) {
		try {
			portalHierarchyService.deleteNode(command.getRedirectId());
		} catch (IllegalStateException e) {
			throw new RuntimeException("Redirects should never have children so shouldn't see this exception.", e);
		} catch (PermissionException e) {
			errors.reject("error.no.permission");
		}
		errors.getModel().put("topRefresh", true);
		return "show";
	}

	@RequestMapping("/*")
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception
			{
		
		String currentPath = request.getPathInfo();
		PortalNodeSite node = getCurrentNode(currentPath);
		
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		Session session = sessionManager.getCurrentSession();
		String cutId = (String) session.getAttribute(ManagerController.CUT_ID);


		Map<String, Object> model = new HashMap<String, Object>();
		populateModel(model,request);


		boolean topRefresh = false;

		String action = request.getParameter(REQUEST_ACTION);
		if (ACT_SELECT_SITE.equals(action))
		{
			toolSession.setAttribute(Tool.HELPER_DONE_URL, buildUrl(request) +"?"+REQUEST_ACTION+"="+ACT_EDIT_SITE);
			toolSession.setAttribute(SiteHelper.SITE_PICKER_PERMISSION, org.sakaiproject.site.api.SiteService.SelectionType.UPDATE);

			return new ModelAndView(new RedirectView("/sites", true), null);
		}
		else if (ACT_EDIT_SITE.equals(action))
		{
			Map<String, Object> editModel = new HashMap<String, Object>();
			populateModel(editModel, request);
			editModel.put("old", createSiteMap(node.getSite()));
			// Check to see if the user has come back from helper.
			Object siteAttribute = toolSession.getAttribute(SiteHelper.SITE_PICKER_SITE_ID);
			toolSession.removeAttribute(SiteHelper.SITE_PICKER_SITE_ID);
			if ( siteAttribute instanceof String) 
			{
				try
				{
					Site site = siteService.getSite((String)siteAttribute);
					editModel.put("new", createSiteMap(site));
					return new ModelAndView( "replace", editModel );
				}
				catch (IdUnusedException iue)
				{
					log.warn("Couldn't find site returned by helper: "+ siteAttribute);
					// TODO Display message to user.
				}
			}
		}
		else if (ACT_SAVE_SITE.equals(action))
		{
			String siteId = request.getParameter(REQUEST_SITE);

			if (siteId != null && siteId.length() > 0)
			{
				portalHierarchyService.changeSite(node.getId(), siteId);
				topRefresh = true;
			}
		}
		else if (ACT_CUT.equals(action))
		{
			cutId = node.getId();
			session.setAttribute(ManagerController.CUT_ID, cutId);
		}
		else if (ACT_PASTE.equals(action))
		{
			if (cutId != null)
			{
				portalHierarchyService.moveNode(cutId, node.getId());
				session.removeAttribute(ManagerController.CUT_ID);
				cutId = null;
				topRefresh = true;
			}
		}
		else if (ACT_CANCEL.equals(action))
		{
			cutId = null;
			session.removeAttribute(ManagerController.CUT_ID);
		}
		else if (ACT_NEWREDIRECT.equals(action))
		{
			// TODO Validation
			String redirectUrl = request.getParameter("url");
			String redirectTitle = request.getParameter("title");
			String path = request.getParameter("path");
			boolean appendPath = Boolean.valueOf(request.getParameter("appendPath"));
			
			portalHierarchyService.newRedirectNode(node.getId(), path, redirectUrl, redirectTitle, appendPath);
			topRefresh = true;
		}
		else if (ACT_DELETEREDIRECT.equals(action))
		{
			String nodeId = request.getParameter("redirectId");
			portalHierarchyService.deleteNode(nodeId);
			topRefresh = true;
		}
		
		Map<String, Object> showModel = new HashMap<String, Object>();
		// This is so that we have one to show.
		showModel.put("command", new AddRedirectCommand());
		populateModel(showModel, request);
		populateSite(showModel, node);
		showModel.put("topRefresh", topRefresh);
		if (cutId != null)
		{
			showModel.put("cutId", cutId);
			PortalNode cutNode = portalHierarchyService.getNodeById(cutId);
			showModel.put("cutChild", node.getPath().startsWith(cutNode.getPath()));
			showModel.put("cutNode", cutNode);
			return new ModelAndView( "cut", showModel);
		}
		else
		{
	
			
			return new ModelAndView("show", showModel);
		}
	}

	private PortalNodeSite getCurrentNode(String currentPath) {
		PortalNode node = null;
		if (currentPath != null && currentPath.length() > 0)
		{
			node = portalHierarchyService.getNode(currentPath);
		}
		if (node == null)
		{
			node = portalHierarchyService.getCurrentPortalNode();
			if ( node == null ) {
				node = portalHierarchyService.getNode(null);
			}
		}
		if (node instanceof PortalNodeSite) { 
			return (PortalNodeSite)node;
		}
		throw new IllegalStateException("You can't manage a non site node");
	}

	private StringBuilder buildUrl(HttpServletRequest request) {
		StringBuilder doneUrl = new StringBuilder();
		if (request.getContextPath() != null) doneUrl.append(request.getContextPath());
		if (request.getServletPath() != null) doneUrl.append(request.getServletPath());
		if (request.getPathInfo() != null) doneUrl.append(request.getPathInfo());
		return doneUrl;
	}

	private void populateSite(Map<String, Object> editModel, PortalNodeSite node) {
			Map<String,Object> site = createSiteMap(node.getSite());
			site.putAll(createNodeMap(node));
			
			List<PortalNode> nodes = portalHierarchyService.getNodesWithSite(node.getSite().getId());
			List<Map<String, Object>> paths = new ArrayList<Map<String, Object>>(nodes.size());
			for (PortalNode currentNode: nodes) {
				if (!node.getPath().equals(currentNode.getPath()))
					paths.add(createNodeMap(currentNode));
			}
			editModel.put("other", paths);
			editModel.put("current", site);
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
		map.put("nodeUrl",serverConfigurationService.getPortalUrl()+"/hierarchy"+ node.getPath());
		return map;
	}
	
	@ModelAttribute
	public void referenceData(HttpServletRequest request, ModelMap model) {
		populateModel(model, request);
	}

	private void populateModel(Map<String, Object> model, HttpServletRequest request)
	{
		model.put("sakai_fragment","false");
		model.put("sakai_head", (String) request
				.getAttribute("sakai.html.head"));
		model.put("sakai_onload", (String) request
				.getAttribute("sakai.html.body.onload"));

		model.put("toolTitle", "Hierarchy Manager");
		String editor = serverConfigurationService.getString("wysiwyg.editor");
		model.put("sakai_editor", editor);
		model.put("sakai_library_path", "/library/");
		model.put("titleMaxLength", titleMaxLength);

		model.put("rootUrl", request.getContextPath()+request.getServletPath());
		
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
	
	public static class RemoveRedirectCommand {
		private String redirectId;

		public String getRedirectId() {
			return redirectId;
		}

		public void setRedirectId(String redirectId) {
			this.redirectId = redirectId;
		}
	}

}
