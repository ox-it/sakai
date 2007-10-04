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
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ManagerController extends AbstractController
{

	private static final Log log = LogFactory.getLog(ManagerController.class);

	public static final String ACT_ADDNODE = "act_addnode";

	public static final String ACT_SITELIST = "act_sitelist";
	
	public static final String ACT_ADDSITE = "act_addsite";
	
	public static final String ACT_DELSITE = "act_delsite";

	public static final String ACT_SETPROPERTY = "act_setproperty";

	public static final String ACT_CLEARPROPERTY = "act_clearproperty";

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

			if (modified)
			{
				log.debug("Modified so attempting to save.");
				PortalHierarchyService.getInstance().begin();
				PortalHierarchyService.getInstance().save(node);
				PortalHierarchyService.getInstance().end();
			}

			Map<String, String> formActions = new HashMap<String, String>();
			model.put("formactions", formActions);
			formActions.put(ManagerController.ACT_ADDNODE,
					ManagerController.ACT_ADDNODE);
			formActions.put(ManagerController.ACT_SITELIST,
					ManagerController.ACT_SITELIST);
			formActions.put(ManagerController.ACT_SETPROPERTY,
					ManagerController.ACT_SETPROPERTY);
			formActions.put(ManagerController.ACT_CLEARPROPERTY,
					ManagerController.ACT_CLEARPROPERTY);
			formActions.put(ManagerController.REQUEST_ACTION,
					ManagerController.REQUEST_ACTION);
			formActions.put(ManagerController.REQUEST_SITES,
					ManagerController.REQUEST_SITES);
			formActions.put(ManagerController.REQUEST_NEWNODE,
					ManagerController.REQUEST_NEWNODE);
			formActions.put(ManagerController.REQUEST_PROPERTY,
					ManagerController.REQUEST_PROPERTY);
			formActions.put(ManagerController.REQUEST_VALUE,
					ManagerController.REQUEST_VALUE);
			
			List sites = SiteService.getSites(SelectionType.ANY, null, null,
					null, SortType.NONE, null);
			
			model.put("nodeSites", getSites(node));
			
			model.put("sites", sites);

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
			log.info("Got "+properties.size()+" properties ");

			for (Iterator i = properties.values().iterator(); i.hasNext();)
			{
				HierarchyProperty property = (HierarchyProperty) i.next();
				Map<String, String> prop = new HashMap<String, String>();
				prop.put("name", property.getName());
				prop.put("value", property.getPropvalue());
				nodeProperties.add(prop);
			}
			
			
			List<Map<String, String>> childrenNodes = new ArrayList<Map<String, String>>();
			nodeMap.put("children", childrenNodes);
			Map children = node.getChildren();
			log.info("Got "+children.size()+" children ");
			for (Iterator i = children.values().iterator(); i.hasNext();)
			{
				Hierarchy child = (Hierarchy) i.next();
				Map<String, String> prop = new HashMap<String, String>();
				prop.put("path", child.getPath());
				prop.put("nodeid", child.getId());
				log.info("Added "+child.getPath());
				childrenNodes.add(prop);
			}
			
			nodeMap.put("parent", node.getParent());
			log.info("Parent: "+ node.getParent());
			model.put("rootUrl", request.getContextPath()+request.getServletPath());
			
			return new ModelAndView("manager", model);
		}
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
	}

}
