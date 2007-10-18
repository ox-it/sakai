package org.sakaiproject.portal.charon.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Web;

public class HierarchyToolResetHandler extends ToolHandler {
		public HierarchyToolResetHandler()
		{
			urlFragment = "hierarchytool-reset";
		}

		@Override
		public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
				Session session) throws PortalHandlerException
		{
			if ((parts.length > 2) && (parts[1].equals(urlFragment)))
			{
				try
				{
					String toolUrl = req.getContextPath() + "/hierarchytool"
							+ Web.makePath(parts, 2, parts.length);
					// Make sure to add the parameters such as panel=Main
					String queryString = req.getQueryString();
					if (queryString != null)
					{
						toolUrl = toolUrl + "?" + queryString;
					}
					portalService.setResetState("true");
					res.sendRedirect(toolUrl);
					return RESET_DONE;
				}
				catch (Exception ex)
				{
					throw new PortalHandlerException(ex);
				}
			}
			else
			{
				return NEXT;
			}
		}

}
