package org.sakaiproject.hierarchy.tool;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.mvc.AbstractController;

public class ToolHandlerMapping extends AbstractHandlerMapping
{

	private Map mappings = null;

	private AbstractController defaultHandler;

	public ToolHandlerMapping()
	{
		super();
	}

	protected Object getHandlerInternal(HttpServletRequest request)
			throws Exception
	{
		Placement p = ToolManager.getCurrentPlacement();
		String toolId = p.getTool().getId();

		Object handler = mappings.get(toolId);
		if (handler == null)
		{
			return defaultHandler;
		}
		else
		{
			return handler;
		}
	}

	public Map getMappings()
	{
		return mappings;
	}

	public void setMappings(Map mappings)
	{
		this.mappings = mappings;
	}

}
