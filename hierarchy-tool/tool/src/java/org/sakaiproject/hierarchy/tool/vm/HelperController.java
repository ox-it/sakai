package org.sakaiproject.hierarchy.tool.vm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;



public abstract class HelperController {

	private static Log log = LogFactory.getLog(HelperController.class); 

	private ActiveToolManager activeToolManager;
	
	public abstract String getHelperId();

	@Autowired
	public void setActiveToolManager(ActiveToolManager activeToolManager) {
	    this.activeToolManager = activeToolManager;
	}

	@RequestMapping(method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		
		ActiveTool tool = activeToolManager.getActiveTool(getHelperId());
		if (log.isDebugEnabled()) {
			log.debug("Dispatching to: "+getHelperId()+ " for "+ req.getRequestURI());
		}
		req.removeAttribute(ActiveTool.NATIVE_URL);
		tool.help(req, res, null, null);
		return null;
	}

}
