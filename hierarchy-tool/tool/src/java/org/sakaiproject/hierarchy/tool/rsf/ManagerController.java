package org.sakaiproject.hierarchy.tool.rsf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.hierarchy.cover.HierarchyService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ManagerController extends AbstractController
{

	public static final String ACT_ADDNODE = "act_addnode";
	public static final String ACT_SITELIST = "act_sitelist";
	public static final String ACT_SETPROPERTY = "act_setproperty";
	public static final String ACT_CLEARPROPERTY = "act_clearproperty";
	public static final String REQUEST_ACTION = "_action";
	public static final String REQUEST_SITES = "_sites";
	public static final String REQUEST_NEWNODE = "_newnode";
	public static final String REQUEST_PROPERTY = "_property";
	public static final String REQUEST_VALUE = "_value";

	public ManagerController()
	{
		super();
	}
	
	public void init() {
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		Hierarchy node = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getCurrentPortalNode();
		Placement p = ToolManager.getCurrentPlacement();
		String siteContext = p.getContext();
		if (node == null)
		{
			return new ModelAndView("nonode");
		}
		else
		{
			String action = request.getParameter(REQUEST_ACTION);
			boolean modified = true;
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
				node.addToproperties(PortalHierarchyService.PORTAL_SITES, siteList);
				modified = true;
			}
			else if (ACT_ADDNODE.equals(action))
			{
				String newNode = request.getParameter(REQUEST_NEWNODE);
				Hierarchy h = HierarchyService.getInstance().newHierarchy(
						node.getPath() + "/" + newNode);
				node.addTochildren(h);
				h.addToproperties(PortalHierarchyService.MANAGEMENT_SITE,
						siteContext);
				modified = true;
			}
			else if (ACT_SETPROPERTY.equals(action))
			{
				String[] propName = request
						.getParameterValues(REQUEST_PROPERTY);
				String[] propValue = request.getParameterValues(REQUEST_VALUE);
				for (int i = 0; i < propName.length; i++)
				{
					node.addToproperties(propName[i], propValue[i]);
				}
				modified = true;
			}
			else if (ACT_CLEARPROPERTY.equals(action))
			{
				String[] propName = request.getParameterValues(REQUEST_PROPERTY);
				for (int i = 0; i < propName.length; i++)
				{
					node.addToproperties(propName[i], null);
				}
				modified = true;
			}

			if (modified)
			{
				HierarchyService.getInstance().save(node);
			}

		 				
			return new ModelAndView("manager");
		}
	}


}
