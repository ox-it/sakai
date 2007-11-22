package org.sakaiproject.hierarchy.tool.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
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

		Hierarchy node = null;
		String currentPath = request.getPathInfo();
		if (currentPath != null && currentPath.length() > 0)
		{
			node = PortalHierarchyService.getNode(currentPath);
		}
		if (node == null)
		{
			node = PortalHierarchyService.getCurrentPortalNode();
			if ( node == null ) {
				node = PortalHierarchyService.getNode(null);
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
			if (ACT_SITELIST.equals(action))
			{
				String[] sites = request.getParameterValues(REQUEST_SITES);
				String siteList = "";
				if (sites != null)
				{
					for (int i = 0; i < sites.length; i++)
					{
						siteList = siteList + sites[i] + ";";
					}
				}
				node.addToproperties(org.sakaiproject.hierarchy.api.PortalHierarchyService.PORTAL_SITES, siteList);
				modified = true;
			}
			else if (ACT_ADDNODE.equals(action))
			{
				String newNode = request.getParameter(REQUEST_NEWNODE);
				Hierarchy h = PortalHierarchyService.newHierarchy(
						node.getPath() + "/" + newNode);
				node.addTochildren(h);
				
				String siteId = request.getParameter(REQUEST_SITE);
				h.addToproperties(org.sakaiproject.hierarchy.api.PortalHierarchyService.CONTENT, siteId);
				modified = true;
			}
			else if (ACT_ADDSITE.equals(action)) {
				String siteId = request.getParameter(REQUEST_SITE);
				List<String> siteIds = getSiteIds(node);
				if (siteId != null && siteId.trim().length() > 0 && !siteIds.contains(siteId)) {
					StringBuilder siteList = new StringBuilder(siteId);
					for (String site : siteIds)
					{
						siteList.append(";");
						siteList.append(site);
					}
					log.debug("Setting site list to :"+ siteList.toString());
					node
							.addToproperties(
									org.sakaiproject.hierarchy.api.PortalHierarchyService.PORTAL_SITES,
									siteList.toString());
					modified = true;
				}
			} else if (ACT_DELSITE.equals(action)) {
				String siteId = request.getParameter(REQUEST_SITE);
				List<String> siteIds = getSiteIds(node);
				if (siteIds.contains(siteId)) {
					StringBuilder siteList = new StringBuilder("");
					for (String site: siteIds)
					{
						if (!site.equals(siteId))
						{
							if (siteList.length() > 0)
							{
								siteList.append(";");
							}
							siteList.append(site);
							
						}
					}
					node
							.addToproperties(
									org.sakaiproject.hierarchy.api.PortalHierarchyService.PORTAL_SITES,
									siteList.toString());
					modified = true;
				}
				
			}
			else if (ACT_SETPROPERTY.equals(action))
			{
				String[] propName = request
						.getParameterValues(REQUEST_PROPERTY);
				String[] propValue = request.getParameterValues(REQUEST_VALUE);
				for (int i = 0; i < propName.length; i++)
				{
					if (propName[i] == null || propName[i].length() < 1)
						continue;
					if (propValue[i] != null && propValue[i].length() < 1)
						propValue[i] = null;
					node.addToproperties(propName[i], propValue[i]);
				}
				modified = true;
			}
			else if (ACT_CLEARPROPERTY.equals(action))
			{
				String[] propName = request
						.getParameterValues(REQUEST_PROPERTY);
				for (int i = 0; i < propName.length; i++)
				{
					node.addToproperties(propName[i], null);
				}
				modified = true;
			}
			else if (ACT_SELECT_SITE.equals(action))
			{
				ToolSession toolSession = SessionManager.getCurrentToolSession();
				
				toolSession.setAttribute(Tool.HELPER_DONE_URL, buildUrl(request) +"?"+REQUEST_ACTION+"="+ACT_EDIT_SITE);
				toolSession.setAttribute(SiteHelper.SITE_PICKER_PERMISSION, org.sakaiproject.site.api.SiteService.SelectionType.UPDATE);
				
				return new ModelAndView(new RedirectView("/sites", true), null);
			}
			else if (ACT_NEW_SITE.equals(action))
			{
				ToolSession toolSession = SessionManager.getCurrentToolSession();
				// TODO Need to change done URL.
				toolSession.setAttribute(Tool.HELPER_DONE_URL, buildUrl(request)+"?"+REQUEST_ACTION+"="+ACT_EDIT_SITE);
				toolSession.setAttribute(SiteHelper.SITE_CREATE_SITE_TYPES, "project,course");
				return new ModelAndView(new RedirectView("/create", true), null);
			}
			else if (ACT_EDIT_SITE.equals(action))
			{
				Map editModel = new HashMap();
				editModel.put("updating", Boolean.TRUE);
				populateModel(editModel, request);
				populateNode(node, editModel);
				
				// Check to see if the user has come back from helper.
				ToolSession toolSession = SessionManager.getCurrentToolSession();
				Object siteAttribute = toolSession.getAttribute(SiteHelper.SITE_PICKER_SITE_ID); 
				if ( siteAttribute instanceof String) 
				{
					editModel.put("site", createSiteMap((String)siteAttribute));
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
					node.addToproperties(org.sakaiproject.hierarchy.api.PortalHierarchyService.CONTENT, siteId);
					try 
					{
						PortalHierarchyService.getInstance().begin();
						PortalHierarchyService.getInstance().save(node);
					} finally {
						PortalHierarchyService.getInstance().end();
					}
				}
			}
			Map showModel = new HashMap();
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

	private void populateSite(Map editModel, Hierarchy node) {
		HierarchyProperty prop = node.getProperty(org.sakaiproject.hierarchy.api.PortalHierarchyService.CONTENT);
		if (prop != null)
		{
			editModel.put("site", createSiteMap(prop.getPropvalue()));
			List<Hierarchy> nodes = PortalHierarchyService.getNodesByProperty(prop.getName(), prop.getPropvalue());
			List<String> paths = new ArrayList(nodes.size());
			for (Hierarchy hierarchy: nodes) {
				if (!node.getPath().equals(hierarchy.getPath()))
					paths.add(hierarchy.getPath());
			}
			editModel.put("otherPaths", paths);
		}
	}
	
	private Map createSiteMap(String siteId) {
		Map<String, Object> siteMap = new HashMap<String, Object>();
		try
		{
			Site newSite = SiteService.getSite(siteId);
			siteMap.put("title", newSite.getTitle());
			siteMap.put("id", newSite.getId());
			siteMap.put("description", newSite.getDescription());
		}
		catch (IdUnusedException iue) 
		{
			return null;
		}
		return siteMap;
	}

	private void populateNode(Hierarchy node, Map<String, Object> model) {
		Map<String, Object> nodeMap = new HashMap<String, Object>();
		model.put("node", nodeMap);

		nodeMap.put("path", node.getPath());
		nodeMap.put("pathhash", node.getPathHash());
		nodeMap.put("realm", node.getRealm());
		nodeMap.put("nodeid", node.getId());
		nodeMap.put("version", node.getVersion().toString());

		List<Map<String, String>> nodeProperties = new ArrayList<Map<String, String>>();
		nodeMap.put("properties", nodeProperties);
		Map properties = node.getProperties();

		for (Iterator i = properties.values().iterator(); i.hasNext();)
		{
			HierarchyProperty property = (HierarchyProperty) i.next();
			Map<String, String> prop = new HashMap<String, String>();
			prop.put("name", property.getName());
			prop.put("value", property.getPropvalue());
			nodeProperties.add(prop);
		}
		
		

	}

	private void populateHierarchy(Hierarchy node, Map<String, Object> nodeMap) {
		List<Map<String, String>> childrenNodes = new ArrayList<Map<String, String>>();
		nodeMap.put("children", childrenNodes);
		Map children = node.getChildren();
		for (Iterator i = children.values().iterator(); i.hasNext();)
		{
			Hierarchy child = (Hierarchy) i.next();
			Map<String, String> prop = new HashMap<String, String>();
			prop.put("path", child.getPath());
			prop.put("nodeid", child.getId());
			HierarchyProperty siteProperty = node.getProperty(org.sakaiproject.hierarchy.api.PortalHierarchyService.PORTAL_SITES);
			if (siteProperty != null) {
				String siteId = siteProperty.getPropvalue();
				try {
					Site childSite = SiteService.getSite(siteId);
					prop.put("nodename", childSite.getTitle());
				} catch (IdUnusedException iue) {
					//
				}
			}
			log.info("Added "+child.getPath());
			childrenNodes.add(prop);
		}
		
		nodeMap.put("parent", node.getParent());
	}
	
	// Should be in service.
	private static List<Site> getSites(Hierarchy node) 
	{
		List siteIds = getSiteIds(node);
		List<Site> sitesList = new ArrayList<Site>(siteIds.size());
		for (String siteId : getSiteIds(node)) {
			try {
				Site site = SiteService.getSite(siteId);
				sitesList.add(site);
			} catch (IdUnusedException e) {
				log
						.warn("Hierarchy Property contains site that doesn't exist: "
								+ siteId);
			}

		}
		return sitesList;
	}
	
	private static List<String> getSiteIds(Hierarchy node)
	{
		HierarchyProperty sitesProp = node.getProperty(org.sakaiproject.hierarchy.api.PortalHierarchyService.PORTAL_SITES);
		List <String>siteIds = new ArrayList<String>(1);
		if (sitesProp != null) {
			String sites = sitesProp.getPropvalue();
			if (sites != null) {
				siteIds = Arrays.asList(sites.split(";"));
			}
		}
		return siteIds;
	
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
