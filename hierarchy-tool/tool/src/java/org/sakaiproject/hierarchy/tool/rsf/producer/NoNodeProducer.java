package org.sakaiproject.hierarchy.tool.rsf.producer;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class NoNodeProducer implements ViewComponentProducer
{

	private static final String VIEW_ID = "nonode";

	private HttpServletRequest httpServletRequest;

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters arg1,
			ComponentChecker arg2)
	{
		UIOutput.make(tofill, "sakai_head", (String) httpServletRequest
				.getAttribute("sakai.html.head"));
		UIBranchContainer bodyContainier = UIBranchContainer.make(tofill,"bodytag:");
		Map m = new HashMap();
		m.put("onload",httpServletRequest
				.getAttribute("sakai.html.body.onload"));
		bodyContainier.decorators.add(new UIFreeAttributeDecorator());
		
		UIOutput.make(tofill, "toolTitle", "Hierarchy Manager");
		
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
