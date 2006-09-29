package org.sakaiproject.hierarchy.tool.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.Velocity;
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

	private static final Log log = LogFactory.getLog(ManagerController.class);

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

	public void init()
	{
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{

		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		Hierarchy node = HierarchyService.getCurrentPortalNode();
		Placement p = ToolManager.getCurrentPlacement();
		String siteContext = p.getContext();
		Map model = new HashMap();
		model.put("sakai_head", (String) request
				.getAttribute("sakai.html.head"));
		model.put("sakai_onload", (String) request
				.getAttribute("sakai.html.body.onload"));

		model.put("toolTitle", "Hierarchy Manager");

		if (node == null)
		{

			return new ModelAndView("nonode", model);
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
				node.addToproperties(HierarchyProperty.PORTAL_SITES, siteList);
				modified = true;
			}
			else if (ACT_ADDNODE.equals(action))
			{
				String newNode = request.getParameter(REQUEST_NEWNODE);
				Hierarchy h = HierarchyService.getInstance().newHierarchy(
						node.getPath() + "/" + newNode);
				node.addTochildren(h);
				h.addToproperties(HierarchyProperty.MANAGEMENT_SITE,
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
				HierarchyService.getInstance().save(node);
			}

			Map formActions = new HashMap();
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

			Map nodeMap = new HashMap();
			model.put("node", nodeMap);

			nodeMap.put("path", node.getPath());
			nodeMap.put("pathhash", node.getPathHash());
			nodeMap.put("realm", node.getRealm());
			nodeMap.put("nodeid", node.getId());
			nodeMap.put("version", node.getVersion().toString());

			List nodeProperties = new ArrayList();
			nodeMap.put("properties", nodeProperties);

			Map properties = node.getProperties();
			for (Iterator i = properties.values().iterator(); i.hasNext();)
			{
				Map prop = new HashMap();
				HierarchyProperty property = (HierarchyProperty) i.next();
				prop.put("name", property.getName());
				prop.put("value", property.getPropvalue());
			}
			return new ModelAndView("manager", model);
		}
	}

}
