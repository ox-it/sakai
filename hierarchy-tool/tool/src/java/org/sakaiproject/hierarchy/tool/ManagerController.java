package org.sakaiproject.hierarchy.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	private static final String ACT_ADDNODE = "act_addnode";
	private static final String ACT_SITELIST = "act_sitelist";
	private static final String ACT_SETPROPERTY = "act_setproperty";
	private static final String ACT_CLEARPROPERTY = "act_clearproperty";
	private static final String REQUEST_ACTION = "_action";
	private static final String REQUEST_SITES = "_sites";
	private static final String REQUEST_NEWNODE = "_newnode";
	private static final String REQUEST_PROPERTY = "_property";
	private static final String REQUEST_VALUE = "_value";

	public ManagerController()
	{
		super();
	}
	
	public void init() {
		
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		Hierarchy node = HierarchyService.getCurrentPortalNode();
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

			// render the output
			Map model = new HashMap();
			
			Map nodeMap = new HashMap();
			model.put("currentNode", nodeMap);
			model.put("sakai_fragment", Boolean.FALSE );
			model.put("sakai_head", request.getAttribute("sakai.html.head"));
			model.put("sakai_onload",request.getAttribute("sakai.html.body.onload"));
			
			model.put("toolTitle","Hierarchy Manager");
			nodeMap.put("path", node.getPath());
			Map properties = node.getProperties();
			List propValues = new ArrayList();
			for (Iterator i = properties.keySet().iterator(); i.hasNext();)
			{
				String key = (String) i.next();
				HierarchyProperty hp = (HierarchyProperty) properties.get(key);
				Map m = new HashMap();
				m.put("name", key);
				m.put("value", hp.getPropvalue());
				propValues.add(m);
			}

			Map m = new HashMap();
			m.put(ACT_ADDNODE, ACT_ADDNODE);
			m.put(ACT_SITELIST, ACT_SITELIST);
			m.put(ACT_SETPROPERTY, ACT_SETPROPERTY);
			m.put(ACT_CLEARPROPERTY, ACT_CLEARPROPERTY);
			m.put(REQUEST_ACTION, REQUEST_ACTION);
			m.put(REQUEST_SITES, REQUEST_SITES);
			m.put(REQUEST_NEWNODE, REQUEST_NEWNODE);
			m.put(REQUEST_PROPERTY, REQUEST_PROPERTY);
			m.put(REQUEST_VALUE, REQUEST_VALUE);
			nodeMap.put("form", m);
			nodeMap.put("properties", propValues);
			return new ModelAndView("manager", "manager", model);
		}
	}

}
