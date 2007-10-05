package org.sakaiproject.hierarchy.tool.rsf.producer;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.hierarchy.cover.HierarchyService;
import org.sakaiproject.hierarchy.cover.PortalHierarchyService;
import org.sakaiproject.hierarchy.tool.rsf.ManagerController;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ManagerProducer implements ViewComponentProducer
{

	private static final String VIEW_ID = "manager";

	private HttpServletRequest httpServletRequest;

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{

		Hierarchy node = PortalHierarchyService.getCurrentPortalNode();

		UIOutput.make(tofill, "sakai_head", (String) httpServletRequest
				.getAttribute("sakai.html.head"));
		UIOutput.make(tofill, "sakai_onload", (String) httpServletRequest
				.getAttribute("sakai.html.body.onload"));

		UIOutput.make(tofill, "toolTitle", "Hierarchy Manager");

		UIBranchContainer formactions = UIBranchContainer.make(tofill,
				"formactions:");
		UIOutput.make(formactions, ManagerController.ACT_ADDNODE,
				ManagerController.ACT_ADDNODE);
		UIOutput.make(formactions, ManagerController.ACT_SITELIST,
				ManagerController.ACT_SITELIST);
		UIOutput.make(formactions, ManagerController.ACT_SETPROPERTY,
				ManagerController.ACT_SETPROPERTY);
		UIOutput.make(formactions, ManagerController.ACT_CLEARPROPERTY,
				ManagerController.ACT_CLEARPROPERTY);
		UIOutput.make(formactions, ManagerController.REQUEST_ACTION,
				ManagerController.REQUEST_ACTION);
		UIOutput.make(formactions, ManagerController.REQUEST_SITES,
				ManagerController.REQUEST_SITES);
		UIOutput.make(formactions, ManagerController.REQUEST_NEWNODE,
				ManagerController.REQUEST_NEWNODE);
		UIOutput.make(formactions, ManagerController.REQUEST_PROPERTY,
				ManagerController.REQUEST_PROPERTY);
		UIOutput.make(formactions, ManagerController.REQUEST_VALUE,
				ManagerController.REQUEST_VALUE);

		UIBranchContainer uinode = UIBranchContainer.make(tofill, "node");
		UIOutput.make(uinode, "path", node.getPath());
		UIOutput.make(uinode, "pathhash", node.getPathHash());
		UIOutput.make(uinode, "realm", node.getRealm());
		UIOutput.make(uinode, "nodeid", node.getId());
		UIOutput.make(uinode, "version", node.getVersion().toString());

		UIBranchContainer pops = UIBranchContainer.make(uinode, "properties");
		Map properties = node.getProperties();
		for (Iterator i = properties.values().iterator(); i.hasNext();)
		{
			HierarchyProperty property = (HierarchyProperty) i.next();
			UIOutput.make(uinode, property.getName(), property.getPropvalue());
		}
	}

	public HttpServletRequest getHttpServletRequest()
	{
		return httpServletRequest;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest)
	{
		this.httpServletRequest = httpServletRequest;
	}
}
