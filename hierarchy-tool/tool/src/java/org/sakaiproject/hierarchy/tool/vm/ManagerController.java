package org.sakaiproject.hierarchy.tool.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.cover.PortalHierarchyService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class ManagerController extends AbstractController
{

	private static final Log log = LogFactory.getLog(ManagerController.class);

	public static final String ACT_ADDNODE = "act_addnode";

	public static final String ACT_SITELIST = "act_sitelist";
	
	public static final String ACT_ADDSITE = "act_addsite";
	
	public static final String ACT_DELSITE = "act_delsite";
	
	public static final String ACT_EDIT_SITE = "act_editsite";
	
	public static final String ACT_SAVE_SITE = "act_savesite";
	
	public static final String ACT_SHOW_SITE = "act_showsite";
	
	public static final String ACT_NEW_SITE = "act_newsite";

	public static final String ACT_SETPROPERTY = "act_setproperty";

	public static final String ACT_CLEARPROPERTY = "act_clearproperty";
	
	public static final String ACT_SELECT_SITE = "act_selectsite";

	public static final String REQUEST_ACTION = "_action";

	public static final String REQUEST_SITES = "_sites";
	
	public static final String REQUEST_SITE = "_site";

	public static final String REQUEST_NEWNODE = "_newnode";

	public static final String REQUEST_PROPERTY = "_property";

	public static final String REQUEST_VALUE = "_value";


	public ManagerController()
	{
		super();
	}

	public void init()
	{
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{

		PortalNode node = null;
		String currentPath = request.getPathInfo();
		if (currentPath != null && currentPath.length() > 0)
		{
			node = PortalHierarchyService.getInstance().getNode(currentPath);
		}
		if (node == null)
		{
			node = PortalHierarchyService.getInstance().getCurrentPortalNode();
			if ( node == null ) {
				node = PortalHierarchyService.getInstance().getNode(null);
			}
		}
		Placement p = ToolManager.getCurrentPlacement();
		String siteContext = (p != null)?p.getId(): "";
		Map<String, Object> model = new HashMap<String, Object>();
		populateModel(model,request);

		if (node == null)
		{
			return new ModelAndView("nonode", model);
		}
		else
		{
			String action = request.getParameter(REQUEST_ACTION);
			boolean modified = false;
			if (ACT_SELECT_SITE.equals(action))
			{
				ToolSession toolSession = SessionManager.getCurrentToolSession();
				
				toolSession.setAttribute(Tool.HELPER_DONE_URL, buildUrl(request) +"?"+REQUEST_ACTION+"="+ACT_EDIT_SITE);
				toolSession.setAttribute(SiteHelper.SITE_PICKER_PERMISSION, org.sakaiproject.site.api.SiteService.SelectionType.UPDATE);
				
				return new ModelAndView(new RedirectView("/sites", true), null);
			}
			else if (ACT_EDIT_SITE.equals(action))
			{
				Map<String, Object> editModel = new HashMap<String, Object>();
				editModel.put("updating", Boolean.TRUE);
				populateModel(editModel, request);
				populateNode(node, editModel);
				
				// Check to see if the user has come back from helper.
				ToolSession toolSession = SessionManager.getCurrentToolSession();
				Object siteAttribute = toolSession.getAttribute(SiteHelper.SITE_PICKER_SITE_ID); 
				if ( siteAttribute instanceof String) 
				{
					try
					{
						Site site = SiteService.getSite((String)siteAttribute);
						editModel.put("site", createSiteMap(site));
					} catch (IdUnusedException iue) {
						log.warn("Couldn't find site returned by helper: "+ siteAttribute);
					}
				}
				if (editModel.get("site") == null)
				{
					populateSite(editModel, node);
				}

				return new ModelAndView( "show", editModel );
			}
			else if (ACT_SAVE_SITE.equals(action))
			{
				String siteId = request.getParameter(REQUEST_SITE);
				if (siteId != null && siteId.length() > 0)
				{
					PortalHierarchyService.getInstance().changeSite(node.getId(), siteId);
				}
			}
			Map<String, Object> showModel = new HashMap<String, Object>();
			populateModel(showModel, request);
			populateSite(showModel, node);
			populateNode(node, showModel);

			return new ModelAndView( "show", showModel);
		}
	}

	private StringBuilder buildUrl(HttpServletRequest request) {
		StringBuilder doneUrl = new StringBuilder();
		if (request.getContextPath() != null) doneUrl.append(request.getContextPath());
		if (request.getServletPath() != null) doneUrl.append(request.getServletPath());
		if (request.getPathInfo() != null) doneUrl.append(request.getPathInfo());
		return doneUrl;
	}

	private void populateSite(Map<String, Object> editModel, PortalNode node) {
			editModel.put("site", createSiteMap(node.getSite()));
			List<PortalNode> nodes = PortalHierarchyService.getInstance().getNodesWithSite(node.getSite().getId());
			List<String> paths = new ArrayList<String>(nodes.size());
			for (PortalNode currentNode: nodes) {
				if (!node.getPath().equals(currentNode.getPath()))
					paths.add(currentNode.getPath());
			}
			editModel.put("otherPaths", paths);
	}
	
	private Map<String, Object> createSiteMap(Site site) {
		Map<String, Object> siteMap = new HashMap<String, Object>();
		siteMap.put("title", site.getTitle());
		siteMap.put("id", site.getId());
		siteMap.put("description", site.getDescription());
		return siteMap;
	}

	private void populateNode(PortalNode node, Map<String, Object> model) {
		Map<String, Object> nodeMap = new HashMap<String, Object>();
		model.put("node", nodeMap);

		nodeMap.put("path", node.getPath());
		nodeMap.put("nodeid", node.getId());

		List<Map<String, String>> nodeProperties = new ArrayList<Map<String, String>>();
		nodeMap.put("properties", nodeProperties);
		

	}

	private void populateHierarchy(PortalNode node, Map<String, Object> nodeMap) {
		List<Map<String, String>> childrenNodes = new ArrayList<Map<String, String>>();
		nodeMap.put("children", childrenNodes);
		List<PortalNode> children = PortalHierarchyService.getInstance().getNodeChildren(node.getId());
		for (PortalNode child: children)
		{
			Map<String, String> prop = new HashMap<String, String>();
			prop.put("path", child.getPath());
			prop.put("nodeid", child.getId());
			
			prop.put("nodename", child.getSite().getTitle());
		
			log.info("Added "+child.getPath());
			childrenNodes.add(prop);
		}
		
	}
	



	private void populateModel(Map<String, Object> model, HttpServletRequest request)
	{
		model.put("sakai_fragment","false");
		model.put("sakai_head", (String) request
				.getAttribute("sakai.html.head"));
		model.put("sakai_onload", (String) request
				.getAttribute("sakai.html.body.onload"));

		model.put("toolTitle", "Hierarchy Manager");
		String editor = ServerConfigurationService.getString("wysiwyg.editor");
		model.put("sakai_editor", editor);
		model.put("sakai_library_path", "/library/");
		

		model.put("rootUrl", request.getContextPath()+request.getServletPath());
	}

}
