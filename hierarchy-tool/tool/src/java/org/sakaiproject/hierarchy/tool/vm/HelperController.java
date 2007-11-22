package org.sakaiproject.hierarchy.tool.vm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.portal.charon.handlers.ToolHandler;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;;

public class HelperController extends AbstractController {

	private String helperId;
	
	private static Log log = LogFactory.getLog(HelperController.class); 
	
	public String getHelperId() {
		return helperId;
	}

	public void setHelperId(String helperId) {
		this.helperId = helperId;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		
		ActiveTool tool = ActiveToolManager.getActiveTool(helperId);
		String path = "/sites";

		String context = req.getContextPath() + req.getServletPath() + path;
		log.info(context);
		req.removeAttribute(ActiveTool.NATIVE_URL);
		tool.help(req, res, null, null);
		return null;
	}

}
